# Unit 0: 共通基盤

**Phase**: A（最初に2人で共同実装）
**目的**: プロジェクト骨格を作り、テストが実行できる状態にする

---

## スコープ

### ユーザーストーリー

- US-12: 認証（ログイン/ログアウト）
- US-10: 社員管理（管理者）
- US-11: 部署管理（管理者）

### テーブル（Flyway — 全テーブルをここで作成）

- departments
- employees
- attendance_records
- leave_requests
- leave_balances
- overtime_requests

### API エンドポイント

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/admin/employees`
- `POST /api/admin/employees`
- `PUT /api/admin/employees/{id}`
- `POST /api/admin/employees/{id}/deactivate`
- `GET /api/admin/departments`
- `POST /api/admin/departments`
- `PUT /api/admin/departments/{id}`
- `DELETE /api/admin/departments/{id}`

---

## 実装内容

### 1. プロジェクト骨格

- Spring Boot プロジェクト生成（Gradle / Maven）
- Next.js プロジェクト生成
- Docker Compose（PostgreSQL + アプリ）
- CI 用の `check` スクリプト

### 2. Backend 共通

- **Flyway マイグレーション**: 全6テーブル（Unit 1/2 のテーブルも含む）
- **Entity**: Employee, Department, AttendanceRecord, LeaveRequest, LeaveBalance, OvertimeRequest
- **Enum**: Role, LeaveType, ApprovalStatus, OvertimeRequestType
- **Repository**: 全6つ（interface 定義）
- **共通例外ハンドラ**: `@RestControllerAdvice`（ValidationError, NotFound, Forbidden）
- **共通 DTO**: ErrorResponse, ValidationErrorResponse

### 3. 認証・認可

- Spring Security 設定（`SecurityFilterChain`）
- `AuthService` / `AuthController`
- BCrypt パスワードエンコーダ
- セッション管理（Cookie ベース）
- ロール別アクセス制御（`/admin/**` = ADMIN、`/manager/**` = MANAGER+ADMIN）

### 4. マスタ管理

- `EmployeeService` / `EmployeeController`（CRUD + 部署異動 + 無効化）
- `DepartmentService` / `DepartmentController`（CRUD + 削除制約）

### 5. Frontend 共通

- Next.js レイアウト（AppLayout / Sidebar）
- 認証コンテキスト・ログイン画面
- API クライアント（fetch ラッパー + withBasePath）
- ルーティング（ロール別ナビゲーション）

### 6. テスト基盤

- `application-test.yml`（H2 or Testcontainers PostgreSQL）
- テストベースクラス（`@SpringBootTest` + `@AutoConfigureMockMvc`）
- 認証テスト
- EmployeeService / DepartmentService のユニットテスト
- Controller テスト（`@WebMvcTest`）

---

## 完了条件

- [ ] `./gradlew test`（or `mvn test`）が全て通る
- [ ] ログイン → 認証付き API 呼び出しが動作する
- [ ] 社員CRUD・部署CRUDのテストが通る
- [ ] Frontend でログイン画面 → ダッシュボードへ遷移できる
- [ ] Unit 1 / Unit 2 の開発者がブランチを切って独立して実装を始められる

---

## 依存

なし（最初の Unit）
