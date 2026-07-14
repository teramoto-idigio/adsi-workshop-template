# Unit 2: 申請・承認ドメイン

**Phase**: B（Unit 0 完了後、Unit 1 と並行実装）
**担当**: 担当者②
**目的**: 有給申請・残業申請・マネージャー承認・有給残日数管理

---

## スコープ

### ユーザーストーリー

- US-4: 有給休暇申請
- US-5: 有給残日数管理
- US-6: 残業申請
- US-7: 申請承認（マネージャー）

### テーブル

- leave_requests（DDL は Unit 0 で作成済み）
- leave_balances（DDL は Unit 0 で作成済み）
- overtime_requests（DDL は Unit 0 で作成済み）

### API エンドポイント

- `GET /api/leave/requests?year=`
- `POST /api/leave/requests`
- `GET /api/leave/balance?fiscalYear=`
- `GET /api/manager/leave/requests`
- `POST /api/manager/leave/requests/{id}/approve`
- `POST /api/manager/leave/requests/{id}/reject`
- `POST /api/admin/leave/balance/{employeeId}`
- `GET /api/overtime/requests?year=&month=`
- `POST /api/overtime/requests`
- `GET /api/manager/overtime/requests`
- `POST /api/manager/overtime/requests/{id}/approve`
- `POST /api/manager/overtime/requests/{id}/reject`

---

## 実装内容

### Backend

1. **LeaveService**（interface + impl）
   - 有給申請の作成（残日数チェック）
   - 有給申請の承認（残日数消化: 全休1.0 / 半休0.5）
   - 有給申請の却下
   - 残日数照会
   - 有給付与（管理者）

2. **OvertimeService**（interface + impl）
   - 残業申請の作成（事前/事後）
   - 残業申請の承認
   - 残業申請の却下

3. **LeaveController**
   - 自分の有給申請 CRUD + 残日数取得

4. **OvertimeController**
   - 自分の残業申請 CRUD

5. **ManagerApprovalController**
   - 有給申請の承認/却下
   - 残業申請の承認/却下
   - 未承認一覧取得（自部署のみ）

6. **AdminLeaveController**
   - 有給付与

### Frontend

1. **有給申請画面** (`/leave`)
   - LeaveRequestForm（日付 + 種類選択）
   - 残日数表示
   - 申請履歴一覧

2. **残業申請画面** (`/overtime`)
   - OvertimeRequestForm（日付 + 事前/事後 + 時間 + 理由）
   - 申請履歴一覧

3. **承認一覧画面** (`/approvals`)
   - タブ切り替え（有給 / 残業）
   - ApprovalList（承認/却下ボタン）
   - 却下理由入力ダイアログ

4. **有給付与画面** (`/admin/leave`)
   - 社員選択 → 付与日数入力
   - 付与履歴一覧

---

## ステートマシン（申請ステータス）

```
         ┌─── approve ───→ APPROVED
PENDING ─┤
         └─── reject ────→ REJECTED
```

- PENDING → APPROVED: 承認者ID・承認日時を記録。有給の場合は残日数を消化。
- PENDING → REJECTED: 却下理由を記録。
- APPROVED / REJECTED からの遷移なし（確定後は変更不可）。

---

## ビジネスルール（テスト重点）

### 有給申請

- 残日数 ≧ 消化日数 でないと申請不可
- 全休: 1.0日消化、AM半休/PM半休: 0.5日消化
- 承認時に `leave_balances.used_days` を加算
- 同一日に複数申請不可

### 残業申請

- 事前申請: 未来日のみ
- 事後申請: 過去日〜当日のみ
- 残業時間は1分以上

### 承認権限

- マネージャーは自部署のメンバーの申請のみ承認可能
- 自分自身の申請は承認不可（将来拡張用。MVP では制約なし）

---

## テストケース（主要）

- 有給申請 → 残日数が正しく表示されること
- 残日数0で申請 → エラーが返ること
- 有給承認 → used_days が加算されること（全休+1.0、半休+0.5）
- 残業申請（事前） → 未来日のみ受付
- 残業申請（事後） → 当日以前のみ受付
- マネージャーが他部署の申請を承認 → 403エラー
- 承認済み申請を再度承認 → エラー
- 却下時に理由が必須

---

## 完了条件

- [ ] 有給申請 → 承認 → 残日数消化の一連フローが動作する
- [ ] 残業申請 → 承認の一連フローが動作する
- [ ] 残日数不足時のエラーハンドリングが正しい
- [ ] 権限チェック（自部署のみ）が正しく動作する
- [ ] 管理者による有給付与が動作する

---

## 依存

- Unit 0（認証・Employee Entity・テスト基盤）
- Unit 1 との依存なし（並行実装可能）
