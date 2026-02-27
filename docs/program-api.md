# Program Wizard API

Base URL: `/api/latest/hr/programs`
Auth: Requires `HR` or `ADMIN` role.

All endpoints resolve the company from the authenticated user. Programs are scoped to the user's company.

---

## Program Options (Visibility & Privacy)

### GET /api/latest/hr/programs/options

Returns all available program options for the visibility & privacy configuration. Options are divided into `HR` and `MANAGER` categories. Options with `alwaysOn: true` are always active and cannot be toggled off by HR.

**Response:**
```json
[
  { "key": "opt.hr.session-attendance",   "category": "HR",      "alwaysOn": true },
  { "key": "opt.hr.goal-completion",      "category": "HR",      "alwaysOn": true },
  { "key": "opt.hr.micro-action-rate",    "category": "HR",      "alwaysOn": false },
  { "key": "opt.hr.checkpoint-responses", "category": "HR",      "alwaysOn": false },
  { "key": "opt.hr.assessment-results",   "category": "HR",      "alwaysOn": false },
  { "key": "opt.mgr.enrollment-status",   "category": "MANAGER", "alwaysOn": true },
  { "key": "opt.mgr.focus-area-goal",     "category": "MANAGER", "alwaysOn": false },
  { "key": "opt.mgr.session-attendance",  "category": "MANAGER", "alwaysOn": false },
  { "key": "opt.mgr.goal-progress",       "category": "MANAGER", "alwaysOn": false }
]
```

FE usage: filter by `category` to render two sections ("HR Admin sees" / "Manager sees"). Disable checkboxes where `alwaysOn: true`. Store selected non-alwaysOn keys in `enabledOptions` when saving a program draft.

---

## Program List

### GET /api/latest/hr/programs

