# 註冊與登入 Migration

## 舊系統分析

此切片以舊專案下列內容作為唯讀行為基準：

- `WEB-INF/flows/main/welcome.xhtml`：登入欄位、必填規則與註冊入口。
- `WEB-INF/flows/main/signUp.xhtml`：註冊欄位、長度、格式、密碼確認及使用者名稱可用性提示。
- `WEB-INF/flows/main/main-flow.xml`：註冊成功後立即認證並進入帳戶頁。
- `UserServiceImpl`、`UserJpaDao`：使用者名稱唯一性、BCrypt 密碼與使用者查詢。
- `UserAuthenticationProviderServiceImpl`、`security-config.xml`：Spring Security 認證、session 與登出。
- `BankUser`、`BaseEntity`、`database.sql`：欄位、型別與主鍵策略。
- 舊 Oracle schema：唯讀確認實際存在共用的 `HIBERNATE_SEQUENCE`；原始 `database.sql` 未包含此物件。

## 新系統實作

### REST API

| Method | Path | 行為 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 建立使用者、BCrypt 儲存密碼並立即建立登入 session |
| `POST` | `/api/auth/login` | 驗證使用者名稱與 BCrypt 密碼並建立 session |
| `POST` | `/api/auth/logout` | 使目前 session 失效 |
| `GET` | `/api/auth/session` | 取得目前登入使用者；未登入回傳 `401` |
| `GET` | `/api/auth/username-availability` | 回傳使用者名稱是否可用及舊系統相容訊息 |

登入與註冊 response 只回傳 `id`、`userName`、`firstName` 與 `lastName`，不回傳密碼或完整個人資料。

### 驗證與錯誤

- 使用者名稱長度為 2–30，並由資料庫唯一鍵保障唯一性。
- 密碼至少 3 個字元，且確認密碼必須一致。
- 姓名、地址與中間名縮寫沿用舊畫面的長度限制。
- 性別為 `M` 或 `F`，生日必須為過去日期。
- 郵遞區號、電話與 Email 沿用舊畫面的格式。
- 重複使用者名稱回傳 `409`；登入失敗回傳 `401`；欄位驗證失敗回傳 `400`。

### Vue

- `/login`：登入畫面與註冊入口。
- `/register`：完整註冊表單與使用者名稱可用性提示。
- Pinia 保存目前登入使用者，Router guard 保護登入後頁面。
- 註冊或登入成功後進入首頁，登出後回到登入畫面。
- Vite 將 `/api` 代理至本機 Spring Boot，瀏覽器可使用同源 session cookie。

## 驗證結果

- Java 21 `mvn clean verify` 成功，Migration 契約測試通過。
- Node 24 `npm run format`、`npm run lint`、`npm run build` 全數成功。
- 全新 Oracle 23 Free schema 成功套用 V001–V004，Hibernate schema validation 通過。
- 註冊回傳 `201` 並可立即讀取 session。
- 重複使用者名稱回傳 `409`。
- 登出回傳 `204`，登出後 session 回傳 `401`。
- 錯誤密碼回傳 `401`，正確密碼可重新登入。
- 測試使用者密碼以 60 字元 BCrypt hash 儲存；`ACCOUNT` 與 `TRANSACTION_REC` 仍為空白。
- 驗證使用的容器、network、volume、cookie 與暫存 response 已清除。

## 差異與後續

- JSF FacesMessage 改為 REST JSON 錯誤與 Vue 欄位訊息；訊息語意和 HTTP 狀態保留相同行為。
- 舊系統 `database.sql` 遺漏實際存在的 `HIBERNATE_SEQUENCE`，新系統以 V004 明確納入 Flyway 管理。
- CSRF、cookie 屬性、登入失敗限制與完整自動化認證測試依 Migration 決策保留到後續安全優化。
- 本切片已完成程式與本機獨立環境驗證，並納入整體新舊系統人工比對。
