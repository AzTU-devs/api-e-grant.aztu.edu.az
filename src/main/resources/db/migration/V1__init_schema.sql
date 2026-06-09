-- =====================================================================================
-- AZTU E-Grant — initial schema
-- Source of truth: docs/DB_ARCHITECTURE.md (§5 enums, §6 tables, §6.3 view).
-- Surrogate BIGINT identity PKs, real FKs with explicit ON DELETE, native enums,
-- DB-computed (GENERATED STORED) line totals, and the authoritative v_budget_totals view.
-- =====================================================================================

-- ---------------------------------------------------------------------------
-- §5  Enumerations
-- ---------------------------------------------------------------------------
CREATE TYPE academic_type     AS ENUM ('TEACHER', 'PHD', 'MASTER');
CREATE TYPE account_status    AS ENUM ('PENDING', 'APPROVED', 'BLOCKED');
CREATE TYPE global_role       AS ENUM ('APPLICANT', 'ADMIN', 'SUPER_ADMIN');
CREATE TYPE project_status    AS ENUM ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED');
CREATE TYPE member_role       AS ENUM ('OWNER', 'COLLABORATOR');
CREATE TYPE member_status     AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE assignment_status AS ENUM ('ASSIGNED', 'ACCEPTED', 'DECLINED', 'COMPLETED');
CREATE TYPE budget_category   AS ENUM ('EQUIPMENT', 'SERVICES', 'RENT', 'OTHER');

-- ---------------------------------------------------------------------------
-- §6.1  Identity & access
-- ---------------------------------------------------------------------------
CREATE TABLE institutions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ
);

CREATE TABLE priorities (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        INTEGER     NOT NULL UNIQUE,
    name        TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ
);

CREATE TABLE users (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fin_kod                 VARCHAR(7)  NOT NULL UNIQUE,
    global_role             global_role   NOT NULL DEFAULT 'APPLICANT',
    academic_type           academic_type,
    -- identity
    name                    TEXT,
    surname                 TEXT,
    father_name             TEXT,
    born_date               DATE,
    born_place              TEXT,
    sex                     VARCHAR(16),
    citizenship             TEXT,
    personal_id_number      VARCHAR(64),
    living_location         TEXT,
    -- contact
    home_phone              VARCHAR(32),
    personal_mobile_number  VARCHAR(32)  UNIQUE,
    personal_email          VARCHAR(255) UNIQUE,
    -- work
    work_place              TEXT,
    department              TEXT,
    duty                    TEXT,
    work_location           TEXT,
    work_phone              VARCHAR(32)  UNIQUE,
    work_email              VARCHAR(255) UNIQUE,
    -- education
    main_education          TEXT,
    additional_education    TEXT,
    -- science
    scientific_degree       TEXT,
    scientific_degree_date  DATE,
    scientific_title        TEXT,
    scientific_title_date   DATE,
    institution_id          BIGINT REFERENCES institutions(id) ON DELETE RESTRICT,
    avatar_url              TEXT,
    profile_completed       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ
);
CREATE INDEX idx_users_institution_id ON users(institution_id);

CREATE TABLE credentials (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    status        account_status NOT NULL DEFAULT 'PENDING',
    otp_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    approved_at   TIMESTAMPTZ,
    blocked_at    TIMESTAMPTZ,
    unblocked_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ
);

CREATE TABLE otp_codes (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code        VARCHAR(16) NOT NULL,
    issued_at   TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);
CREATE INDEX idx_otp_codes_user_expires ON otp_codes(user_id, expires_at);

CREATE TABLE experts (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email                      VARCHAR(255) NOT NULL UNIQUE,
    name                       TEXT NOT NULL,
    surname                    TEXT NOT NULL,
    father_name                TEXT,
    personal_id_serial_number  VARCHAR(64) NOT NULL UNIQUE,
    work_place                 TEXT,
    duty                       TEXT,
    scientific_degree          TEXT,
    phone_number               VARCHAR(32),
    user_id                    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ
);
CREATE INDEX idx_experts_user_id ON experts(user_id);

-- ---------------------------------------------------------------------------
-- §6.2  Projects
-- ---------------------------------------------------------------------------
CREATE TABLE projects (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_code         BIGINT      NOT NULL UNIQUE,
    owner_id             BIGINT      NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    institution_id       BIGINT REFERENCES institutions(id) ON DELETE RESTRICT,
    priority_id          BIGINT REFERENCES priorities(id) ON DELETE RESTRICT,
    -- content
    project_name         TEXT,
    project_purpose      TEXT,
    annotation           TEXT,
    key_words            TEXT,
    scientific_idea      TEXT,
    structure            TEXT,
    team_characterization TEXT,
    monitoring_plan      TEXT,
    assessment_plan      TEXT,
    requirements         TEXT,
    deadline             DATE,
    status               project_status NOT NULL DEFAULT 'DRAFT',
    submitted_at         TIMESTAMPTZ,
    collaborator_limit   INTEGER     NOT NULL DEFAULT 7,
    max_budget_amount    INTEGER     NOT NULL DEFAULT 30000,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ,
    deleted_at           TIMESTAMPTZ
);
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_institution_id ON projects(institution_id);
CREATE INDEX idx_projects_priority_id ON projects(priority_id);
CREATE INDEX idx_projects_status ON projects(status);

CREATE TABLE project_members (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id  BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    role        member_role   NOT NULL,
    status      member_status NOT NULL DEFAULT 'PENDING',
    joined_at   TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    CONSTRAINT uq_project_members_project_user UNIQUE (project_id, user_id)
);
CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);

