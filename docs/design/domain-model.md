# ドメインモデル設計

## Entity 一覧

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| name | String | 氏名 |
| email | String | メールアドレス（ログインID・一意） |
| passwordHash | String | BCrypt ハッシュ化パスワード |
| role | Role (enum) | EMPLOYEE / MANAGER / ADMIN |
| department | Department | 所属部署（ManyToOne） |
| active | boolean | 有効フラグ（論理削除用） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

### Department（部署）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| name | String | 部署名（一意） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

### AttendanceRecord（勤怠記録）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| employee | Employee | 社員（ManyToOne） |
| date | LocalDate | 勤務日 |
| clockIn | LocalTime | 出勤時刻 |
| clockOut | LocalTime | 退勤時刻（null許可：退勤前） |
| breakMinutes | Integer | 休憩時間（分） |
| note | String | 備考（null許可） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

**ビジネスルール:**
- 同一社員・同一日の記録は1件のみ（UNIQUE制約）
- 勤務時間 = clockOut − clockIn − breakMinutes
- 残業時間 = max(0, 勤務時間 − 8.5h)
- 深夜勤務時間 = 22:00〜5:00 に該当する勤務時間

### LeaveRequest（有給休暇申請）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| employee | Employee | 申請者（ManyToOne） |
| date | LocalDate | 休暇日 |
| leaveType | LeaveType (enum) | FULL_DAY / AM_HALF / PM_HALF |
| status | ApprovalStatus (enum) | PENDING / APPROVED / REJECTED |
| rejectionReason | String | 却下理由（null許可） |
| approver | Employee | 承認者（ManyToOne、null許可） |
| approvedAt | LocalDateTime | 承認日時（null許可） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

**ビジネスルール:**
- 有給残日数 > 0 でないと申請不可
- 全休 = 1.0日消化、半休 = 0.5日消化
- 承認者は申請者の所属部署のマネージャー

### LeaveBalance（有給残日数）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| employee | Employee | 社員（ManyToOne） |
| fiscalYear | Integer | 年度 |
| totalDays | BigDecimal | 付与日数 |
| usedDays | BigDecimal | 消化日数（0.5刻み） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

**ビジネスルール:**
- remainingDays = totalDays − usedDays
- 半休承認時に usedDays += 0.5、全休承認時に usedDays += 1.0

### OvertimeRequest（残業申請）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | PK（自動採番） |
| employee | Employee | 申請者（ManyToOne） |
| date | LocalDate | 残業日 |
| requestType | OvertimeRequestType (enum) | PRE_APPROVAL / POST_APPROVAL |
| overtimeMinutes | Integer | 残業時間（分） |
| reason | String | 理由 |
| status | ApprovalStatus (enum) | PENDING / APPROVED / REJECTED |
| rejectionReason | String | 却下理由（null許可） |
| approver | Employee | 承認者（ManyToOne、null許可） |
| approvedAt | LocalDateTime | 承認日時（null許可） |
| version | Long | 楽観ロック |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |

---

## Value Object

### WorkDuration（勤務時間）

計算結果を保持する値オブジェクト（DB非永続化、計算用）:

| フィールド | 型 | 説明 |
|-----------|-----|------|
| totalMinutes | int | 総勤務時間（分） |
| overtimeMinutes | int | 残業時間（分）= max(0, total − 510) |
| nightMinutes | int | 深夜勤務時間（分） |

### MonthlyS Summary（月次集計）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| yearMonth | YearMonth | 対象年月 |
| totalWorkMinutes | int | 総労働時間（分） |
| totalOvertimeMinutes | int | 総残業時間（分） |
| totalNightMinutes | int | 総深夜勤務時間（分） |
| prescribedMinutes | int | 月の所定労働時間（分） |
| balanceMinutes | int | 過不足（分） |

---

## Enum

```java
public enum Role {
    EMPLOYEE, MANAGER, ADMIN
}

public enum LeaveType {
    FULL_DAY,   // 全休（1.0日消化）
    AM_HALF,    // AM半休（0.5日消化）
    PM_HALF     // PM半休（0.5日消化）
}

public enum ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

public enum OvertimeRequestType {
    PRE_APPROVAL,   // 事前申請
    POST_APPROVAL   // 事後申請
}
```

---

## 関連図

```
Department ──< Employee ──< AttendanceRecord
                  │
                  ├──< LeaveRequest
                  │
                  ├──< LeaveBalance
                  │
                  └──< OvertimeRequest
```

- Department 1 : N Employee
- Employee 1 : N AttendanceRecord（日別）
- Employee 1 : N LeaveRequest
- Employee 1 : N LeaveBalance（年度別）
- Employee 1 : N OvertimeRequest

---

## Service 一覧

| Service | 責務 |
|---------|------|
| AuthService | ログイン認証・セッション管理 |
| AttendanceService | 勤怠記録 CRUD・勤務時間計算 |
| AttendanceSummaryService | 日次/月次集計・フレックス清算計算 |
| LeaveService | 有給申請・承認・残日数管理 |
| OvertimeService | 残業申請・承認 |
| EmployeeService | 社員 CRUD・部署異動 |
| DepartmentService | 部署 CRUD |
| CsvExportService | CSV生成・ダウンロード |

---

## Repository 一覧

| Repository | 主なクエリ |
|-----------|-----------|
| EmployeeRepository | findByEmail, findByDepartmentId, findByActive |
| DepartmentRepository | findByName |
| AttendanceRecordRepository | findByEmployeeIdAndDateBetween, findByEmployeeIdAndDate |
| LeaveRequestRepository | findByEmployeeIdAndStatus, findByApproverDepartmentAndStatus |
| LeaveBalanceRepository | findByEmployeeIdAndFiscalYear |
| OvertimeRequestRepository | findByEmployeeIdAndStatus, findByApproverDepartmentAndStatus |
