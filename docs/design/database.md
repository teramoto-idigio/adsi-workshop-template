# DB設計

## RDBMS

PostgreSQL 15+

## ER図

```
┌──────────────┐       ┌──────────────────────┐
│ departments  │       │ employees            │
├──────────────┤       ├──────────────────────┤
│ id (PK)      │──┐    │ id (PK)              │
│ name (UQ)    │  └───<│ department_id (FK)    │
│ version      │       │ name                 │
│ created_at   │       │ email (UQ)           │
│ updated_at   │       │ password_hash        │
└──────────────┘       │ role                 │
                       │ active               │
                       │ version              │
                       │ created_at           │
                       │ updated_at           │
                       └──────────┬───────────┘
                                  │
            ┌─────────────────────┼─────────────────────┐
            │                     │                     │
            ▼                     ▼                     ▼
┌───────────────────┐ ┌───────────────────┐ ┌───────────────────────┐
│attendance_records │ │ leave_requests    │ │ overtime_requests     │
├───────────────────┤ ├───────────────────┤ ├───────────────────────┤
│ id (PK)           │ │ id (PK)           │ │ id (PK)               │
│ employee_id (FK)  │ │ employee_id (FK)  │ │ employee_id (FK)      │
│ date              │ │ date              │ │ date                  │
│ clock_in          │ │ leave_type        │ │ request_type          │
│ clock_out         │ │ status            │ │ overtime_minutes      │
│ break_minutes     │ │ rejection_reason  │ │ reason                │
│ note              │ │ approver_id (FK)  │ │ status                │
│ version           │ │ approved_at       │ │ rejection_reason      │
│ created_at        │ │ version           │ │ approver_id (FK)      │
│ updated_at        │ │ created_at        │ │ approved_at           │
└───────────────────┘ │ updated_at        │ │ version               │
                      └───────────────────┘ │ created_at            │
                                            │ updated_at            │
            ┌───────────────────┐           └───────────────────────┘
            │ leave_balances    │
            ├───────────────────┤
            │ id (PK)           │
            │ employee_id (FK)  │
            │ fiscal_year       │
            │ total_days        │
            │ used_days         │
            │ version           │
            │ created_at        │
            │ updated_at        │
            └───────────────────┘
```

---

## テーブル定義

### departments

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| name | VARCHAR(100) | NOT NULL, UNIQUE | 部署名 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

### employees

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| department_id | BIGINT | FK → departments(id), NOT NULL | 所属部署 |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | メールアドレス |
| password_hash | VARCHAR(255) | NOT NULL | BCryptハッシュ |
| role | VARCHAR(20) | NOT NULL | EMPLOYEE/MANAGER/ADMIN |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 有効フラグ |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

### attendance_records

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| date | DATE | NOT NULL | 勤務日 |
| clock_in | TIME | NOT NULL | 出勤時刻 |
| clock_out | TIME | NULL | 退勤時刻 |
| break_minutes | INTEGER | NOT NULL, DEFAULT 0 | 休憩（分） |
| note | VARCHAR(500) | NULL | 備考 |
| version | BIGINT | NOT NULL, DEFAULT 0 | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

**制約:** UNIQUE(employee_id, date)

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 申請者 |
| date | DATE | NOT NULL | 休暇日 |
| leave_type | VARCHAR(20) | NOT NULL | FULL_DAY/AM_HALF/PM_HALF |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| rejection_reason | VARCHAR(500) | NULL | 却下理由 |
| approver_id | BIGINT | FK → employees(id), NULL | 承認者 |
| approved_at | TIMESTAMP | NULL | 承認日時 |
| version | BIGINT | NOT NULL, DEFAULT 0 | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

### leave_balances

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| fiscal_year | INTEGER | NOT NULL | 年度 |
| total_days | DECIMAL(4,1) | NOT NULL | 付与日数 |
| used_days | DECIMAL(4,1) | NOT NULL, DEFAULT 0 | 消化日数 |
| version | BIGINT | NOT NULL, DEFAULT 0 | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

**制約:** UNIQUE(employee_id, fiscal_year)

### overtime_requests

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 申請者 |
| date | DATE | NOT NULL | 残業日 |
| request_type | VARCHAR(20) | NOT NULL | PRE_APPROVAL/POST_APPROVAL |
| overtime_minutes | INTEGER | NOT NULL | 残業時間（分） |
| reason | VARCHAR(500) | NOT NULL | 理由 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| rejection_reason | VARCHAR(500) | NULL | 却下理由 |
| approver_id | BIGINT | FK → employees(id), NULL | 承認者 |
| approved_at | TIMESTAMP | NULL | 承認日時 |
| version | BIGINT | NOT NULL, DEFAULT 0 | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

---

## インデックス

```sql
-- 勤怠記録: 社員×期間での検索が主
CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, date);

-- 申請一覧: ステータス＋部署での絞り込み
CREATE INDEX idx_leave_requests_status ON leave_requests(status, employee_id);
CREATE INDEX idx_overtime_requests_status ON overtime_requests(status, employee_id);

-- 有給残高: 社員×年度
CREATE INDEX idx_leave_balances_employee_year ON leave_balances(employee_id, fiscal_year);

-- 社員: 部署所属の検索
CREATE INDEX idx_employees_department ON employees(department_id) WHERE active = TRUE;
```

---

## マイグレーション方針

- Flyway で管理（`src/main/resources/db/migration/`）
- ファイル命名: `V{番号}__{説明}.sql`
- `ddl-auto=validate`（Flyway 以外での自動DDL禁止）
- データ保持期間: 3年（アプリケーション層でアーカイブ/削除を管理）
