# E-Grant — Requirements (capabilities + business rules)

Derived from the legacy Flask app (`AZTU-E-QRANT-BACK`, behavioural source of truth) and
the target schema in `docs/DB_ARCHITECTURE.md` (schema source of truth). This is a
from-zero rebuild: the capabilities below must all be reachable in the new `/api/v1` API,
but the API shape is redesigned (see `API_MAP.md`).

## Roles

- **Legacy:** numeric `user_type` (0 teacher / 1 phd / 2 master) and `project_role`
  (0 owner / 1 collaborator / 2 super-admin), stored on `auth`.
- **New:** `users.global_role` enum (`APPLICANT | ADMIN | SUPER_ADMIN`) for system-wide
  authorization, plus project-scoped `project_members.role` (`OWNER | COLLABORATOR`).
  `academic_type` (`TEACHER | PHD | MASTER`) becomes a profile attribute, not a role.

## IAM / Auth

- **Register**: create user + credential (`PENDING`), `profile_completed=false`.
- **Login**: verify password (BCrypt); **blocked when account is `BLOCKED` or not
  `APPROVED`**. Returns JWT with claims `sub` (user id), `fin_kod`, `profile_completed`,
  `role` (single consistent claim — fixes the legacy `role`/`role_code` mismatch).
- **OTP**: numeric 6-digit code, `issued_at`/`expires_at` (5-min TTL), `consumed_at`;
  emailed via notification module; verify marks consumed and issues an OTP token.
- **Password reset**: OTP-token based.
- **Admin approval queue**: list `PENDING` users; approve (→ `APPROVED`, email sent) or
  reject (delete). Block/unblock. Set `global_role`.
- **Profile**: get/edit own profile; `profile_completed` becomes `true` only when all
  required profile fields are present (mirrors legacy `POST /api/approve/profile`).
- **Avatar**: stored as a file reference (`users.avatar_url`) via `FileStorage`, not a DB
  blob; streamed on read.

## Institutions & Priorities (lookups)

- CRUD on institutions (`code`, `name`) and priorities (`code` int, `name`). Public read,
  admin write. Replaces legacy string `institution_code` / `priotet` with FKs.

## Projects

- Create/update/delete projects; **multiple projects per owner allowed** (legacy
  `project.fin_kod UNIQUE` defect dropped → `owner_id` FK, not unique).
- `project_code`: random unique 8-digit business key (kept, `UNIQUE`).
- Status lifecycle `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED` replaces legacy
  `approved`(int) + `submitted`(bool).
- **Submit** guarded by: system lock off, `profile_completed`, and budget cap
  (`grand_total ≤ max_budget_amount`, default 30000) → **409** on violation. Sets
  `submitted_at`. Publishes `ProjectSubmitted`.
- Approve/reject (admin).
- PDF + Excel export of project + smeta.

## Team (project_members)

- Request to join as collaborator (requires `profile_completed`); owner/admin
  approve/reject. **`UNIQUE(project_id, user_id)`** (fixes the global-unique `fin_kod`
  bug — a person may join many projects, a project may have many collaborators).
- **Collaborator limit** per project (`collaborator_limit`, default 7) → **409** when
  exceeded.
- Email on approval/rejection.

## Activities

- Monthly activity plan per project (`month` 1–12, `activity_name`). CRUD.

## Budget (Smeta) — 1:1 with project

- `budgets`: `total_fee`, `defense_fund` (policy values) + cached rollups.
- `budget_salaries`: per team member, `salary_per_month * months` → `total_amount`
  (DB-**generated**). `UNIQUE(budget_id, member_id)`.
- `budget_line_items`: **unified** table for equipment/services/rent/other discriminated
  by `category` enum; `unit_price * quantity * duration` → `total_amount` (DB-generated).
  Replaces the four legacy near-identical tables.
- **Totals come from `v_budget_totals` view** (authoritative); never client-supplied.
  Cached header columns refreshed transactionally and verifiable against the view.
  Fixes the legacy manual-sum drift defect.

## Experts & review

- CRUD experts (FK-backed, replaces email-string linkage).
- **Expert assignment only after project is `SUBMITTED`** → 409 otherwise.
- Assessments (`score`, `note`) FK-linked to `experts` and `projects`.
- `expert` consumes the `ProjectSubmitted` event.

## Reports

- Quarterly reports per `(project_id, year, quarter_number)` (unique). Body carries the
  17 points as a list → `quarterly_report_items` rows (replaces `point_1..17`).

## System lock (admin)

- Global `system_lock.is_locked`. When locked, project submission (and other locked
  writes) are blocked. Get/set lock status.

## Public (unauthenticated)

- List/get approved projects (sanitized: no contact/personal/budget data), and a
  priorities-tree (legacy "leads-tree").

## Cross-cutting

- Stateless JWT security; `@PreAuthorize`; public + auth endpoints unauthenticated.
- RFC 7807 `ProblemDetail` errors; 400/403/404/409 mapped to domain exceptions.
- Rate limiting (Bucket4j): 200/day + 50/hour per IP (configurable).
- CORS configurable per profile (permissive in dev).
- Async, event-driven transactional email (Thymeleaf templates).
- 12-factor config via env; no committed secrets.

## Defects fixed (not reproduced)

1. JWT `role` vs `role_code` mismatch → single `role` claim.
2. `collaborators.fin_kod UNIQUE` → `project_members UNIQUE(project_id, user_id)`.
3. Double PKs on salary/subject/services → single surrogate PK.
4. Manual smeta totals drift → DB-generated line totals + `v_budget_totals`.
5. Expert/assessment by raw email → FK to `experts`.
6. Inconsistent `fin_kod`/`project_code` types → consistent per schema doc.
