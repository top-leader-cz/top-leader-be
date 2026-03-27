# Programs API - Frontend Integration Guide

Base URL: `/api/latest/hr/programs`
Auth: requires `HR` or `ADMIN` role (session-based)

---

## Endpoints

### 1. GET `/options`

Returns available program visibility/privacy options for Step 3.

**Response:** `ProgramOptionDto[]`

```json
[
  { "key": "opt.hr.session-attendance", "category": "HR", "alwaysOn": true },
  { "key": "opt.hr.micro-action-rate", "category": "HR", "alwaysOn": false },
  { "key": "opt.mgr.enrollment-status", "category": "MANAGER", "alwaysOn": true },
  { "key": "opt.mgr.focus-area-goal", "category": "MANAGER", "alwaysOn": false }
]
```

---

### 2. GET `/`

List all programs for the current user's company.

**Response:** `ProgramSummaryDto[]`

```json
[
  {
    "id": 1,
    "name": "Leadership Development Q1 2026",
    "status": "ACTIVE",
    "validFrom": "2026-03-15T00:00:00",
    "validTo": "2026-06-13T00:00:00",
    "milestoneDate": "2026-06-13T00:00:00",
    "daysUntilMilestone": 83,
    "totalParticipants": 4,
    "activeParticipants": 3
  }
]
```

---

### 3. GET `/{programId}`

Program detail with participants and stats (HR Dashboard).

**Response:** `ProgramDetailDto`

```json
{
  "id": 1,
  "name": "Leadership Development Q1 2026",
  "status": "ACTIVE",
  "validFrom": "2026-03-15T00:00:00",
  "validTo": "2026-06-13T00:00:00",
  "milestoneDate": "2026-06-13T00:00:00",
  "daysUntilMilestone": 83,
  "stats": {
    "totalParticipants": 4,
    "progressPercent": 50
  },
  "participants": [
    {
      "username": "tomas.novak",
      "firstName": "Tomas",
      "lastName": "Novak",
      "coachUsername": "coach1",
      "managerUsername": "petr.motloch",
      "lastActiveAt": "2026-03-22T10:00:00",
      "sessionsConsumed": 3,
      "sessionsAllocated": 5,
      "status": "ON_TRACK"
    }
  ]
}
```

**Participant status values:** `NOT_STARTED` | `ON_TRACK` | `AT_RISK`

Status is auto-calculated from sessions consumed vs expected pace (with 80% grace buffer).

---

### 4. GET `/users?programId={id}`

List company users for participant picker (Step 2). `programId` is optional.

**Response:** `ProgramUserDto[]`

```json
[
  {
    "username": "tomas.novak",
    "firstName": "Tomas",
    "lastName": "Novak",
    "email": "tomas@company.cz",
    "added": true
  }
]
```

`added: true` = user is already a participant in the given program. When `programId` is omitted, `added` is always `false`.

---

### 5. POST `/`

Create or update a draft program (wizard save). Send `id: null` to create, `id: <number>` to update.

**Request:** `SaveProgramRequest`

```json
{
  "id": null,
  "name": "Leadership Development Q1 2026",
  "goal": "Improve feedback culture & accountability",
  "targetGroup": "Team Leads, Engineering",
  "durationDays": 90,
  "cycleLengthDays": 30,
  "startDate": "2026-06-01T00:00:00",
  "milestoneDate": "2026-08-15T00:00:00",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "participants": [
    { "username": "tomas.novak", "managerUsername": "petr.motloch" },
    { "username": "jana.dvorak", "managerUsername": "petr.motloch" },
    { "username": "petr.svoboda", "managerUsername": null }
  ],
  "sessionsPerParticipant": 5,
  "recommendedCadence": "Every 2-3 weeks",
  "coachAssignmentModel": "PARTICIPANT_CHOOSES",
  "shortlistedCoaches": [],
  "microActionsEnabled": true,
  "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"],
  "coachLanguages": ["en", "cs"],
  "coachCategories": ["ec.leadership", "ec.communication"]
}
```

