# API map — legacy Flask → new `/api/v1`

The legacy endpoints are a **capability checklist**, not a contract. The new API is
resource-oriented under `/api/v1` (plural nouns, proper verbs, paginated lists, DTOs,
`ProblemDetail` errors). Status column: ✅ implemented · 🚧 planned (follow-up turn).

## iam / auth

| Legacy | New | Status |
|--------|-----|--------|
| `POST /auth/signup` | `POST /api/v1/auth/register` | ✅ |
| `POST /auth/signin` | `POST /api/v1/auth/login` | ✅ |
| `POST /auth/send-otp/{fin_kod}` | `POST /api/v1/auth/otp` | ✅ |
| `POST /auth/validate-otp/{fin_kod}/{otp}` | `POST /api/v1/auth/otp/verify` | ✅ |
| `POST /auth/reset-password` | `POST /api/v1/auth/password/reset` | ✅ |
| `POST /auth/app-user/{fin_kod}` | `POST /api/v1/users/{id}/approval` | ✅ |
| `DELETE /auth/reject-user/{fin_kod}` | `DELETE /api/v1/users/{id}` (reject) | ✅ |
| `GET /auth/app-wait-users` | `GET /api/v1/users/pending` | ✅ |
| `POST /auth/{fin_kod}/update/role/{role}` | `PUT /api/v1/users/{id}/role` | ✅ |

## iam / user

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/users/all` | `GET /api/v1/users` (filter `?status=`) | ✅ |
| `GET /api/profile/{fin_kod}` | `GET /api/v1/users/{id}` | ✅ |
| (self profile) | `GET /api/v1/me` | ✅ |
| `PUT /api/profile/{fin_kod}/edit` | `PUT /api/v1/me` | ✅ |
| `GET /api/profile/image/{fin_kod}` | `GET /api/v1/users/{id}/avatar` | ✅ |
| `POST /api/approve/profile` | `PUT /api/v1/me` (sets `profile_completed`) + avatar upload | ✅ |
| (block / unblock) | `POST /api/v1/users/{id}/block` · `DELETE /api/v1/users/{id}/block` | ✅ |

## institution

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/institutions` | `GET /api/v1/institutions` | 🚧 |
| `GET /api/institution/{code}` | `GET /api/v1/institutions/{id}` | 🚧 |
| `POST /api/create-institution/{name}` | `POST /api/v1/institutions` | 🚧 |
| (delete) | `DELETE /api/v1/institutions/{id}` | 🚧 |

## priority

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/priotets` | `GET /api/v1/priorities` | 🚧 |
| `GET /api/priotet/{code}` | `GET /api/v1/priorities/{id}` | 🚧 |
| `POST /api/create-priotet` | `POST /api/v1/priorities` | 🚧 |
| `POST /api/upd-prioritet` | `PUT /api/v1/priorities/{id}` | 🚧 |
| `DELETE /api/del-prioritet/{code}` | `DELETE /api/v1/priorities/{id}` | 🚧 |

## project

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/projects` | `GET /api/v1/projects` (`?status=&mine=`) | 🚧 |
| `GET /api/projects/submitted` | `GET /api/v1/projects?status=SUBMITTED` | 🚧 |
| `GET /api/project/{project_code}` | `GET /api/v1/projects/{id}` | 🚧 |
| `GET /api/project/{fin_kod}` | `GET /api/v1/projects?mine=true` | 🚧 |
| `GET /api/project-details/{project_code}` | `GET /api/v1/projects/{id}` (expanded DTO) | 🚧 |
| `GET /api/project-owner/{project_code}` | `GET /api/v1/projects/{id}` (owner in DTO) | 🚧 |
| `GET /api/col-project/{fin_kod}` | `GET /api/v1/projects?mine=true` | 🚧 |
| `POST /api/save/project` | `POST /api/v1/projects` | 🚧 |
| `PATCH /api/upd/project` | `PATCH /api/v1/projects/{id}` | 🚧 |
| `DELETE /api/delete/project` | `DELETE /api/v1/projects/{id}` | 🚧 |
| `POST /api/submit-project` | `POST /api/v1/projects/{id}/submit` | 🚧 |
| `POST /api/approve_project` | `POST /api/v1/projects/{id}/approve` (+ `/reject`) | 🚧 |
| `GET /api/project-pdf/{project_code}` | `GET /api/v1/projects/{id}/pdf` | 🚧 |
| `GET /api/project-excel/{project_code}` | `GET /api/v1/projects/{id}/excel` | 🚧 |

