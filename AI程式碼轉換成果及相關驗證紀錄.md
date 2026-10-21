# AI 程式碼轉換成果及相關驗證紀錄

## 1. 文件目的

本文件彙整 `onlinebanking` 舊 JSF／Spring Web Flow 專案轉換至 `OnlineBankingSystem` 的程式碼成果與驗證紀錄。

本報告可證明目前已完成的 Legacy Migration 程式碼、功能行為、編譯、測試、資料庫與新舊系統人工比對結果。AI 模型選擇與標準轉換方法另記錄於 [AI選用模型轉換流程及輸出入方法.md](AI選用模型轉換流程及輸出入方法.md)。

## 2. 轉換邊界

### 2.1 來源專案

`onlinebanking` 為 Java 8 WAR 專案，包含 JSF／PrimeFaces、Spring Web Flow、Spring Security、JPA／Hibernate、XML 設定、DAO、Service 與 Oracle schema。

來源專案只作為唯讀的畫面、流程、商業規則、資料模型與行為基準，未在本次轉換中修改。

### 2.2 目標專案

`OnlineBankingSystem` 使用：

- Java 21、Spring Boot 4.1.0、Maven、REST API。
- Spring Data JPA、Hibernate、Oracle JDBC。
- Vue、TypeScript、Vue Router、Pinia、Vite。
- Flyway migration 與獨立 Oracle schema。

### 2.3 資料邊界

舊表結構轉換為新表名：

| 舊資料表 | 新資料表 |
| --- | --- |
| `bankuser` | `BANK_USER` |
| `account` | `ACCOUNT` |
| `transactionrec` | `TRANSACTION_REC` |

新資料庫只建立 schema 結構，不匯入舊使用者、帳戶或交易資料；新舊 Oracle volume 與可寫入 schema 分離。

## 3. 程式碼轉換成果

### 3.1 後端成果

已產生 Spring Boot 後端功能模組：

| Legacy 功能 | Spring Boot 轉換成果 |
| --- | --- |
| 註冊／登入／登出 | `auth/AuthController.java`、`AuthService.java`、Request／Response、`SecurityConfig.java` |
| 個人資料 | `user/UserProfileController.java`、`UserProfileService.java`、`BankUser.java`、Profile Request／Response |
| 帳戶 | `account/AccountController.java`、`AccountService.java`、`BankAccount.java`、Repository、Request／Response |
| 存款／提款 | `transaction/MoneyTransactionController.java`、`MoneyTransactionService.java`、Request／Response |
| 轉帳 | `TransferRequest.java`、交易 Service、目的帳戶存在性 API |
| 交易紀錄 | `TransactionRecord.java`、Repository、History Response、查詢 API |
| 共用錯誤處理 | `common/ApiError.java`、`ApiExceptionHandler.java` |

後端已將下列舊系統行為轉換為 REST API：

- Session 登入狀態與未登入 `401`。
- BCrypt 密碼與 Security PIN 驗證。
- 使用者、帳戶與交易資料的權限限制。
- 舊版欄位驗證、金額上限、餘額驗證與錯誤訊息。
- 存提款與轉帳的餘額及交易紀錄交易邊界。
- 以 Flyway 建立 `BANK_USER`、`ACCOUNT`、`TRANSACTION_REC` 與 `HIBERNATE_SEQUENCE`。

### 3.2 前端成果

已產生 Vue 前端功能模組：

| Legacy 畫面／流程 | Vue 轉換成果 |
| --- | --- |
| 登入／註冊 | `LoginView.vue`、`RegisterView.vue`、`stores/auth.ts`、Router guard |
| 帳戶首頁 | `MigrationHomeView.vue`、`LegacyBankingLayout.vue` |
| 建立帳戶 | `NewAccountView.vue`、`api/account.ts` |
| 存款／提款 | `MoneyTransactionView.vue`、`api/transaction.ts` |
| 轉帳 | `TransferView.vue`、轉帳 API 與 TypeScript type |
| 交易紀錄 | `TransactionHistoryView.vue`、查詢 API 與結果表格 |
| 個人資料 | `UserProfileView.vue`、`api/user.ts`、Profile type |

同時保留或重製舊系統的重要畫面特徵：

- 綠色標題列、fieldset、背景紋理與舊版圖片資源。
- 欄位順序、英文文案、按鈕配置與成功摘要。
- 舊版登入、註冊、帳戶與交易操作流程。
- Vue Router route、Pinia session state 與 Vite `/api` proxy。

### 3.3 資料庫成果

- `V001__create_bank_user.sql`：建立 `BANK_USER`。
- `V002__create_account.sql`：建立 `ACCOUNT`。
- `V003__create_transaction_rec.sql`：建立 `TRANSACTION_REC`。
- `V004__create_hibernate_sequence.sql`：建立 `HIBERNATE_SEQUENCE`。