**Field details:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | Long | no | `null` = create, number = update existing draft |
| `name` | String | yes | Cannot be blank |
| `goal` | String | no | Required for launch, not for save |
| `targetGroup` | String | no | |
| `durationDays` | int | yes | Program duration in days |
| `cycleLengthDays` | Integer | no | Cycle length for checkpoint calculation. If null, defaults to durationDays (single cycle) |
| `startDate` | DateTime | no | Used to set coaching package valid_from |
| `milestoneDate` | DateTime | no | |
| `focusAreas` | String[] | yes | Keys from focus_area table (e.g. `fa.giving-feedback`) |
| `participants` | ParticipantAssignment[] | yes | Can be empty for initial save |
| `participants[].username` | String | yes | |
| `participants[].managerUsername` | String | no | null = no manager assigned |
| `sessionsPerParticipant` | int | yes | |
| `recommendedCadence` | String | no | Free text |
| `coachAssignmentModel` | enum | no | `PARTICIPANT_CHOOSES` (default), `HR_ASSIGNS`, `HR_SHORTLIST` |
| `shortlistedCoaches` | String[] | yes | Legacy field, send `[]`. Kept for backward compatibility |
| `microActionsEnabled` | boolean | yes | |
| `enabledOptions` | String[] | yes | Keys from program_option table |
| `coachLanguages` | String[] | yes | **NEW** — Language codes for coach matching (e.g. `["en", "cs"]`). Hard filter — coaches must speak at least one. Send `[]` for no restriction |
| `coachCategories` | String[] | yes | **NEW** — Expertise category keys (e.g. `["ec.leadership"]`). See `GET /coach-categories` for available values. Send `[]` for no restriction |

**Response:** `ProgramDraftDto`

```json
{
  "id": 1,
  "name": "Leadership Development Q1 2026",
  "goal": "Improve feedback culture & accountability",
  "targetGroup": "Team Leads, Engineering",
  "status": "DRAFT",
  "durationDays": 90,
  "cycleLengthDays": 30,
  "checkpoints": [
    { "name": "Enrollment", "day": 0 },
    { "name": "Mid-cycle", "day": 15 },
    { "name": "Cycle review", "day": 30 },
    { "name": "Mid-cycle", "day": 45 },
    { "name": "Cycle review", "day": 60 },
    { "name": "Mid-cycle", "day": 75 },
    { "name": "Final review", "day": 90 }
  ],
  "milestoneDate": "2026-08-15T00:00:00",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "sessionsPerParticipant": 5,
  "recommendedCadence": "Every 2-3 weeks",
  "coachAssignmentModel": "PARTICIPANT_CHOOSES",
  "shortlistedCoaches": [],
  "microActionsEnabled": true,
  "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"],
  "coachLanguages": ["en", "cs"],
  "coachCategories": ["ec.leadership", "ec.communication"]
}
```

**Checkpoints** are auto-calculated server-side from `durationDays` and `cycleLengthDays`. FE should display them read-only in Step 4 (Review).

**Checkpoint logic:**
- Always starts with `Enrollment` (day 0) and ends with `Final review` (day = durationDays)
- Each cycle gets a `Mid-cycle` (day = cycle/2 offset) and `Cycle review` (day = cycle end)
- If `cycleLengthDays` is null or 0, treated as single cycle (= durationDays)
- Example: 90 days, no cycle -> `Enrollment(0), Mid-cycle(45), Final review(90)`
- Example: 90 days, 30-day cycles -> 7 checkpoints as shown above

---

### 6. POST `/{programId}/launch`

Launch a draft program. Validates required fields before launching.

**Response:** `ProgramDraftDto` (same as save, with `status: "CREATED"`)

**Validation errors (HTTP 422):**

| errorCode | Condition |
|-----------|-----------|
| `program.goal.required` | Goal is blank |
| `program.coach.model.required` | Coach assignment model is null |
| `program.participants.required` | No participants added |

---

### 7. GET `/coach-categories` (NEW)

