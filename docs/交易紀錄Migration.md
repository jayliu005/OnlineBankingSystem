# 交易紀錄 Migration

## 舊系統分析

此功能切片以舊專案下列內容作為唯讀行為基準：

- `tranHistory.xhtml`：選擇帳戶、開始日期、結束日期與 View History 操作。
- `tranHistory2.xhtml`：交易 ID、交易時間、交易金額與交易 Note 表格。
- `account-flow.xml`：`historyDetail` 到 `historyDetail2` 的查詢流程。
- `TransactionServiceImpl.loadRecordsByAccountAndTime`：帳戶權限、日期範圍與轉帳 Note 規則。
- `TransactionJpaDao.loadRecordsByAccount`：來源或目的帳戶都可查到交易。

## 舊系統行為

- 只能選擇目前登入使用者擁有的帳戶。
- 查詢必須提供開始日期與結束日期，開始日期不可晚於結束日期。
- 日期範圍包含結束日期當天。
- Deposit 顯示 `Deposit Money`，Withdraw 顯示 `Withdraw Money`。
- Transfer 若本人是來源帳戶，顯示 `Send to Account ID ...`；若本人是目的帳戶，顯示 `Receive from Account ID ...`。
- 查詢結果顯示交易 ID、交易時間、交易金額與交易 Note，不顯示 PIN。

## 新系統實作

### REST API

`GET /api/transactions/history`：

```text
/api/transactions/history?accountId=36&startDate=2026-07-23&endDate=2026-07-24
```

回應：

```json
[
  {
    "id": 38,
    "transactionTime": "2026-07-24T16:53:50.075745",
    "transactionAmount": 200.00,
    "transactionNote": "Deposit Money"
  }
]
```

- 伺服器以登入名稱和帳戶 ID 一起驗證帳戶歸屬。
- 查詢涵蓋 `ACCOUNT_ID_FROM` 或 `ACCOUNT_ID_TO`。
- 查詢使用開始日 00:00 至結束日隔日 00:00 的半開區間，確保完整包含結束日期。
- 非本人帳戶回傳 `404`，未登入回傳 `401`，日期錯誤回傳 `400`。

### Vue

- 新增 `/accounts/history`，並啟用帳戶首頁的 Transaction History 選單。
- 畫面沿用舊系統欄位順序、文案、版型與 `history.png` 資源。
- 查詢表單完成後顯示舊版四欄結果表格及 Back to Home／Logout 操作。
- 沒有帳戶時停用查詢並提示先建立銀行帳戶。

## 驗證結果

- Java 21 `mvn test` 成功，共 10 筆測試通過。
- Java 21 `mvn package -DskipTests` 成功產生新版 Jar。
- Node 24 format、lint、型別檢查及 production build 全數成功。
- Oracle 23 Free 通過 Flyway 與 Hibernate schema validation。
- 實際建立兩個使用者與帳戶，存款 `200.00` 後轉帳 `75.50`；來源帳戶查到 Deposit 與 Send 紀錄，目的帳戶查到 Receive 紀錄。
- 日期範圍查詢經 Spring Boot 及 Vite proxy 均回傳 `200`。
- 查詢他人帳戶回傳 `404`，開始日期晚於結束日期回傳 `400`。
- 驗證用使用者、帳戶、交易與 Cookie 已清除，資料庫恢復為原有 `1:0:0`。

## 差異與後續

- JSF Flow scope 改為 Vue route、表單狀態與 REST API。
- 舊系統在交易日期比較上使用結束日加一天；新系統以資料庫半開區間實作相同的日期意圖。
- 本階段不加入分頁、額外篩選、匯出或交易彙總；這些屬於後續優化。
- 個人資料查詢／修改為下一個 Migration 功能切片。