Migration 保留舊 schema 的欄位、型別、主鍵、外鍵與唯一性，不包含舊資料與破壞性的 `DROP TABLE` 語句。

## 4. 功能轉換與驗證結果

| 功能 | 程式碼成果 | 主要驗證結果 | 狀態 |
| --- | --- | --- | --- |
| 註冊／登入／登出 | REST auth、Session、Vue 登入／註冊與 Router guard | BCrypt、Session、重複帳號、錯誤密碼、登出 | 通過 |
| 帳戶 | Account Entity、Repository、Service、Controller、Vue 建立畫面 | Checking／Saving、4 位數 PIN、初始餘額 0、未登入 `401` | 通過 |
| 存款 | Deposit API、Service、Vue 表單與成功頁 | `100.50` 成功、錯誤 PIN／超額／他人帳戶拒絕 | 通過 |
| 提款 | Withdraw API、Service、Vue 表單與成功頁 | `40.25` 成功、餘額不足／錯誤 PIN／超額拒絕 | 通過 |
| 轉帳 | Transfer API、跨帳戶交易 Service、Vue 轉帳頁 | 跨使用者轉帳、來源／目的餘額及 Transfer 紀錄一致 | 通過 |
| 交易紀錄 | History Repository、Service、Controller、Vue 表格 | Deposit、Withdraw、Send／Receive、日期範圍 | 通過 |
| 個人資料 | Profile GET／PUT、欄位驗證、Vue 編輯與成功頁 | 本人資料限制、欄位驗證、空白 Middle Initial 為 `NULL` | 通過 |

各功能的詳細來源分析與驗證結果：

- [註冊與登入 Migration](docs/註冊與登入Migration.md)
- [帳戶 Migration](docs/帳戶Migration.md)
- [存款與提款 Migration](docs/存款與提款Migration.md)
- [轉帳 Migration](docs/轉帳Migration.md)
- [交易紀錄 Migration](docs/交易紀錄Migration.md)
- [個人資料 Migration](docs/個人資料Migration.md)

## 5. 編譯與自動化檢查紀錄

### 5.1 後端

已記錄並通過：

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean verify
```

另於交易紀錄與個人資料切片執行：

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn package -DskipTests
```

結果包含：

- Java 21 Maven build 成功。
- Schema 契約測試、Service 測試與交易測試通過。
- Spring Boot Jar 成功產生。
- Flyway migration 與 Hibernate schema validation 通過。

### 5.2 前端

已記錄並通過：

```bash
cd frontend
npm run format
npm run lint
npm run build
```

結果包含：

- Prettier 格式檢查成功。
- ESLint／Oxlint 檢查成功。
- TypeScript 型別檢查成功。
- Vite production build 成功。

### 5.3 本機服務

新舊系統使用分離的本機連接埠：

| 服務 | 舊系統 | OnlineBankingSystem 新系統 |
| --- | --- | --- |
| Oracle | `1521` | `11521` |
| Backend | Tomcat `8080` | Spring Boot `18080` |
| Frontend | `/onlinebanking` | Vite `5173` |

已記錄 Oracle healthcheck、Spring Boot `/actuator/health`、Vue 首頁及未登入 Session API 驗證結果。

## 6. 新舊系統人工比對紀錄

人工比對報告記錄於 [新舊系統人工比對.md](docs/新舊系統人工比對.md)，主要結果如下：

- 註冊／登入／登出：通過。
- 帳戶建立與列表：通過。
- 存款與提款：通過。
- 跨使用者轉帳：通過。
- 交易紀錄與日期範圍：通過。
- 個人資料讀取與修改：通過。
- 未登入 API 回傳 `401`。
- 他人帳戶操作回傳 `404`，不洩漏帳戶歸屬。
- 錯誤 PIN、餘額不足、同帳戶轉帳與超額金額回傳 `400`，且資料不異動。
- 舊版 JSF／PrimeFaces／Web Flow 已由 Vue route、HTML form、REST API 與元件狀態取代；此差異屬技術實作差異。

## 7. 資料安全與隔離驗證

- 舊專案未被修改。
- 新 Oracle 使用獨立 schema、container 與 volume。
- 新 schema 驗證時三張業務表保持空白。
- 測試使用者、帳戶、交易、Cookie 與暫存 response 於驗證後清除。
- 文件與 Git 掃描未發現應提交的密碼、Token、私鑰或正式連線資訊。

## 8. 結論

目前 `OnlineBankingSystem` 已有完整的 Legacy Migration 程式碼成果與相關驗證紀錄，核心功能已由舊 JSF／Spring Web Flow 行為轉換為 Spring Boot REST API 與 Vue 操作流程，並完成編譯、測試、資料庫啟動與新舊人工比對。

但目前文件主要證明「轉換後成果可運作且行為相容」，尚未形成逐次 AI 模型輸入／輸出／人工決策的完整追蹤鏈。
