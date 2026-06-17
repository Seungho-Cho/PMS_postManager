# 프로젝트 기획

## 1. 프로젝트 개요
스케일 모델 동호회 Discord와 연동되는 웹 앱.
Discord에 올라온 사진을 자동 수집해 외부 스토리지에 저장하고,
웹앱에서 콘텐츠를 작성해 SNS(Instagram, X)에 동시 포스팅하는 스케줄링 시스템.

### 핵심 플로우
1. Discord 특정 채널에 사진 업로드
2. Discord 봇이 사진 첨부파일 URL을 감지해 api로 전달 → api가 Cloudflare R2 업로드 (원본 + 썸네일)
3. 웹앱에서 업로드된 사진 기반으로 포스팅 콘텐츠 작성
   - 태그 추천
   - 영문 번역 (Groq Free API)
4. 스케줄링하여 Instagram + X 동시 포스팅
5. Discord 계정 연동(OAuth2)으로 웹앱 계정 관리
6. 각종 API 키 값 관리

### 목적
7년차 백엔드 개발자 포트폴리오용. 사용 가능한 기술을 최대한 폭넓게,
하지만 과설계 없이 실용적인 근거를 가지고 적용.

---

## 2. 프로젝트명 후보

| 컨셉 | 후보 | 비고 |
|---|---|---|
| 스케일모델 컨셉 | Diorama | 미니어처 작품을 전시하는 장면 → 프로젝트 성격과 부합 |
| 스케일모델 컨셉 | Scaler | 스케일 모델 + 확장/처리 의미 중복 |
| 스케일모델 컨셉 | ModelDock | 모형을 정박/보관하는 공간 뉘앙스 |
| 기능 직관형 | PicRelay | 사진 중계 의미가 명확, 기술 문서에 적합 |
| 기능 직관형 | SnapBridge | Discord-SNS를 잇는 다리 |
| 감성/브랜드형 | Modelog | Model + Log, 모형 기록 |
| 감성/브랜드형 | Kitfolio | Kit + Portfolio |
| 감성/브랜드형 | Plamo | プラモ, 모형 동호인들에게 익숙한 단어 |

**추천**: Modelog 또는 Diorama (서비스명 전체) / PicRelay, SnapBridge (내부 모듈·아키텍처용)

현재 Gradle 프로젝트명은 `mason_insta` (group: `com.mason`) — 추후 변경 가능.

---

## 3. 아키텍처 방향

### 풀 MSA 지양 → "모듈화된 모놀리스 + 부분 분리"
1인 개발 + 낮은 트래픽 환경에서 풀 MSA는 오버엔지니어링.
실행 모델이 다른 컴포넌트만 분리하는 것이 합리적.

### 서비스 구성 (2개 + 공통 모듈)
- **api**: REST API, 인증, 도메인 로직 전부 (Post/Photo/Tag), 예약 발행 스케줄링(`@Scheduled`), DB 쓰기 단일 책임자
  - 도메인 모듈: 사용자/인증, 포스팅(Post/Photo), 태그/번역, 스케줄링
- **discord-bot**: JDA 기반, 상시 WebSocket 연결 유지, 특정 채널 사진 업로드 감지 → 첨부파일 URL을 Kafka로 전달 (R2 업로드는 직접 하지 않음, 자격증명을 들고 있지 않음)
- **common**: 두 서비스가 공유하는 라이브러리 모듈 (도메인 로직 없음). 현재는 Kafka 이벤트 DTO만 가짐
  - R2(`com.mason.api.r2`), Groq(`com.mason.api.groq`), Instagram Graph API(`com.mason.api.instagram`), X API(`com.mason.api.x`) 클라이언트는 모두 `api`에서만 쓰는 단순 HTTP 호출이라 `common`으로 분리하지 않고 `api` 내부에 위치 — 두 번째 사용처가 생기면 그때 `common`으로 끌어올린다