Returns the 8 expertise categories used for coach matching in Step 3. Each category maps to a set of coach profile fields.

**Response:** `CoachCategoryDto[]`

```json
[
  {
    "key": "ec.leadership",
    "name": "Leadership & Management",
    "coachFields": ["leadership", "cross_cultural_leadership", "remote_leadership", "leading_without_authority", "executive", "decision_making", "delegation", "strategic_thinking", "teams", "management"]
  },
  {
    "key": "ec.communication",
    "name": "Communication & Influence",
    "coachFields": ["communication", "feedback_culture", "negotiations", "influencing_skills", "conflict", "coaching_skills_for_managers"]
  },
  {
    "key": "ec.resilience",
    "name": "Resilience & Wellbeing",
    "coachFields": ["resilience", "stress_management", "mental_fitness", "wellbeing", "fitness", "health", "psychological_safety"]
  },
  {
    "key": "ec.self-development",
    "name": "Self-Development",
    "coachFields": ["self_leadership", "self_criticism", "confidence", "imposter_syndrome", "emotional_intelligence", "career", "change"]
  },
  {
    "key": "ec.performance",
    "name": "Performance & Business",
    "coachFields": ["performance", "sales", "marketing", "business", "entrepreneurship", "management", "time_management", "finance"]
  },
  {
    "key": "ec.innovation",
    "name": "Innovation & Transformation",
    "coachFields": ["innovation_and_creativity", "transformations", "ai_adoption_for_leaders", "ai_governance_and_ethics", "organizational_development"]
  },
  {
    "key": "ec.diversity",
    "name": "Diversity & Culture",
    "coachFields": ["diversity", "cultural_differences", "women", "relationships"]
  },
  {
    "key": "ec.mentoring",
    "name": "Mentoring & Facilitation",
    "coachFields": ["mentorship", "facilitation", "life"]
  }
]
```

**Usage in Step 3:** Display categories as selectable chips. HR picks which categories are relevant for the program. Selected category keys are saved in `coachCategories`.

---

### 8. POST `/coach-preview` (NEW)

Returns an optimally-sized pool of matching coaches. Hard filters (language + category) find exact matches, then AI fills or trims to reach the target pool size.

**Pool size formula:** `clamp(8, participantCount × 2, 15)` — minimum 8, maximum 15, ideally 2× participants.

**Request:** `CoachMatchRequest`

