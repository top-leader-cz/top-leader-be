# Security Audit — Meeting Link Feature (TOP-394)

Audited commit: `9f3b22c3` (TOP-394 meeting link)

## Findings & Status

### [HIGH] OAuth Tokens Stored in Plaintext — FIXED
- **Fix commit:** `bc78a029` — AES-256-GCM encryption via `TokenEncryptor`
- **Key:** stored in GCP Secret Manager (`token-encryption-key`), injected via Cloud Run env var
- **Graceful fallback:** plaintext tokens auto-re-encrypt on next OAuth refresh
- **Files:** `common/util/crypto/TokenEncryptor.java`, `MeetingService.java`

### [HIGH] OAuth Callback Endpoints Permit Unauthenticated Access — FIXED
- Removed `/login/google-meet`, `/login/zoom`, `/login/google`, `/login/calendly` from `permitAll()`
- OAuth callbacks work because browser redirect carries session cookie
- `CalendlyController` — added `@AuthenticationPrincipal` + username mismatch validation (IDOR fix)
- **Test:** `OAuthSecurityIT` — 11 tests covering auth requirements, redirects, state validation, username mismatch

### [MEDIUM] XSS via Template Variable Injection in OAuth Redirect Page — TODO
- `templates/oauth/redirect.html` uses raw `${redirectUrl}` in JS
- Currently safe (appUrl from config), but pattern is dangerous
- **Fix:** use server-side 302 redirect or JS-encode the URL

### [MEDIUM] Missing `@Secured` on OAuth Controllers — PARTIAL
- `MeetingController` has `@Secured({"COACH"})` correctly
- `GoogleMeetController` / `ZoomController` — no role check (any authenticated user can connect)
- **Decision needed:** should only coaches connect meeting providers?

### [MEDIUM] Zoom Meeting Created Without Waiting Room — BY DESIGN
- `ZoomApiClient.java` — `join_before_host: true`, `waiting_room: false`
- Intentional: allows client to join before coach

### [LOW] OAuth State Not Bound to User Identity — TODO
- State is random UUID in session, not tied to username
- Low risk (Spring Security session fixation protection mitigates)

### [LOW] Tokens May Appear in Logs via Exception Stack Traces — TODO
- `MeetingService.java:77` logs full exception from RestClient calls
- **Fix:** custom error handler on RestClient to strip sensitive data

### [LOW] Hardcoded Google Client Secret Placeholder — TODO
- `application.yml:99` — `client-secret: client-id` (placeholder)
- Zoom uses proper `${ENV_VAR}` pattern, Google doesn't

## Code Review Findings (commit bc78a029)

### [MEDIUM] Missing charset in TokenEncryptor — TODO
- `getBytes()` / `new String()` should use `StandardCharsets.UTF_8`

### [MEDIUM] Missing key length validation — TODO
- Invalid Base64 key accepted at startup, fails at runtime

### [MEDIUM] `persistTokens` skips accessToken when refreshToken is null — pre-existing