- 통신: Kafka로 비동기 처리 (아래 "포스팅 생성 플로우" 참고), 나머지(예약 발행 등)는 api 내부에서 처리
- DB: PostgreSQL, `api`만 접근 (다른 서비스는 직접 DB를 건드리지 않음)

### 분리 이유 (면접 답변 포인트)
- MSA 철학이 아니라 **운영상 필요**가 있는 컴포넌트만 분리
  - 봇(상시 WebSocket 연결) vs API(요청-응답 + 스케줄러) 실행 모델 차이
  - 배포 시 봇 연결 끊김 방지, API 스케일아웃 시 봇 중복 실행 방지
- 별도 서비스로 분리할 운영상 근거가 없는 부분(이미지 처리/SNS 포스팅 실행/번역 호출)은 굳이 쪼개지 않고 `api` 내부 스케줄러 + `api` 내부 외부 클라이언트로 처리
  - 외부 클라이언트는 전부 `api`에서만 쓰여서 `common`으로 공유할 이유가 없고, DB 쓰기 책임도 `api`로 단일화되어 정합성 문제를 피함
- R2 자격증명은 DB(`api`)에만 저장 — discord-bot은 R2 업로드를 직접 하지 않고 Discord CDN URL만 전달하므로 비밀키를 공유할 필요가 없음

### 포스팅 생성 플로우 (Discord 사진 업로드 → Post 생성)
1. discord-bot이 특정 채널 메시지의 첨부파일(사진)을 감지 — 한 메시지에 여러 장이면 카루셀로 간주, `discordMessageId`가 그룹 키
2. discord-bot이 Kafka에 발행: `PhotoUploadRequested { discordMessageId, discordChannelId, uploaderDiscordId, attachmentUrls[] }`
3. api가 이 이벤트를 구독 → 각 URL을 다운로드 → `api` 내부 R2 클라이언트로 원본 업로드 + Thumbnailator로 썸네일 생성 후 업로드
4. api가 R2 URL 기반으로 `Photo`를 생성(N장), `discordMessageId`로 `Post`를 생성(DRAFT 상태)하고 `addPhoto()`로 연결 후 저장
   - `discordMessageId`는 `Post`의 unique 컬럼 — Kafka 재전송으로 인한 중복 생성을 멱등하게 방지
5. **(예정/미구현)** Post 생성이 정상 완료되면 api가 `PostCreated { discordMessageId, postEditUrl }`를 Kafka로 역방향 발행 → discord-bot이 원본 메시지에 "포스트 수정하기" 링크가 달린 버튼(JDA Link Button)을 답글로 추가
   - 이 단계가 추가되면 Kafka 통신이 `discord-bot → api` 단방향에서 양방향으로 바뀜

---

## 4. 기술 스택

### 4-1. 코어 프레임워크
- Spring Boot 3.x (최신 3.5.x 계열) — 단, 현재 build.gradle은 4.1.0으로 설정되어 있음 (확인 필요)
- Java 21 LTS
- Virtual Threads 활성화 (Spring Boot 3.2+) — I/O 바운드 외부 호출 多 → 명확한 도입 근거
- Java 21 최신 문법: record, sealed class, pattern matching

### 4-2. 데이터 계층
- PostgreSQL (메인 DB)
- Spring Data JPA + QueryDSL
- Redis
  - 캐싱
  - Discord 연동 토큰 저장
  - 스케줄링 분산 락 (Redisson)
  - 외부 API 레이트리밋 카운터

### 4-3. 비동기/메시징 (핵심 차별점)
- Kafka
  - discord-bot → api: `PhotoUploadRequested` (Discord 사진 첨부 감지 이벤트, R2 업로드는 api가 수신 후 처리)
  - api → discord-bot: `DiscordBotConnectCommand` (시작/종료/재시작 지시)
- 스케줄링: api 내부에서 Spring `@Scheduled`로 예약 발행 대상 조회 → api 내부 외부 클라이언트 호출 → 필요시 Quartz로 확장