```json
{
  "coachLanguages": ["en", "cs"],
  "coachCategories": ["ec.leadership", "ec.communication"],
  "participantCount": 4,
  "goal": "Improve feedback culture & accountability",
  "focusAreas": ["fa.giving-feedback", "fa.delegation"],
  "targetGroup": "Team Leads"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `coachLanguages` | String[] | yes | Language codes. **Hard filter (show-stopper)** — coach must speak at least one. AI can NEVER override this. `[]` = no restriction |
| `coachCategories` | String[] | yes | Category keys from `/coach-categories`. **Hard filter** — coach must have at least one matching field. `[]` = no restriction |
| `participantCount` | int | yes | Number of participants — used to calculate target pool size |
| `goal` | String | yes | Program goal — used by AI for ranking (not filtering). Cannot be blank |
| `focusAreas` | String[] | yes | Focus area keys — used by AI for ranking. Can be `[]` |
| `targetGroup` | String | no | Target group — used by AI for ranking |

**Response:** `CoachPreviewResponse`

```json
{
  "total": 10,
  "exact": 7,
  "recommended": 3,
  "summary": "10 experts — 7 exact + 3 recommended",
  "coaches": [
    {
      "username": "coach1",
      "firstName": "Anna",
      "lastName": "Smith",
      "bio": "Experienced leadership coach with 15+ years...",
      "languages": ["en", "cs"],
      "fields": ["leadership", "communication", "teams"],
      "reason": "Specializes in feedback culture and leadership development, ideal for team leads.",
      "matchType": "exact"
    },
    {
      "username": "coach5",
      "firstName": "Eva",
      "lastName": "Brown",
      "bio": "Career development specialist...",
      "languages": ["en"],
      "fields": ["career", "confidence", "change"],
      "reason": "Strong background in personal development coaching, relevant for leadership growth.",
      "matchType": "recommended"
    }
  ]
}
```

**Response fields:**

| Field | Description |
|-------|-------------|
| `total` | Total coaches in the pool |
| `exact` | Coaches matching both language + category hard filters |
| `recommended` | AI-suggested coaches (language-matched only, no category match) added to fill target |
| `summary` | Human-readable string for wizard display (e.g. "12 best-matched experts" or "8 experts — 3 exact + 5 recommended") |
| `coaches[].matchType` | `"exact"` = passed both hard filters, `"recommended"` = AI-suggested from language-only pool |
| `coaches[].reason` | AI explanation why this coach fits. `null` if AI didn't specifically recommend |

**Matching logic:**
1. **Hard filter (language):** Coach must speak at least one of `coachLanguages`. **Show-stopper** — AI can NEVER add a coach without matching language
2. **Hard filter (category):** Selected categories are resolved to coach field values. Coach must have at least one matching field → these are "exact" matches
3. **Pool sizing:** Target = `clamp(8, participantCount × 2, 15)`
   - If exact < target: AI suggests additional coaches from the language-only pool (skipping category filter) to fill up to target. Only coaches that AI confirms as relevant (gives a `reason`) are added as "recommended"
   - If exact > target: AI trims by keeping the most relevant exact matches up to target
4. **Sorting:** Coaches with `reason` (AI-ranked) first, then alphabetically by `lastName`

**FE usage:**
- Show `summary` in the wizard (e.g. "10 experts — 7 exact + 3 recommended")
- "Preview profiles" opens the list showing coach cards with `matchType` badge
- HR sees this for reference only — HR does not select individual coaches

---

### 9. GET `/focus-area-mappings` (NEW)

Returns the default mapping from focus areas to coach categories. Used for auto-prefilling `coachCategories` when HR selects focus areas in Step 1.

**Response:** `Map<String, String>`

```json
{
  "fa.giving-feedback": "ec.communication",
  "fa.delegation": "ec.leadership",
  "fa.self-awareness": "ec.self-development",
  "fa.strategic-thinking": "ec.leadership",
  "fa.team-motivation": "ec.leadership",
  "fa.communication": "ec.communication",
  "fa.conflict-resolution": "ec.communication",
  "fa.time-management": "ec.performance",
  "fa.emotional-intelligence": "ec.self-development",
  "fa.decision-making": "ec.leadership"
}
```

**FE usage:** When HR selects focus areas in Step 1, auto-select the corresponding coach categories in Step 3. HR can still add/remove categories manually.

---

### 10. POST `/recommend-coaches` (DEPRECATED)

Replaced by `POST /coach-preview` which combines hard filtering with AI ranking.

Still available for backward compatibility.

**Request:**

```json
{
  "goal": "Improve leadership",
  "focusAreas": ["fa.giving-feedback"],
  "targetGroup": "Team Leads"
}
```

**Response:** `RecommendedCoachDto[]`

```json
[
  {
    "username": "coach1",
    "firstName": "Anna",
    "lastName": "Smith",
    "reason": "Specializes in feedback and leadership development..."
  }
]
```

---

## Validation Errors

All validation errors return HTTP 422 with body:

```json
[
  {
    "errorCode": "program.participant.already.in.program",
    "fields": ["participants"],
    "errorMessage": "Participants already in an active program: [tomas.novak]"
  }
]
```

| errorCode | When |
|-----------|------|
| `program.participant.already.in.program` | Participant is already in a CREATED or ACTIVE program |
| `program.participants.not.found` | Username doesn't exist in the system |
| `program.goal.required` | Launch without goal |
| `program.coach.model.required` | Launch without coach assignment model |
| `program.participants.required` | Launch without participants |

---

## Enums

**CoachAssignmentModel:** `PARTICIPANT_CHOOSES` | `HR_ASSIGNS` | `HR_SHORTLIST`

**Program status:** `DRAFT` | `CREATED` | `ACTIVE` | `COMPLETED`

**Participant status:** `NOT_STARTED` | `ON_TRACK` | `AT_RISK`

---

## Step 3 Integration Guide (Coach Matching)

### Flow

1. **Load categories:** `GET /coach-categories` → render as selectable chips
2. **Auto-prefill from Step 1:** Use `GET /focus-area-mappings` to pre-select categories based on chosen focus areas
3. **Language selection:** Show language chips/dropdown. Default from company locale
4. **Live match preview:** Call `POST /coach-preview` with current criteria + `participantCount` → show `response.summary` (e.g. "10 experts — 7 exact + 3 recommended")
5. **Preview profiles:** On click, show coach cards from `response.coaches` with `matchType` badge ("exact" vs "recommended")
6. **Save:** Include `coachLanguages` and `coachCategories` in the `POST /` save request

### Pool size

Target = `clamp(8, participantCount × 2, 15)`:
- 4 participants → target 8
- 6 participants → target 12
- 10 participants → target 15 (capped)

### Important rules

- **Language is a show-stopper** — never show coaches that don't match the language filter, AI cannot override
- **exact** = matched both language + category hard filters
- **recommended** = AI-suggested from language-only pool when exact count is below target
- HR sees count + optional preview, never selects individual coaches
- Participant sees pre-filtered list on Coaches page (existing `POST /api/latest/coaches` with `languages` + `fields` filter params derived from program criteria)

---

## Translation Keys

FE needs translations for the following keys returned by the API.

### Expertise Category keys (`GET /coach-categories`)

| Key | Default EN name |
|-----|----------------|
| `ec.leadership` | Leadership & Management |
| `ec.communication` | Communication & Influence |
| `ec.resilience` | Resilience & Wellbeing |
| `ec.self-development` | Self-Development |
| `ec.performance` | Performance & Business |
| `ec.innovation` | Innovation & Transformation |
| `ec.diversity` | Diversity & Culture |
| `ec.mentoring` | Mentoring & Facilitation |

### Focus Area keys (used in `focusAreas` field)

| Key | Default EN name |
|-----|----------------|
| `fa.giving-feedback` | Giving feedback |
| `fa.delegation` | Delegation |
| `fa.self-awareness` | Self-awareness |
| `fa.strategic-thinking` | Strategic thinking |
| `fa.team-motivation` | Team motivation |
| `fa.communication` | Communication |
| `fa.conflict-resolution` | Conflict resolution |
| `fa.time-management` | Time management |
| `fa.emotional-intelligence` | Emotional intelligence |
| `fa.decision-making` | Decision making |

### Coach fields (inside `coachFields` arrays and coach profile `fields`)

| Key | Default EN name |
|-----|----------------|
| `leadership` | Leadership |
| `cross_cultural_leadership` | Cross-Cultural Leadership |
| `remote_leadership` | Remote Leadership |
| `leading_without_authority` | Leading Without Authority |
| `executive` | Executive |
| `decision_making` | Decision Making |
| `delegation` | Delegation |
| `strategic_thinking` | Strategic Thinking |
| `teams` | Teams |
| `management` | Management |
| `communication` | Communication |
| `feedback_culture` | Feedback Culture |
| `negotiations` | Negotiations |
| `influencing_skills` | Influencing Skills |
| `conflict` | Conflict |
| `coaching_skills_for_managers` | Coaching Skills for Managers |
| `resilience` | Resilience |
| `stress_management` | Stress Management |
| `mental_fitness` | Mental Fitness |
| `wellbeing` | Wellbeing |
| `fitness` | Fitness |
| `health` | Health |
| `psychological_safety` | Psychological Safety |
| `self_leadership` | Self-Leadership |
| `self_criticism` | Self-Criticism |
| `confidence` | Confidence |
| `imposter_syndrome` | Imposter Syndrome |
| `emotional_intelligence` | Emotional Intelligence |
| `career` | Career |
| `change` | Change |
| `performance` | Performance |
| `sales` | Sales |
| `marketing` | Marketing |
| `business` | Business |
| `entrepreneurship` | Entrepreneurship |
| `time_management` | Time Management |
| `finance` | Finance |
| `innovation_and_creativity` | Innovation & Creativity |
| `transformations` | Transformations |
| `ai_adoption_for_leaders` | AI Adoption for Leaders |
| `ai_governance_and_ethics` | AI Governance & Ethics |
| `organizational_development` | Organizational Development |
| `diversity` | Diversity |
| `cultural_differences` | Cultural Differences |
| `women` | Women |
| `relationships` | Relationships |
| `mentorship` | Mentorship |
| `facilitation` | Facilitation |
| `life` | Life |

### Program status

| Key | Default EN |
|-----|-----------|
| `DRAFT` | Draft |
| `CREATED` | Created |
| `ACTIVE` | Active |
| `COMPLETED` | Completed |

### Participant status

| Key | Default EN |
|-----|-----------|
| `NOT_STARTED` | Not Started |
| `ON_TRACK` | On Track |
| `AT_RISK` | At Risk |

### Coach assignment model

| Key | Default EN |
|-----|-----------|
| `PARTICIPANT_CHOOSES` | Participant chooses |
| `HR_ASSIGNS` | HR assigns |
| `HR_SHORTLIST` | HR shortlist |

### Match type (`matchType` in coach-preview response)

| Key | Default EN |
|-----|-----------|
| `exact` | Exact match |
| `recommended` | AI recommended |

---

## cURL Examples

All examples assume session cookie from login. Replace `localhost:8080` with actual host.

### List coach categories
```bash
curl -s -b cookies.txt http://localhost:8080/api/latest/hr/programs/coach-categories | jq
```

### Get focus area → category mappings
```bash
curl -s -b cookies.txt http://localhost:8080/api/latest/hr/programs/focus-area-mappings | jq
```

### Coach preview (match + AI ranking)
```bash
curl -s -b cookies.txt -X POST http://localhost:8080/api/latest/hr/programs/coach-preview \
  -H 'Content-Type: application/json' \
  -d '{
    "coachLanguages": ["en", "cs"],
    "coachCategories": ["ec.leadership", "ec.communication"],
    "participantCount": 4,
    "goal": "Improve feedback culture & accountability",
    "focusAreas": ["fa.giving-feedback", "fa.delegation"],
    "targetGroup": "Team Leads"
  }' | jq
