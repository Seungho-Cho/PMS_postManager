# 프로젝트 기획

## 1. 프로젝트 개요
스케일 모델 동호회 Discord와 연동되는 웹 앱.
Discord에 올라온 사진을 자동 수집해 외부 스토리지에 저장하고,
웹앱에서 콘텐츠를 작성해 SNS(Instagram, X)에 동시 포스팅하는 스케줄링 시스템.

### 핵심 플로우
1. Discord 특정 채널에 사진 업로드
2. Discord 봇이 사진 크롤링 → Cloudflare R2 업로드 (원본 + 썸네일)
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

### 서비스 구성 (3개)
- **api**: REST API, 인증, 도메인 로직 대부분 (Gradle 멀티모듈로 도메인 분리)
  - 도메인 모듈: 사용자/인증, 사진/포스팅, 태그/번역, 스케줄링
- **discord-bot**: JDA 기반, 상시 WebSocket 연결 유지, 사진 업로드 이벤트 감지
- **worker**: 이미지 처리, SNS 포스팅 실행, 번역 호출 등 시간 소요/재시도 필요 작업
- 통신: Kafka 이벤트 기반
- DB: PostgreSQL 단일 인스턴스 공유 (MSA 원칙상 분리 권장되나, 이 규모에서는 단일 DB가 더 실용적)

### 분리 이유 (면접 답변 포인트)
- MSA 철학이 아니라 **운영상 필요**에 의한 분리
  - 봇(상시 연결) vs API(요청-응답) 실행 모델 차이
  - 배포 시 봇 연결 끊김 방지
  - API 스케일아웃 시 봇 중복 실행 방지

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
  - Discord 사진 업로드 이벤트
  - R2 업로드 완료 이벤트
  - 포스팅 스케줄 트리거
- 스케줄링: Spring `@Scheduled` → 필요시 Quartz로 확장

### 4-4. 외부 연동
- Discord: JDA 라이브러리
- Discord OAuth2: Spring Security OAuth2 Client
- Cloudflare R2: AWS SDK for Java (S3 호환)
- 썸네일 생성: Thumbnailator (경량 라이브러리)
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
- 분산 추적 (Zipkin/Tempo) — 서비스 3개 규모에선 효용 낮음
- ELK 풀스택 — 리소스 부담 大

### 4-9. 인프라/CI-CD
- Docker (멀티스테이지 빌드)
- Kubernetes (홈서버 k3s)
- GitHub Actions (빌드/테스트/이미지 빌드)
- ArgoCD (GitOps 배포, 서비스 3개 → 멀티 앱 관리)
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
