# Meeting Link Integration - Frontend Implementation Guide

## Overview

Coaches can connect a video conferencing provider (Google Meet or Zoom) to automatically generate meeting links when sessions are booked. Links are included in booking confirmation emails and iCal attachments sent to both coach and client.

## User Flow

```
Coach Settings Page
  └─> "Connect Google Meet" / "Connect Zoom" button
        └─> OAuth popup/redirect to provider
              └─> Callback redirects to /#/sync-success?provider=zoom|gmeet
                    └─> Show success message, refresh settings

Coach Settings Page
  └─> Toggle auto-generate on/off (PATCH /api/latest/meeting)
  └─> "Disconnect" button (DELETE /api/latest/meeting)

Client Books Session
  └─> POST /api/latest/coaches/{username}/schedule
        └─> Backend generates meeting link automatically
        └─> Email with Zoom/Meet link + iCal sent to both parties
```

## API Endpoints

### 1. Get Meeting Settings

```
GET /api/latest/meeting
Role: COACH
```

**Response (200):**
```json
{
  "provider": "ZOOM",
  "email": "coach@example.com",
  "autoGenerate": true,
  "status": "OK"
}
```

**Response when not connected (200):**
```json
{
  "provider": null,
  "email": null,
  "autoGenerate": false,
  "status": null
}
```

| Field          | Type    | Values                          | Description                              |
|----------------|---------|---------------------------------|------------------------------------------|
| `provider`     | string  | `GOOGLE`, `ZOOM`, `null`        | Connected provider                       |
| `email`        | string  | email or `null`                 | Provider account email                   |
| `autoGenerate` | boolean | `true`/`false`                  | Auto-generate links on booking           |
| `status`       | string  | `OK`, `WARN`, `ERROR`, `null`   | Provider health status                   |

**Status meaning:**
- `OK` - Working normally
- `WARN` - Last generation failed (token may need refresh, show warning to coach)
- `ERROR` - Multiple failures (suggest reconnecting provider)

---

### 2. Connect Provider (OAuth)

Open a new browser window/popup to initiate OAuth:

**Google Meet:**
```
GET /login/google-meet
```

**Zoom:**
```
GET /login/zoom
```

**Important:** The user must be authenticated (session cookie must be present). Open as a popup or redirect from the SPA - do NOT use fetch/XHR, as it requires browser redirect.

**On success:** The OAuth callback redirects the browser to:
- Google Meet: `{appUrl}/#/sync-success?provider=gmeet`
- Zoom: `{appUrl}/#/sync-success?provider=zoom`

**Frontend should:**
1. Open OAuth URL in a popup window or redirect
2. Listen for the `/#/sync-success?provider=...` route
3. Close popup (if used) and refresh meeting settings via `GET /api/latest/meeting`
4. Show success message: "Google Meet connected" / "Zoom connected"

**On error (expired session):** Returns 401 HTML response. Frontend should prompt user to log in again.

---

### 3. Update Auto-Generate Setting

```
PATCH /api/latest/meeting
Role: COACH
Content-Type: application/json
```

**Request:**
```json
{
  "autoGenerate": false
}
```

**Response:** `204 No Content`

---

### 4. Disconnect Provider

```
DELETE /api/latest/meeting
Role: COACH
```

**Response:** `204 No Content`

After disconnecting, `GET /api/latest/meeting` returns the default (null/disconnected) state.

---

### 5. Book Session (Client)

```
POST /api/latest/coaches/{coachUsername}/schedule
Role: USER
Content-Type: application/json
```

**Request:**
```json
{
  "time": "2026-04-05T10:00:00+02:00"
}
```

**Response:** `204 No Content`

The meeting link is **not returned in the response**. It is generated server-side and included in the booking confirmation email sent to both coach and client. The email contains:
- Meeting link (clickable)
- iCal attachment with the meeting link in the event location

**Error responses:**

| HTTP | errorCode            | Meaning                                |
|------|----------------------|----------------------------------------|
| 400  | `time.not.available` | Coach has no availability at that time  |
| 400  | `no.units.available` | Client has no remaining session credits |

---

## Frontend Components

### Coach Settings Page

```
┌──────────────────────────────────────────┐
│  Video Conferencing                      │
│                                          │
│  [When not connected:]                   │
│  Connect a provider to auto-generate     │
│  meeting links for your sessions.        │
│                                          │
│  [Connect Google Meet]  [Connect Zoom]   │
│                                          │
│  [When connected:]                       │
│  ✓ Connected to Zoom (coach@email.com)   │
│  [x] Auto-generate meeting links         │
│  [Disconnect]                            │
│                                          │
│  [When status = WARN:]                   │
│  ⚠ Last meeting link generation failed.  │
│    Links will retry on next booking.     │
│                                          │
│  [When status = ERROR:]                  │
│  ✗ Meeting link generation is failing.   │
│    Please reconnect your account.        │
│  [Reconnect]                             │
└──────────────────────────────────────────┘
```

### Sync Success Page (`/#/sync-success`)

Query params: `?provider=zoom` or `?provider=gmeet`

Show a success message and redirect back to coach settings after a few seconds, or provide a "Back to Settings" button.

---

## Implementation Checklist

1. **Coach Settings Page**
   - [ ] Call `GET /api/latest/meeting` on load
   - [ ] Show connect buttons when `provider` is `null`
   - [ ] Show connected state with email, toggle, disconnect when `provider` is set
   - [ ] Show warning/error banner based on `status`
   - [ ] "Connect" button opens `/login/zoom` or `/login/google-meet` in popup
   - [ ] Handle `/#/sync-success?provider=...` route - close popup, refresh settings
   - [ ] Auto-generate toggle calls `PATCH /api/latest/meeting`
   - [ ] Disconnect button calls `DELETE /api/latest/meeting`

2. **Sync Success Route** (`/#/sync-success`)
   - [ ] Read `provider` query param
   - [ ] Show success message
   - [ ] If opened as popup: notify parent window and close
   - [ ] If opened as redirect: navigate back to settings

3. **Session Booking** (no FE changes needed)
   - Meeting links are handled entirely server-side
   - Links appear in emails, not in the booking API response