```

### Create draft with coach criteria
```bash
curl -s -b cookies.txt -X POST http://localhost:8080/api/latest/hr/programs \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Leadership Q1 2026",
    "goal": "Improve feedback culture",
    "targetGroup": "Team Leads",
    "durationDays": 90,
    "cycleLengthDays": 30,
    "startDate": "2026-06-01T00:00:00",
    "focusAreas": ["fa.giving-feedback", "fa.delegation"],
    "participants": [],
    "sessionsPerParticipant": 5,
    "shortlistedCoaches": [],
    "microActionsEnabled": true,
    "enabledOptions": [],
    "coachLanguages": ["en", "cs"],
    "coachCategories": ["ec.leadership"]
  }' | jq
```

### List programs
```bash
curl -s -b cookies.txt http://localhost:8080/api/latest/hr/programs | jq
```

### Program detail
```bash
curl -s -b cookies.txt http://localhost:8080/api/latest/hr/programs/1 | jq
```

### Launch program
```bash
curl -s -b cookies.txt -X POST http://localhost:8080/api/latest/hr/programs/1/launch | jq
```

### List company users for participant picker
```bash
curl -s -b cookies.txt "http://localhost:8080/api/latest/hr/programs/users?programId=1" | jq
```

### Get visibility/privacy options
```bash
curl -s -b cookies.txt http://localhost:8080/api/latest/hr/programs/options | jq
```
