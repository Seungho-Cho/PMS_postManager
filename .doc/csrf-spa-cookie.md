# CSRF 403 (config POST/PUT/DELETE) 트러블슈팅

## 증상
`/admin/configs.html`에서 설정 추가/수정/삭제(POST/PUT/DELETE `/api/configs`)
요청이 모두 403으로 거부됨.

## 원인 (2단계)

### 1. CSRF 토큰을 아예 보내지 않음
`SecurityConfig`에 CSRF 관련 설정이 없어 Spring Security 기본 보호(세션 기반 CSRF)가
켜져 있었는데, `admin-configs.js`의 fetch 호출이 CSRF 토큰을 전혀 첨부하지 않았음.

### 2. 쿠키 기반 CSRF의 토큰 마스킹 불일치
1차 수정으로 `CookieCsrfTokenRepository`를 적용해 `XSRF-TOKEN` 쿠키를 내려주고
JS에서 이를 읽어 `X-XSRF-TOKEN` 헤더로 보내도록 했음에도 여전히 403이 발생.

원인: Spring Security 6부터 기본 `CsrfTokenRequestHandler`가
`XorCsrfTokenRequestAttributeHandler`로 바뀌었음. 이 핸들러는 BREACH 공격 방어를
위해 서버가 노출하는(form에 렌더링하거나 `getToken()`으로 꺼내는) 토큰 값을
매번 다른 랜덤 값으로 XOR 마스킹한다. 반면 `CookieCsrfTokenRepository`는 쿠키에
**원본(raw) 토큰**을 그대로 저장한다.

즉, SPA가 쿠키 값(원본)을 그대로 읽어 헤더로 보내면, 서버는 기본적으로
"마스킹된 값"이 와야 한다고 가정하고 검증하므로 항상 불일치 → 403.

이건 Spring Security 5 → 6 마이그레이션 시 "쿠키를 읽어서 헤더로 그대로 보내는"
SPA 패턴이 흔히 깨지는 대표적인 이슈로, 공식 문서에도 별도 가이드가 있음.

## 해결

Spring Security 공식 문서가 제시하는 SPA 전용 패턴을 그대로 적용:

1. **`CookieCsrfTokenRepository.withHttpOnlyFalse()`**
   JS가 읽을 수 있는 `XSRF-TOKEN` 쿠키를 내려줌.

2. **`SpaCsrfTokenRequestHandler`** (`CsrfTokenRequestAttributeHandler` 상속)
   - `handle()`: 기존 `XorCsrfTokenRequestAttributeHandler`에 위임 (폼 렌더링 등
     다른 노출 경로의 BREACH 방어는 유지).
   - `resolveCsrfTokenValue()`: 요청에 `X-XSRF-TOKEN` 헤더가 있으면 마스킹 해제
     없이 **원본 값 그대로** 비교. 헤더가 없는 경우(폼 파라미터 등)에만 기존
     XOR 해제 로직(`super.resolveCsrfTokenValue`)으로 폴백.

3. **`CsrfCookieFilter`** (`OncePerRequestFilter`, `BasicAuthenticationFilter` 뒤에 등록)
   `CsrfToken`은 지연 로딩이라 누군가 `getToken()`을 실제로 호출하기 전까지는
   `CookieCsrfTokenRepository`가 쿠키를 응답에 쓰지 않는다. 서버 렌더링 폼이
   없는 SPA는 이 호출을 트리거할 코드가 없으므로, 매 요청마다 강제로
   `csrfToken.getToken()`을 호출해 쿠키가 항상 내려가도록 함.

4. **`admin-configs.js`**
   `document.cookie`에서 `XSRF-TOKEN` 값을 읽어 `csrfHeader()` 헬퍼로
   `X-XSRF-TOKEN` 헤더를 구성, POST/PUT/DELETE 요청마다 첨부.

## 관련 파일
- `api/src/main/java/com/mason/api/auth/SecurityConfig.java`
- `api/src/main/java/com/mason/api/auth/SpaCsrfTokenRequestHandler.java`
- `api/src/main/java/com/mason/api/auth/CsrfCookieFilter.java`
- `api/src/main/resources/static/js/admin-configs.js`

## 참고
- Spring Security Reference: CSRF — Integrating with a Single Page Application
  (`SpaCsrfTokenRequestHandler` + `CsrfCookieFilter` 패턴은 공식 문서 예제와 동일)