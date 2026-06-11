# Open questions

Behaviours not fully resolvable from the Flask code; proceeding with the noted
assumption. Revisit with stakeholders.

1. **Avatar upload flow.** Legacy `POST /api/approve/profile` took a multipart image and
   set `profile_completed=1` in one shot. New design splits this: `PUT /api/v1/me`
   completes the profile; avatar is uploaded/streamed separately via
   `/api/v1/users/{id}/avatar`. **Assumption:** profile may be marked complete once all
   required text fields are present; avatar is recommended but not strictly required.

2. **System-lock scope.** Legacy lock endpoints existed but were not wired into any
   write path. **Assumption (per prompt business rules):** when locked, block project
   **submission**; other writes remain allowed unless a concrete legacy block is found.

3. **`profile_completed` required-field set.** Legacy required ~21 fields. New schema
   renames some (`additonal_education`→`additional_education`,
   `scientific_name`→`scientific_title`). **Assumption:** require the renamed equivalents;
   exact mandatory list is configurable in the iam profile-completion service.

4. **Password reset token.** Legacy issued a 1-hour OTP token after OTP validation, then
   reset password by decoding it. **Assumption:** keep the same two-step
   (verify OTP → receive short-lived reset token → reset) flow.

5. **`total_fee` / `defense_fund` semantics.** Treated as policy integer values added to
   `grand_total` per `v_budget_totals`. Editable via `PUT .../budget`. No legacy
   validation rules found beyond the 30000 cap.

6. **Quarterly report points.** Schema offers `quarterly_report_items` rows
   (`item_no` 1..17, `content`) vs a `jsonb` alternative. **Decision:** use the
   normalized `quarterly_report_items` rows (matches doc's primary recommendation).

7. **Expert login.** `experts.user_id` is nullable (expert may also be a platform user).
   No legacy expert-login flow exists. **Assumption:** experts are admin-managed records;
   self-login for experts is out of scope unless requested.

## Technical notes

- **Native enums + `ddl-auto=validate`.** Per the schema doc, status columns use native
  Postgres enum types and JPA maps them with `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` +
  `columnDefinition` matching the type name (Hibernate 6.6 / Boot 3.5). This is the
  faithful, modern mapping and validates against the migrated schema. If a given
  Hibernate version rejects native-enum validation, the fallback is to switch
  `spring.jpa.hibernate.ddl-auto` to `none` (Flyway remains authoritative) — no schema
  change needed.
- **`Instant` ↔ `timestamptz`.** Audit/timestamp columns are `timestamptz`; entities use
  `Instant` with `hibernate.jdbc.time_zone=UTC`. This is the recommended pairing.
- **Build verified.** `./mvnw verify` is green on JDK 21 + Docker (Testcontainers Postgres
  16): unit + integration (full critical-path flow) + Spring Modulith boundary check. The
  native-enum and generated-column mappings and the `v_budget_totals` view all validate
  against real Postgres. See README "Test" for the macOS/Colima setup.
- **Acyclic modules via inversion.** `project` owns submission and exposes a
  `ProjectSubmissionGuard` SPI; `admin` (system lock) and `budget` (grand-total cap)
  implement it. So `admin`/`budget` depend on `project`, never the reverse — no cycle.
- **PDF font.** The PDF export embeds `/fonts/NotoSans-Regular.ttf` if present
  (`src/main/resources/fonts/`); the binary isn't committed. Add it for correct
  Azerbaijani glyphs — the export degrades gracefully (default fonts) without it.