### 4-4. 외부 연동
- Discord: JDA 라이브러리 (discord-bot에서만 사용)
- Discord OAuth2: Spring Security OAuth2 Client (api)
- Cloudflare R2: AWS SDK for Java (S3 호환) — `api`에서만 호출 (`com.mason.api.r2`), discord-bot은 R2를 직접 다루지 않음
- 썸네일 생성: Thumbnailator (경량 라이브러리, api에서만 사용)
- Instagram Graph API 클라이언트는 `api` 내부(`com.mason.api.instagram`)에 위치
- X API 클라이언트는 `api` 내부(`com.mason.api.x`)에 위치
- Groq API 클라이언트는 `api` 내부(`com.mason.api.groq`)에 위치
- 위 세 클라이언트 모두 api에서만 쓰는 단순 HTTP 호출이라 `common`으로 분리하지 않음
- HTTP 클라이언트: WebClient / RestClient
- 장애 대응: Resilience4j (재시도, 서킷브레이커, 레이트리밋)
  - 대상: Instagram Graph API, X API, Groq API (모두 정책 제약 있는 외부 의존성)

### 4-5. 인증/보안
- Spring Security 6.x
- Discord OAuth2 (메인 로그인 수단)
- API 키 관리: AES 암호화 후 DB 저장 + 환경변수 분리
- (여유시) HashiCorp Vault — 시크릿 관리 외부화

### 4-6. API 문서/검증
- SpringDoc OpenAPI (Swagger UI)
- Bean Validation (Jakarta Validation)

### 4-7. 테스트
- JUnit 5 + Mockito
- Testcontainers (PostgreSQL/Redis/Kafka 통합 테스트)
- MockWebServer / WireMock (외부 API 모킹 — 타임아웃, 5xx, 레이트리밋 시나리오 검증)

### 4-8. 관찰성 (적정화 버전)
**필수**
- Actuator + Micrometer (Resilience4j 서킷브레이커 상태, 외부 API 호출 메트릭 자동 노출)
- 구조화 로깅 (Logback JSON)

**여유 있으면**
- Prometheus + Grafana (외부 API별 성공률/응답시간/서킷브레이커 오픈 횟수 대시보드)

**생략 / 향후계획으로만 언급**
- 분산 추적 (Zipkin/Tempo) — 서비스 2개 규모에선 효용 낮음
- ELK 풀스택 — 리소스 부담 大

### 4-9. 인프라/CI-CD
- Docker (멀티스테이지 빌드)
- Kubernetes (홈서버 k3s)
- GitHub Actions (빌드/테스트/이미지 빌드)
- ArgoCD (GitOps 배포, 서비스 2개 → 멀티 앱 관리)
- Helm 차트 (배포 설정 관리)

---

## 5. 우선순위 요약

| 구분 | 항목 |
|---|---|
| **필수** | Spring Boot 3 + Java 21(Virtual Threads), PostgreSQL/Redis, Kafka, Spring Security OAuth2(Discord), WebClient + Resilience4j, Testcontainers, Actuator+Micrometer, Docker/k8s + GitHub Actions + ArgoCD |
| **여유 있으면** | QueryDSL, Vault, Prometheus+Grafana, Quartz |
| **생략/향후계획** | 분산 추적, ELK 풀스택, 풀 MSA(서비스 디스커버리, API Gateway 등) |

---

## 6. 다음 단계 (TODO)
- [ ] 프로젝트명 최종 확정 + 도메인/GitHub organization 이름 가용성 확인
- [ ] ERD 설계
- [ ] Kafka 토픽 설계 (이벤트 스키마 포함)
- [ ] Gradle 멀티모듈 구조 설계 (api 서비스 내부 도메인 모듈 분리)
- [ ] 모노레포 vs 멀티레포 결정
- [ ] Instagram Graph API / X API 정책 제약사항 사전 조사 (앱 심사, 레이트리밋, 쓰기 권한)