## project / members (legacy collaborators)

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/collaborators` | `GET /api/v1/projects/{id}/members` | 🚧 |
| `GET /api/collaborators/{project_code}` | `GET /api/v1/projects/{id}/members?status=APPROVED` | 🚧 |
| `GET /api/app-wait-collaborators/{project_code}` | `GET /api/v1/projects/{id}/members?status=PENDING` | 🚧 |
| `GET /api/project/owner/{project_code}` | `GET /api/v1/projects/{id}/members?role=OWNER` | 🚧 |
| `POST /api/be-collaborator` | `POST /api/v1/projects/{id}/members` | 🚧 |
| `POST /api/app-collaborator/{fin_kod}` | `POST /api/v1/projects/{id}/members/{userId}/approve` | 🚧 |
| `DELETE /api/reject-collaborator/{fin_kod}` | `POST .../members/{userId}/reject` · `DELETE .../members/{userId}` | 🚧 |

## project / activities

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/project-activity/{project_code}` | `GET /api/v1/projects/{id}/activities` | 🚧 |
| `POST /api/project-activity/create` | `POST /api/v1/projects/{id}/activities` | 🚧 |
| `PATCH /api/project-activity/update/{id}` | `PATCH /api/v1/projects/{id}/activities/{activityId}` | 🚧 |
| `DELETE /api/project-activity/delete/{id}` | `DELETE /api/v1/projects/{id}/activities/{activityId}` | 🚧 |
| `DELETE /api/project-activity/{project_code}/{month}` | `DELETE .../activities?month=` | 🚧 |

## budget (smeta) — unified

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/main-smeta/{project_code}` | `GET /api/v1/projects/{id}/budget` (computed totals) | 🚧 |
| `POST /api/create-smeta` | `PUT /api/v1/projects/{id}/budget` | 🚧 |
| `PATCH /api/edit-smeta/{project_code}` | `PUT /api/v1/projects/{id}/budget` | 🚧 |
| `PATCH /api/update-smeta-field/{project_code}` | `PUT /api/v1/projects/{id}/budget` | 🚧 |
| `DELETE /api/delete-smeta/{project_code}` | `DELETE /api/v1/projects/{id}/budget` | 🚧 |
| `GET /api/salary/smeta/{project_code}` · `all-salaries-table` | `GET /api/v1/projects/{id}/budget/salaries` | 🚧 |
| `POST /api/create-salary-table` | `POST .../budget/salaries` | 🚧 |
| `PATCH /api/edit-salary-table/{project_code}` | `PATCH .../budget/salaries/{salaryId}` | 🚧 |
| `DELETE /api/delete-salary/{project_code}` | `DELETE .../budget/salaries/{salaryId}` | 🚧 |
| `GET /api/subject/smeta/{project_code}` (equipment) | `GET .../budget/line-items?category=EQUIPMENT` | 🚧 |
| `GET /api/get-services/{project_code}` | `GET .../budget/line-items?category=SERVICES` | 🚧 |
| `GET /api/get-rent-all-tables/{project_code}` | `GET .../budget/line-items?category=RENT` | 🚧 |
| `GET /api/get-other_exp-all-tables/{project_code}` | `GET .../budget/line-items?category=OTHER` | 🚧 |
| `POST /api/add-subject` · `add-services` · `rent` · `other_exp` | `POST .../budget/line-items` (category in body) | 🚧 |
| `PATCH /api/update-subject` · `update-services` · `edit-rent-table` · `edit-other_exp-table` | `PATCH .../budget/line-items/{itemId}` | 🚧 |
| `DELETE .../subject` · `services` · `rent` · `other_exp` | `DELETE .../budget/line-items/{itemId}` | 🚧 |

> The four legacy line-item groups collapse into the single
> `/api/v1/projects/{id}/budget/line-items` resource backed by `budget_line_items`,
> discriminated by `category` (`EQUIPMENT | SERVICES | RENT | OTHER`).

## expert

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/experts` | `GET /api/v1/experts` | 🚧 |
| `POST /api/create-expert` | `POST /api/v1/experts` | 🚧 |
| `POST /api/set-expert` (after submit) | `POST /api/v1/projects/{id}/expert-assignments` | 🚧 |
| (assessments) | `GET/POST /api/v1/projects/{id}/assessments` | 🚧 |

## report

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/reports/{project_code}/{quarter}/{year}` | `GET /api/v1/projects/{id}/reports?year=&quarter=` | 🚧 |
| `POST /api/reports/save` (`point_1..17`) | `POST /api/v1/projects/{id}/reports` (points list) | 🚧 |

## admin / lock

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/lock-status` | `GET /api/v1/system/lock` | 🚧 |
| `POST /api/lock` | `PUT /api/v1/system/lock` `{ "locked": true }` | 🚧 |
| `POST /api/unlock` | `PUT /api/v1/system/lock` `{ "locked": false }` | 🚧 |

## publicapi

| Legacy | New | Status |
|--------|-----|--------|
| `GET /api/public/projects` | `GET /api/v1/public/projects` | 🚧 |
| `GET /api/public/project/{project_code}` | `GET /api/v1/public/projects/{projectCode}` | 🚧 |
| `GET /api/public/leads-tree` | `GET /api/v1/public/priorities-tree` | 🚧 |
