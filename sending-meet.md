# Google Meet Link Integration

## Current State

- Session invitations are sent via **email with .ics attachment** (`ICalService`)
- Google Calendar OAuth is used only for **reading coach availability** (scope `calendar.readonly`)
- **No Google Meet links** are generated or included in invitations
- Email templates (HTML) and iCal templates do not contain any meeting link

## Key Files

| File | Role |
|---|---|
| `common/calendar/ical/ICalService.java` | Generates .ics calendar event attachments |
| `common/calendar/ical/ICalEvent.java` | Wrapper for iCal content |
| `common/email/EmailService.java` | Sends emails with optional .ics attachment |
| `common/email/EmailTemplateService.java` | Orchestrates email sending with templates |
| `common/email/SessionEmailData.java` | DTO for session data passed to email service |
| `common/calendar/google/GoogleCalendarApiClientFactory.java` | Google Calendar API client (OAuth, free/busy) |
| `common/calendar/google/GoogleCalendarService.java` | Google Calendar business logic |
| `common/calendar/google/GoogleCalendarController.java` | OAuth2 flow for Google Calendar |
| `templates/ical/event.ics` | iCal template for 2-participant events |
| `templates/ical/private-event.ics` | iCal template for private events |
| `templates/reservation/reservation-{locale}.html` | Coach booking email (en, cs, de, fr) |
| `templates/reservation/user-reservation-{locale}.html` | Client booking email (en, cs, de, fr) |

## Implementation Options

### Option A: Generate Meet link via Coach's Google Account

**How:** Use coach's existing OAuth token to create a Google Calendar event with `conferenceData`, extract the Meet link.

**Changes needed:**
1. Expand OAuth scope from `calendar.readonly` to `calendar.events` in `GoogleCalendarController`
2. Add `createEventWithMeet()` method to `GoogleCalendarApiClientFactory`
3. Add orchestration in `GoogleCalendarService` — create event, extract Meet link
4. Add `meetLink` field to `SessionEmailData`
5. Pass Meet link to iCal templates (`LOCATION` and `DESCRIPTION` fields)
6. Pass Meet link to HTML email templates
7. Handle fallback when coach doesn't have Google connected

**Pros:** No extra infrastructure needed, uses existing OAuth flow.
**Cons:** Requires coach to have Google connected + Google Meet license. Needs re-authorization for new scope.

### Option B: Generate Meet link via Service Account

**How:** Use a Google Workspace service account with domain-wide delegation to create Meet links.

**Changes needed:**
1. Configure service account with domain-wide delegation
2. New service for Meet link generation independent of coach OAuth
3. Same template changes as Option A

**Pros:** Independent of coach's Google connection.
**Cons:** Requires Google Workspace, domain-wide delegation setup, more complex.

### Option C: Static Meet Room per Coach

**How:** Each coach configures a personal Google Meet room URL in their profile. No API needed.

**Changes needed:**
1. Add `meetLink` field to coach profile (DB migration + entity)
2. Admin/coach UI to set their Meet link
3. Pass link through to email and iCal templates

**Pros:** Simplest, no API integration needed, works immediately.
**Cons:** Coach must manually create and set their Meet link. Same link for all sessions.

## Template Changes (Common to All Options)

### iCal Template (`event.ics`)
Add `LOCATION` and update `DESCRIPTION`:
```
LOCATION:${meetLink}
DESCRIPTION:${eventName}\n\nJoin Google Meet: ${meetLink}
```

### HTML Email Templates
Add Meet link section:
```html
<p>Join via Google Meet: <a href="${meetLink}" target="_blank">${meetLink}</a></p>
```

## Decision Required

Which option to implement? Key question: **Should Meet links be auto-generated via API or manually configured by coaches?**
