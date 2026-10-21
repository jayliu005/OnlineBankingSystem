# 帳戶 Migration

## 舊系統分析

此切片以舊專案下列內容作為唯讀行為基準：

- `account-flow.xml`：登入後載入目前使用者帳戶、建立帳戶與返回首頁流程。
- `accountHome.xhtml`：帳戶摘要欄位及 `New Account` 操作。
- `newAccount.xhtml`：Checking／Saving、4 位數 Security Pin 與確認 Pin 驗證。
- `newAccountAdded.xhtml`：建立成功後顯示帳戶編號、類型、餘額及建立時間。
- `BankAccount`、`AccountServiceImpl`、`AccountJpaDao`：帳戶關聯、初始值、PIN 雜湊及使用者帳戶查詢。
- `database.sql` 與新專案 `V002__create_account.sql`：`ACCOUNT` 欄位、主鍵及 `BANK_USER` 外鍵。

## 舊系統帳戶行為

- 帳戶只能建立為 `Checking` 或 `Saving`。
- Security Pin 必須為 4 位數且兩次輸入一致。
- PIN 使用 BCrypt 雜湊後才寫入資料庫。
- 新帳戶餘額固定為 `0`，建立時間由伺服器產生。
- 帳戶 ID 使用舊系統共用的 `HIBERNATE_SEQUENCE`。
- 帳戶首頁只載入目前登入使用者所屬帳戶。
- 帳戶摘要顯示 Account Id、Account Type、Current Balance 與 Time of Created。

## 新系統實作

### REST API

| Method | Path | 行為 |
| --- | --- | --- |
| `GET` | `/api/accounts` | 依登入 Session 列出目前使用者的帳戶 |
| `POST` | `/api/accounts` | 建立目前使用者的新帳戶並回傳帳戶摘要 |

建立帳戶 request：

```json
{
  "accountType": "Checking",
  "securityPin": "1234",
  "repeatSecurityPin": "1234"
}
```

Response 不包含 Security Pin 或 BCrypt hash。

### 後端

- `BankAccount` 對應既有 `ACCOUNT` schema，使用共用 `HIBERNATE_SEQUENCE`。
- `AccountService` 強制由目前登入名稱取得 `BANK_USER`，不接受前端指定使用者 ID。
- `BankAccountRepository` 只依目前使用者 ID 查詢帳戶。
- 建立時由後端設定零餘額、BCrypt PIN 與建立時間。
- 未登入呼叫帳戶 API 回傳 `401`。

### Vue

- 帳戶首頁從 `/api/accounts` 載入實際資料，不再使用固定空白列。
- `New Account` 選單已啟用；尚未 Migration 的存款、提款、轉帳與交易紀錄維持停用。
- `/accounts/new` 依舊版畫面提供帳戶類型、PIN、確認 PIN、Cancel 與 Create Account。
- 建立成功後顯示舊版成功訊息與帳戶摘要，並可返回首頁或登出。
- `succeed.png` 與 `home.png` 為從舊專案複製的未修改資源。

## 驗證結果

- Java 21 `mvn clean verify` 成功，2 筆測試通過。
- Node 24 format、lint、型別檢查與 production build 全數成功。
- 現有獨立 Oracle 23 Free schema 通過 Flyway 與 Hibernate schema validation。
- 未登入帳戶 API 回傳 `401`。
- 新註冊測試使用者初始帳戶清單為空。
- 不合法 PIN 及不一致 PIN 回傳 `400` 與對應欄位錯誤。
- 建立 Checking 帳戶回傳 `201`，列表可立即讀回相同帳戶。
- Vite `/api` proxy 可使用同一 Session 讀取帳戶。
- 資料庫確認帳戶類型為 Checking、餘額為 `0.00`、PIN 為 60 字元 BCrypt。
- 驗證用使用者、帳戶、Cookie 與暫存 response 已清除。

## 差異與後續

- JSF Flow scope 改為 Vue route 與元件狀態；頁面文案及操作順序維持一致。
- 帳戶清單依 ID 排序，避免資料庫未指定順序造成畫面不穩定。
- 存款、提款、轉帳、交易紀錄及其帳戶 PIN 驗證不屬於本切片，於後續功能實作。
