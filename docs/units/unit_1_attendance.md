# Unit 1: 勤怠ドメイン

**Phase**: B（Unit 0 完了後、Unit 2 と並行実装）
**担当**: 担当者①
**目的**: 勤怠記録の入力・表示・集計・CSV出力

---

## スコープ

### ユーザーストーリー

- US-1: 勤怠入力
- US-2: 月次勤怠カレンダー表示
- US-3: 勤務時間集計
- US-8: 部署メンバー勤怠閲覧（マネージャー）
- US-9: CSV エクスポート（個人月次一覧 + 部署月次サマリー）

### テーブル

- attendance_records（DDL は Unit 0 で作成済み）

### API エンドポイント

- `GET /api/attendance?year=&month=`
- `PUT /api/attendance/{date}`
- `GET /api/attendance/summary?year=&month=`
- `GET /api/manager/attendance?year=&month=`
- `GET /api/manager/attendance/{employeeId}?year=&month=`
- `GET /api/export/attendance?year=&month=`
- `GET /api/export/department-summary?year=&month=`

---

## 実装内容

### Backend

1. **AttendanceService**（interface + impl）
   - 勤怠記録の登録・更新（upsert: 同一日は上書き）
   - 月次勤怠一覧取得
   - 勤務時間計算ロジック

2. **AttendanceSummaryService**（interface + impl）
   - 日次計算: 勤務時間、残業時間（8.5h超過）、深夜勤務時間（22:00-5:00）
   - 月次集計: 総労働/残業/深夜、所定労働時間、フレックス過不足
   - 月の所定労働時間 = 営業日数 × 8.5h

3. **CsvExportService**（勤怠部分）
   - 個人月次一覧CSV
   - 部署月次サマリーCSV

4. **AttendanceController**
   - 自分の勤怠 CRUD

5. **ManagerAttendanceController**
   - 部署メンバー勤怠閲覧（自部署のみ権限チェック）

6. **ExportController**（勤怠CSV部分）

### Frontend

1. **勤怠カレンダー画面** (`/attendance`)
   - CalendarGrid / CalendarCell コンポーネント
   - 月切り替え
   - 有給・半休の日の視覚表示（Unit 2 のデータ連携は後で統合）

2. **勤怠入力モーダル**
   - AttendanceForm（出勤/退勤/休憩/備考）
   - リアルタイム勤務時間計算表示

3. **月次サマリーカード**
   - 総労働/残業/深夜/フレックス過不足

4. **部署メンバー勤怠画面** (`/team`, `/team/:id`)
   - メンバーサマリー一覧
   - メンバー選択→日別詳細

5. **CSVダウンロードボタン**

---

## ビジネスロジック（テスト重点）

### 勤務時間計算

```
勤務時間(分) = clockOut - clockIn - breakMinutes
残業時間(分) = max(0, 勤務時間 - 510)   // 510分 = 8.5h
```

### 深夜勤務時間計算

```
深夜時間帯: 22:00〜翌5:00
深夜勤務時間 = 勤務時間のうち深夜時間帯に該当する部分
（休憩時間は深夜時間帯からは差し引かない簡易計算）
```

### フレックス清算

```
月の所定労働時間 = 営業日数 × 510分
過不足 = 総労働時間 - 月の所定労働時間
```

---

## テストケース（主要）

- 通常勤務（9:00-18:00, 休憩60分）→ 勤務8h, 残業0
- 残業あり（9:00-20:00, 休憩60分）→ 勤務10h, 残業1.5h
- 深夜勤務（18:00-23:30, 休憩0分）→ 深夜1.5h
- 月次集計の合計が正しいこと
- CSV出力のフォーマットが正しいこと
- マネージャーが他部署メンバーの勤怠を見られないこと

---

## 完了条件

- [ ] 勤怠入力 → カレンダー表示 → 集計が一通り動作する
- [ ] 深夜・残業の計算ロジックのテストが通る
- [ ] CSVダウンロードが動作する
- [ ] マネージャーの部署メンバー閲覧が動作する

---

## 依存

- Unit 0（認証・Employee Entity・テスト基盤）
- Unit 2 との依存なし（並行実装可能）
