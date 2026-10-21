# 轉帳 Migration

## 舊系統分析

此功能切片以舊專案下列內容作為唯讀行為基準：

- `transfer.xhtml`：來源帳戶、目的帳戶、金額、Security Pin 與即時帳戶存在檢查。
- `tranSucceed.xhtml`：交易成功摘要欄位與文案。
- `TransactionServiceImpl.createTransferRec`：驗證順序、餘額更新與交易紀錄建立。
- `messages.properties`：目的帳戶存在、同帳戶轉帳、錯誤 PIN 與餘額不足訊息。

## 舊系統行為

- 來源帳戶必須屬於目前登入使用者。
- 目的帳戶可屬於其他使用者，但必須存在。
- 來源與目的帳戶不得相同。
- 必須輸入正確的來源帳戶 Security Pin。
- 單筆轉帳金額範圍為 `0.01–10000.00`，且不得超過來源帳戶餘額。
- 成功時扣除來源帳戶餘額、增加目的帳戶餘額，並建立一筆同時記錄兩個帳戶 ID 的 `Transfer` 紀錄。
- 餘額更新與交易紀錄必須在同一資料庫交易內完成。

## 新系統實作

### REST API

| Method | Path | 行為 |
| --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/existence` | 回傳目的帳戶是否存在及舊版提示訊息 |
| `POST` | `/api/transactions/transfers` | 驗證來源歸屬、目的帳戶、金額、PIN 與餘額後執行轉帳 |

轉帳 request：

```json
{
  "accountFromId": 30,
  "accountToId": 31,
  "amount": "75.50",
  "securityPin": "1234"
}
```

成功 response 包含交易 ID、`Transfer` 類型、金額、來源帳戶、目的帳戶與伺服器交易時間，不包含 PIN。

### 後端

- 以目前登入名稱與來源帳戶 ID 一起查詢，不能從其他使用者帳戶轉出。
- 目的帳戶只驗證存在性，保留舊系統可跨使用者轉帳的行為。
- 使用 BCrypt 比對來源帳戶 Security Pin。
- 使用 `@Transactional` 確保兩個帳戶餘額及 `TRANSACTION_REC` 一起提交或一起回滾。
- 目的帳戶不存在或來源帳戶不可見時回傳 `404`。
- 同帳戶、錯誤 PIN、餘額不足或不合法金額回傳 `400`。

### Vue

- 新增 `/accounts/transfer`，並啟用帳戶首頁的 Transfer Money 選單。
- 畫面沿用舊系統版型、欄位順序、按鈕及文案。
- 來源帳戶選單由目前使用者的 `/api/accounts` 載入。
- 輸入目的帳戶 ID 時延遲呼叫存在性 API，並防止較舊的非同步回應覆蓋新輸入結果。
- 沒有來源帳戶時停用確認按鈕並提示先建立帳戶。
- 成功後沿用舊版 `Money Transaction succeed!` 摘要畫面。

## 驗證結果

- Java 21 `mvn clean verify` 成功，共 10 筆測試通過，其中交易 Service 測試 8 筆。
- Node 24 format、lint、型別檢查及 production build 全數成功。
- Oracle 23 Free 通過 Flyway 與 Hibernate schema validation。
- 實際建立兩個不同使用者的帳戶，先存款 `200.00`，再由帳戶 `30` 轉帳 `75.50` 至帳戶 `31`。
- 成功後來源餘額為 `124.50`、目的餘額為 `75.50`，資料庫只有一筆 Deposit 與一筆 Transfer 成功紀錄，Transfer 同時保存來源及目的帳戶 ID。
- 不存在的目的帳戶回傳 `404`；同帳戶、錯誤 PIN、餘額不足及超過 `10000.00` 回傳 `400`。
- 另一位登入使用者嘗試從不屬於自己的來源帳戶轉出時回傳 `404`；未登入呼叫回傳 `401`。
- 所有失敗情境均未改變兩個帳戶餘額或新增交易紀錄。
- 驗證用使用者、帳戶、交易與 Cookie 已清除；資料庫恢復為原有 `1` 位使用者、`0` 個帳戶及 `0` 筆交易。

## 差異與後續

- JSF Flow scope 改為 Vue route 與元件狀態，欄位、文案、限制與成功摘要維持一致。
- 新系統除前端帳戶選單外，也在 REST API 伺服器端強制驗證來源帳戶歸屬。
- 本階段依既定範圍不加入 locking、版本欄位或 idempotency；高併發轉帳控制保留至後續優化。
- 交易紀錄與個人資料為下一個 Migration 功能切片。
