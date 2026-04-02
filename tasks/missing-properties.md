# Missing Properties in BE DTOs

FE sends these fields but they're silently ignored by Jackson. Consider adding them to the DTOs or cleaning up the FE payloads.

## `allowedCoachRates` in `CreateUserRequestDto`

- **FE**: `fe/src/features/Settings/Admin/api.js` sends `allowedCoachRates` for both create and update
- **BE**: `AdminViewController.CreateUserRequestDto` does not include `allowedCoachRates` — only `UpdateUserRequestDto` has it
- **Impact**: Field ignored on user creation. If admin sets coach rates during create, they're silently lost

## `templateId` in `SaveProgramRequest`

- **FE**: `fe/src/features/HRPrograms/api.js` sends `templateId` (converted: `"custom"` → `null`)
- **BE**: `SaveProgramRequest` DTO has no `templateId` field
- **Impact**: Field ignored, no runtime error

## `programId` in enrollment body

- **FE**: `fe/src/features/HRPrograms/participant/api.js` sends `programId` in the request body (also used in URL path)
- **BE**: `EnrollRequest` only expects `focusArea` and `personalGoal`
- **Impact**: Extra field ignored, no runtime error
