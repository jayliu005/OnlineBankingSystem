# 存款與提款 Migration

## 舊系統分析

此切片以舊專案下列內容作為唯讀行為基準：

- `account-flow.xml`：存款、提款與交易成功頁面流程。
- `deposit.xhtml`、`withdraw.xhtml`：帳戶選擇、金額範圍、Security Pin 與操作文案。
- `tranSucceed.xhtml`：成功後顯示的交易欄位與返回首頁／登出操作。
- `TransactionDetail`：交易輸入及成功結果資料。
- `TransactionRec`：交易類型、金額、時間與來源／目的帳戶關聯。
- `TransactionServiceImpl`：PIN、餘額驗證、餘額更新與交易紀錄建立順序。
- `TransactionJpaDao`、`AccountJpaDao` 與 `database.sql`：交易及帳戶持久化行為。

## 舊系統行為

### 共同規則

- 必須選擇一個目前使用者可見的帳戶。
- 必須輸入正確的帳戶 Security Pin。
- 成功時使用共用 `HIBERNATE_SEQUENCE` 產生交易 ID。
- 交易時間由伺服器產生。
- 餘額更新與 `TRANSACTION_REC` 建立必須同時成功。
- Deposit／Withdraw 的 `ACCOUNT_ID_TO` 為 `NULL`。

### 存款

- 單筆金額範圍為 `0.01–50000.00`。
- 交易類型為 `Deposit`。
- 新餘額為原餘額加存款金額。

### 提款

- 單筆金額範圍為 `0.01–2000.00`。
- 交易類型為 `Withdraw`。
- 提款不得超過目前餘額；否則顯示 `No enough money in account ID '...'`。
- 新餘額為原餘額減提款金額。

## 新系統實作

### REST API

| Method | Path | 行為 |
| --- | --- | --- |
| `POST` | `/api/transactions/deposits` | 驗證帳戶、金額及 PIN，增加餘額並建立 Deposit 紀錄 |
| `POST` | `/api/transactions/withdrawals` | 驗證帳戶、金額、PIN 及餘額，扣減餘額並建立 Withdraw 紀錄 |

Request：

```json
{
  "accountId": 1,
  "amount": "100.50",
  "securityPin": "1234"
}
```

成功 response 包含交易 ID、類型、金額、來源帳戶、目的帳戶及交易時間，不包含 PIN。

### 後端

- `TransactionRecord` 對應既有 `TRANSACTION_REC` schema。
- 以目前登入名稱和帳戶 ID 一起查詢，不能操作其他使用者帳戶。
- 使用 BCrypt 比對 Security Pin。
- `@Transactional` 確保交易紀錄與帳戶餘額一起提交或一起回滾。
- 不合法輸入、錯誤 PIN 或餘額不足回傳 `400`。
- 不存在或不屬於目前使用者的帳戶回傳 `404`，不洩漏帳戶歸屬。

### Vue

- `/accounts/deposit`：舊版 Deposit Money 畫面，金額上限 `50000.00`。
- `/accounts/withdraw`：舊版 Withdraw Money 畫面，金額上限 `2000.00`。
- 帳戶選單由 `/api/accounts` 載入目前使用者帳戶。
- 沒有帳戶時停用確認按鈕並提示先建立帳戶。
- 成功後沿用舊版 `Money Transaction succeed!` 摘要畫面。
- 帳戶首頁已啟用 Withdraw Money 與 Deposit Money；轉帳及交易紀錄仍停用。

## 驗證結果

- Java 21 `mvn clean verify` 成功，共 6 筆測試通過，其中存提款 Service 測試 4 筆。
- Node 24 format、lint、型別檢查及 production build 全數成功。
- Oracle 23 Free 通過 Flyway 與 Hibernate schema validation。
- 經 Vite proxy 存款 `100.50` 回傳 `201` 並建立 Deposit 紀錄。
- 錯誤 PIN 回傳 `400`，沒有改變餘額或新增紀錄。
- 提款超過餘額回傳 `400`，沒有改變餘額或新增紀錄。
- 存款 `50000.01` 與提款 `2000.01` 均回傳 `400`。
- 另一位登入使用者操作不屬於自己的帳戶時回傳 `404`。
- 經 Vite proxy 提款 `40.25` 回傳 `201` 並建立 Withdraw 紀錄。
- 最終資料庫餘額為 `60.25`，且只有 `Deposit=100.50`、`Withdraw=40.25` 兩筆成功紀錄。
- 驗證用使用者、帳戶、交易、Cookie 與暫存 response 已清除；原有資料未變更。

## 差異與後續

- JSF Flow scope 改為 Vue route 與元件狀態，欄位、文案、金額限制及成功摘要維持一致。
- 舊畫面只能選取使用者帳戶；新 REST API 另外在伺服器端強制驗證帳戶歸屬。
- 本階段依既定範圍不加入 locking、版本欄位或 idempotency；併發控制保留至後續優化。
- 轉帳與交易紀錄查詢屬於後續 Migration 切片。