CREATE TABLE project_activities (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id    BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    month         INTEGER     NOT NULL,
    activity_name TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ
);
CREATE INDEX idx_project_activities_project_month ON project_activities(project_id, month);

CREATE TABLE expert_assignments (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id   BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    expert_id    BIGINT NOT NULL REFERENCES experts(id) ON DELETE RESTRICT,
    status       assignment_status NOT NULL DEFAULT 'ASSIGNED',
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at TIMESTAMPTZ,
    CONSTRAINT uq_expert_assignments_project_expert UNIQUE (project_id, expert_id)
);
CREATE INDEX idx_expert_assignments_project_id ON expert_assignments(project_id);
CREATE INDEX idx_expert_assignments_expert_id ON expert_assignments(expert_id);

CREATE TABLE assessments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id  BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    expert_id   BIGINT      NOT NULL REFERENCES experts(id) ON DELETE RESTRICT,
    score       INTEGER,
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    CONSTRAINT uq_assessments_project_expert UNIQUE (project_id, expert_id)
);
CREATE INDEX idx_assessments_project_id ON assessments(project_id);
CREATE INDEX idx_assessments_expert_id ON assessments(expert_id);

CREATE TABLE quarterly_reports (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id      BIGINT      NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    quarter_number  INTEGER     NOT NULL,
    year            INTEGER     NOT NULL,
    submission_date TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    CONSTRAINT uq_quarterly_reports_project_year_quarter UNIQUE (project_id, year, quarter_number)
);
CREATE INDEX idx_quarterly_reports_project_id ON quarterly_reports(project_id);

CREATE TABLE quarterly_report_items (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    report_id BIGINT  NOT NULL REFERENCES quarterly_reports(id) ON DELETE CASCADE,
    item_no   INTEGER NOT NULL,
    content   TEXT,
    CONSTRAINT uq_quarterly_report_items_report_item UNIQUE (report_id, item_no)
);
CREATE INDEX idx_quarterly_report_items_report_id ON quarterly_report_items(report_id);

-- ---------------------------------------------------------------------------
-- §6.3  Budget (Smeta)
-- ---------------------------------------------------------------------------
CREATE TABLE budgets (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id      BIGINT      NOT NULL UNIQUE REFERENCES projects(id) ON DELETE CASCADE,
    total_fee       INTEGER     NOT NULL DEFAULT 0,
    defense_fund    INTEGER     NOT NULL DEFAULT 0,
    -- cached rollups, refreshed transactionally; v_budget_totals is the authority
    total_salary    INTEGER     NOT NULL DEFAULT 0,
    total_equipment INTEGER     NOT NULL DEFAULT 0,
    total_services  INTEGER     NOT NULL DEFAULT 0,
    total_rent      INTEGER     NOT NULL DEFAULT 0,
    total_other     INTEGER     NOT NULL DEFAULT 0,
    grand_total     INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE TABLE budget_salaries (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    budget_id        BIGINT  NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    member_id        BIGINT  NOT NULL REFERENCES project_members(id) ON DELETE RESTRICT,
    salary_per_month INTEGER NOT NULL,
    months           INTEGER NOT NULL,
    total_amount     INTEGER GENERATED ALWAYS AS (salary_per_month * months) STORED,
    CONSTRAINT uq_budget_salaries_budget_member UNIQUE (budget_id, member_id)
);
CREATE INDEX idx_budget_salaries_budget_id ON budget_salaries(budget_id);
CREATE INDEX idx_budget_salaries_member_id ON budget_salaries(member_id);

CREATE TABLE budget_line_items (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    budget_id       BIGINT      NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    category        budget_category NOT NULL,
    item_name       TEXT        NOT NULL,
    unit_of_measure TEXT,
    unit_price      INTEGER     NOT NULL,
    quantity        INTEGER     NOT NULL,
    duration        INTEGER     NOT NULL DEFAULT 1,
    total_amount    INTEGER GENERATED ALWAYS AS (unit_price * quantity * duration) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);
CREATE INDEX idx_budget_line_items_budget_category ON budget_line_items(budget_id, category);

-- ---------------------------------------------------------------------------
-- §6.3  Authoritative totals view
-- ---------------------------------------------------------------------------
CREATE VIEW v_budget_totals AS
SELECT b.id AS budget_id,
       COALESCE(s.salary, 0)                                   AS total_salary,
       COALESCE(li.equipment, 0)                               AS total_equipment,
       COALESCE(li.services, 0)                                AS total_services,
       COALESCE(li.rent, 0)                                    AS total_rent,
       COALESCE(li.other, 0)                                   AS total_other,
       COALESCE(s.salary, 0) + COALESCE(li.total, 0)
         + b.total_fee + b.defense_fund                        AS grand_total
FROM budgets b
LEFT JOIN (SELECT budget_id, SUM(total_amount) AS salary
           FROM budget_salaries GROUP BY budget_id) s ON s.budget_id = b.id
LEFT JOIN (SELECT budget_id,
             SUM(total_amount)                                          AS total,
             SUM(total_amount) FILTER (WHERE category = 'EQUIPMENT')    AS equipment,
             SUM(total_amount) FILTER (WHERE category = 'SERVICES')     AS services,
             SUM(total_amount) FILTER (WHERE category = 'RENT')         AS rent,
             SUM(total_amount) FILTER (WHERE category = 'OTHER')        AS other
           FROM budget_line_items GROUP BY budget_id) li ON li.budget_id = b.id;

-- ---------------------------------------------------------------------------
-- §9  System lock (global submission gate)
-- ---------------------------------------------------------------------------
CREATE TABLE system_lock (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_locked  BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ
);
-- single canonical row
INSERT INTO system_lock (is_locked) VALUES (FALSE);