Returns all programs for the authenticated user's company. Used on the Programs dashboard.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Leadership Development Q1 2026",
    "status": "DRAFT",
    "validFrom": "2026-03-15T00:00:00",
    "validTo": "2026-06-13T00:00:00",
    "milestoneDate": "2026-05-30T00:00:00",
    "daysUntilMilestone": 83,
    "totalParticipants": 4,
    "activeParticipants": 2
  }
]
```

---

## Save / Update Draft (Upsert)

### POST /api/latest/hr/programs

Creates a new program draft or updates an existing one. This single endpoint handles all 4 wizard steps. Each call saves the full program state (all fields).

- If `id` is `null` → creates a new program with a linked `coaching_package`
- If `id` is provided → updates the existing DRAFT program (non-DRAFT programs cannot be updated)

Participants are synced: new ones are added (with `user_allocation`), removed ones are deleted.

**Request:**
```json
{
  "id": null,
  "name": "Leadership Development Q1 2026",
  "goal": "Improve feedback culture & accountability for Team Leads",
  "targetGroup": "Team Leads, Engineering division",
  "durationDays": 90,
  "startDate": "2026-03-15T00:00:00",
  "milestoneDate": "2026-05-30T00:00:00",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "participants": ["user1@company.cz", "user2@company.cz"],
  "sessionsPerParticipant": 5,
  "recommendedCadence": "Every 2-3 weeks",
  "coachAssignmentModel": "PARTICIPANT_CHOOSES",
  "shortlistedCoaches": [],
  "microActionsEnabled": true,
  "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"]
}
```

| Field | Required | Notes |
|---|---|---|
| `id` | No | `null` to create, existing ID to update |
| `name` | Yes | Internal program name (only required field for draft) |
| `goal` | No | Visible to participants as program purpose |
| `targetGroup` | No | Description of target audience |
| `durationDays` | Yes | Program duration in days (default `0`) |
| `startDate` | No | When participants get enrolled |
| `milestoneDate` | No | Key milestone/checkpoint date |
| `focusAreas` | Yes | Set of focus area keys, at least `[]` |
| `participants` | Yes | Set of usernames, at least `[]` |
| `sessionsPerParticipant` | Yes | Number of coaching sessions per person (default `0`) |
| `recommendedCadence` | No | Guidance text shown to participants |
| `coachAssignmentModel` | No | `PARTICIPANT_CHOOSES` (default), `HR_ASSIGNS`, or `HR_SHORTLIST` |
| `shortlistedCoaches` | Yes | Coach usernames, at least `[]` |
| `microActionsEnabled` | Yes | Weekly AI micro-actions (default `true`) |
| `enabledOptions` | Yes | Selected visibility/privacy option keys, at least `[]` |

**Response:** `ProgramDraftDto` — the saved program state.

```json
{
  "id": 1,
  "name": "Leadership Development Q1 2026",
  "goal": "Improve feedback culture & accountability for Team Leads",
  "targetGroup": "Team Leads, Engineering division",
  "status": "DRAFT",
  "durationDays": 90,
  "milestoneDate": "2026-05-30T00:00:00",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "sessionsPerParticipant": 5,
  "recommendedCadence": "Every 2-3 weeks",
  "coachAssignmentModel": "PARTICIPANT_CHOOSES",
  "shortlistedCoaches": [],
  "microActionsEnabled": true,
  "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"]
}
```

---

## Program Detail

### GET /api/latest/hr/programs/{programId}

Returns full program detail with participant list and computed statistics. Used on the program dashboard after launch.

Participant status is computed dynamically based on time elapsed and session consumption:
- `NOT_STARTED` — program hasn't started yet
- `ON_TRACK` — consumed sessions are proportional to elapsed time
- `AT_RISK` — participant is behind expected pace (with 80% grace buffer)

**Response:**
```json
{
  "id": 1,
  "name": "Leadership Development Q1 2026",
  "status": "ACTIVE",
  "validFrom": "2026-03-15T00:00:00",
  "validTo": "2026-06-13T00:00:00",
  "milestoneDate": "2026-05-30T00:00:00",
  "daysUntilMilestone": 83,
  "stats": {
    "totalParticipants": 4,
    "progressPercent": 35
  },
  "participants": [
    {
      "username": "user1@company.cz",
      "firstName": "Tomas",
      "lastName": "Novak",
      "coachUsername": "coach1@topleader.com",
      "lastActiveAt": "2026-04-01T14:30:00",
      "sessionsConsumed": 2,
      "sessionsAllocated": 5,
      "status": "ON_TRACK"
    }
  ]
}
```

---

## Company Users (Participant Selection)

### GET /api/latest/hr/programs/users?programId={id}

Returns all active users in the company with an `added` flag indicating whether they are already participants of the specified program. Used in Step 2 (Participants) to populate the user selection list.

- `programId` is optional — if `null`, all users are returned with `added: false`
- Only users with `USER` authority and non-`CANCELED` status are included

**Response:**
```json
[
  {
    "username": "user1@company.cz",
    "firstName": "Tomas",
    "lastName": "Novak",
    "email": "user1@company.cz",
    "added": true
  },
  {
    "username": "user3@company.cz",
    "firstName": "Petr",
    "lastName": "Svoboda",
    "email": "user3@company.cz",
    "added": false
  }
]
```

---

## AI Coach Recommendations

### POST /api/latest/hr/programs/recommend-coaches

Returns all public coaches with AI-recommended ones highlighted. Used in Step 3 when coach assignment model is `HR_SHORTLIST`. The AI evaluates coaches based on their bio, primary roles, fields, topics, and priority score against the program's goal, focus areas, and target group.

- Coaches with a non-null `reason` are AI-recommended (top 3)
- Results are sorted: recommended first, then alphabetically by last name

**Request:**
```json
{
  "goal": "Improve feedback culture & accountability for Team Leads",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "targetGroup": "Team Leads, Engineering division"
}
```

| Field | Required | Notes |
|---|---|---|
| `goal` | Yes | Program goal for matching |
| `focusAreas` | Yes | Focus areas for matching |
| `targetGroup` | No | Target audience description |

**Response:**
```json
[
  {
    "username": "coach1@topleader.com",
    "firstName": "Dana",
    "lastName": "Brozkova",
    "reason": "Strong expertise in feedback and delegation with 10+ years in leadership coaching."
  },
  {
    "username": "coach2@topleader.com",
    "firstName": "Adam",
    "lastName": "Joseph",
    "reason": "Specializes in team dynamics and accountability frameworks for tech teams."
  },
  {
    "username": "coach3@topleader.com",
    "firstName": "Lucie",
    "lastName": "Novakova",
    "reason": null
  }
]
```

---

## Launch Program

### POST /api/latest/hr/programs/{programId}/launch

Moves a DRAFT program to `CREATED` status. Only DRAFT programs can be launched. Admin later activates the program (`CREATED → ACTIVE`).

Validates before launch:
- `goal` must not be blank
- `coachAssignmentModel` must not be null
- At least one participant must exist

**Response:** `ProgramDraftDto` — the program with status `CREATED`.

**Errors:**
- `program.goal.required` — goal is blank
- `program.coach.model.required` — coach assignment model is null
- `program.participants.required` — no participants added

---

## Program Templates

Base URL: `/api/latest/hr/program-templates`

### GET /api/latest/hr/program-templates

Returns available templates (global + company-specific). Templates prefill the wizard form on the FE — they are not linked to the created program.

**Response:**
```json
[
  {
    "id": 1,
    "name": "template.leadership-90d",
    "description": "template.leadership-90d.description",
    "goal": "template.leadership-90d.goal",
    "targetGroup": null,
    "durationDays": 90,
    "custom": false,
    "focusAreas": ["fa.giving-feedback", "fa.delegation", "fa.strategic-thinking"]
  }
]
```

Template `name`, `description`, and `goal` fields use i18n keys (e.g. `template.leadership-90d`) that the FE translates.

### GET /api/latest/hr/program-templates/{templateId}

Returns a single template by ID.

---

## i18n Keys

All translatable keys are in `misc/program-i18n-keys.json`. Key prefixes:

| Prefix | Usage |
|---|---|
| `fa.*` | Focus areas (e.g. `fa.giving-feedback` → "Giving feedback") |
| `template.*` | Template names, descriptions, goals |
| `opt.hr.*` | HR visibility options |
| `opt.mgr.*` | Manager visibility options |

Custom focus areas (added by users) have no prefix and are stored as plain text.

---

## Data Model

```
program (1:1) ←→ coaching_package
program (1:N) ←→ program_participant (1:1) ←→ user_allocation
program_template — prefills form, no FK to program
program_option — visibility/privacy catalog
focus_area — predefined focus area catalog
```

### Program Status Flow

```
DRAFT → CREATED → ACTIVE → COMPLETED
         (HR)      (Admin)
```

### Coach Assignment Models

| Model | Description |
|---|---|
| `PARTICIPANT_CHOOSES` | Participant picks from full marketplace |
| `HR_ASSIGNS` | HR assigns a coach per participant |
| `HR_SHORTLIST` | HR pre-selects coaches, participant picks from shortlist |
