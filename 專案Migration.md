# Online Banking 專案 Migration

本文件整合以下三份文件的決策與執行內容，作為 `OnlineBankingSystem` 新專案開發的主要依據：

- `Codex存取2個專案注意事項.md`
- `專案移轉分析.md`
- `專案移轉規劃.md`

若原始文件與本文件內容不同，以本文件的最新決策為準。原始文件保留作為歷史參考。

## 每次開發前必讀

每次進行 `OnlineBankingSystem` 的分析、設計、程式修改、資料庫變更、建置、測試或部署前，Codex 必須先完整閱讀本文件，再開始操作。

建立新專案後，必須在新專案的 `AGENTS.md` 加入相同規則，並記錄：

- 舊專案與新專案的實際路徑。
- 舊專案只能讀取，禁止修改。
- 新專案的技術架構、建置與啟動方式。
- 每次開發前必須閱讀 `專案Migration.md`。
- 功能完成時的人工比對與驗收標準。

## 一、已確定的 Migration 決策

### 1. 專案角色

- A 專案：`onlinebanking`，保留為舊系統、功能規格及行為基準。
- B 專案：`OnlineBankingSystem`，所有新程式碼只寫在此專案。
- 不直接在舊 JSF、Spring 4、Spring Web Flow 與 XML 架構中逐檔改寫。
- 舊專案在 Migration 期間保持可執行，以便人工比對新舊行為。

### 2. 新專案技術

- 前端：Vue、TypeScript、Vue Router、Pinia。
- 後端：Java 21、Spring Boot、Maven、REST API。
- 資料存取：Spring Data JPA、Oracle JDBC。
- 資料庫：Oracle。
- Schema 管理：Flyway 或 Liquibase。
- 後端使用 Spring Boot 內嵌伺服器及可執行 Jar 或 Docker image，不再部署 WAR 到外部 Tomcat。

### 3. 功能範圍

目前只重新實作舊專案既有功能：

- 註冊、登入及登出。
- 個人資料讀取與修改。
- 建立、讀取及更新銀行帳戶。
- 存款、提款及轉帳。
- 依帳戶與時間查詢交易紀錄。

資料模型、驗證規則、REST API request、response、錯誤情境與商業流程只需符合舊專案現有資料及行為，不增加額外領域或 API 設計。

### 4. Oracle 資料策略

- 建立全新的空白 Oracle schema `ONLINEBANKINGSYSTEM`。
- 複製舊專案 schema 的資料表結構、欄位、型別、主鍵、外鍵及唯一鍵。
- 不複製、不匯入及不移轉任何舊資料。
- 新舊專案不可共用可寫入的 schema 或 Oracle data volume。
- 除資料表名稱格式調整外，目前不增加新資料表、欄位、索引或 constraint。

資料表名稱統一改成全大寫與底線格式：

| 舊資料表 | 新資料表 |
| --- | --- |
| `bankuser` | `BANK_USER` |
| `account` | `ACCOUNT` |
| `transactionrec` | `TRANSACTION_REC` |

### 5. 不在目前實作範圍

下列項目列為後續優化：

- 重寫或強化認證與安全機制。
- 自動化測試及完整測試架構。
- 狀態欄位、版本欄位、reference number、locking 或 idempotency。
- API 版本化、額外查詢條件、分頁或統一回應包裝。
- 舊專案不存在的新功能或 API。

目前認證只需維持舊專案的註冊、登入、登出與 BCrypt 驗證行為。目前階段以啟動確認及新舊功能人工比對為主。

## 二、Migration 方式分析

推薦以舊專案作為規格，建立全新的 Vue 與 Spring Boot 專案，再依功能垂直切片重新實作。

舊系統約有 25 個 Java 類別、3 個主要資料表，以及註冊、登入、帳戶、存提款、轉帳與交易紀錄等功能。若直接改造舊專案，會同時受到 JSF、Spring Web Flow、Spring 4、舊 Hibernate 與 XML 設定牽制，因此新建專案較快且容易控制。

不採用以下方式：

- 不把 JSF 頁面逐頁直接改成 Vue。
- 不把 Spring XML 逐個直接改成 Spring Boot 設定。
- 不把舊 DAO 原封不動搬到新專案。
- 不讓新舊系統共用可寫入的 Oracle schema。
- 不把舊資料匯入新專案。
- 不一次重寫全部功能。

執行原則：

1. 保留並凍結舊專案。
2. 建立全新的 `OnlineBankingSystem`。
3. 每次只分析及實作一項功能。
4. 讀取舊畫面、流程、Entity、Service、DAO 與相關測試作為參考。
5. 使用相同輸入人工比對新舊系統結果。
6. 功能一致後才進入下一個切片。

## 三、Codex 同時存取兩個專案

### 建議目錄

```text
IntelliJProjects/
├── onlinebanking/              A：舊專案，只供參考
└── OnlineBankingSystem/    B：新專案，實際開發
```

### 啟動 Codex

可將共同父目錄 `IntelliJProjects` 開成 Codex workspace，或使用：

```bash
codex -C /path/to/OnlineBankingSystem \
  --add-dir /path/to/onlinebanking
```

`-C` 指定主要工作目錄，`--add-dir` 加入舊專案參考目錄。

每次工作都應明確指定：

```text
A 專案：/path/to/onlinebanking
- 僅供讀取及功能分析
- 禁止修改任何檔案

B 專案：/path/to/OnlineBankingSystem
- 所有新程式只能寫在此目錄
- 使用 Vue、TypeScript、Spring Boot、Java 21 與 Oracle
- 依照 A 專案既有資料、API 及行為重新實作
- 開始工作前先完整閱讀專案Migration.md
```

### 存取注意事項

- Codex 會搜尋及讀取當前功能相關檔案，不會一次把整個舊專案放入上下文。
- `--add-dir` 可能同時授予額外目錄寫入能力；提示中的「僅供讀取」不是機械式唯讀保護。
- 建議先提交舊專案 Git、建立唯讀副本或使用檔案權限限制，避免誤改。
- 新專案必須有自己的 Git repository、`AGENTS.md`、建置設定與啟動說明。
- 修改 workspace 以外的位置可能需要額外權限或使用者核准。

## 四、目標架構

```text
瀏覽器
  ↓ HTTPS
Vue SPA
  ↓ REST JSON
Spring Boot（內嵌伺服器）
  ↓ JDBC
Oracle
```

建議目錄：

```text
OnlineBankingSystem/
├── frontend/             Vue SPA
├── backend/              Spring Boot REST API
├── database/
│   ├── migrations/       Flyway 或 Liquibase
│   └── reference/        舊 schema 結構參考，不含舊資料
├── infra/
│   ├── compose.yaml
│   └── docker/
├── docs/
└── README.md
```

本機開發服務可規劃為：

```text
Vue Vite       localhost:5173
Spring Boot    localhost:18080
Oracle         localhost:11521
```

## 五、詳細執行步驟

### 1. 凍結及備份舊系統

1. 提交舊專案必要的原始碼修改。
2. 記錄 Java、Maven、Tomcat、Oracle 與 Docker 版本。
3. 備份舊 Oracle 資料庫，只供舊系統保存及回復。
4. 讀取舊專案 schema DDL。
5. 整理舊資料表、欄位、型別、主鍵、外鍵及唯一鍵。
6. 列出舊系統全部畫面、Service 功能、驗證規則及錯誤情境。

### 2. 建立新專案

```bash
mkdir OnlineBankingSystem
cd OnlineBankingSystem
git init
mkdir backend database infra docs
npm create vue@latest frontend
```

Vue 選擇 TypeScript、Vue Router、Pinia、ESLint 與 Prettier。

### 3. 建立 Spring Boot 後端

使用 Java 21、Spring Boot、Maven 與可執行 Jar，加入：

- Spring Web
- Spring Validation
- Spring Security
- Spring Data JPA
- Spring Boot Actuator
- Oracle JDBC `ojdbc11`
- Flyway 或 Liquibase

建議結構：

```text
backend/src/main/java/.../
├── config/
├── auth/
├── user/
├── account/
├── transaction/
├── common/
│   ├── exception/
│   └── response/
└── OnlineBankingSystemApplication.java
```

不複製舊版 Spring XML、`persistence.xml`、JSF 或 Spring Web Flow 設定。

### 4. 建立 Oracle schema migration

根據舊專案 `database.sql` 建立：

```text
backend/src/main/resources/db/migration/
├── V001__create_bank_user.sql
├── V002__create_account.sql
└── V003__create_transaction_rec.sql
```

資料庫設定使用環境變數：

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

由 migration 管理 schema，不依靠 Hibernate 自動建表，不執行會先刪表的 SQL，不提交密碼、Token 或正式連線資訊。完成後驗證新 schema 沒有舊使用者、帳戶或交易資料。

### 5. 移植資料模型

- `BankUser` 對應 `BANK_USER`，欄位沿用舊 `bankuser`。
- `BankAccount` 對應 `ACCOUNT`，欄位沿用舊 `account`。
- `TransactionRec` 對應 `TRANSACTION_REC`，欄位沿用舊 `transactionrec`。

Java 型別、關聯及驗證規則以舊專案為準，只做 Spring Boot 與 Jakarta JPA 所需的技術調整。

### 6. 建立相容舊功能的 REST API

只建立舊專案畫面及 Service 所需端點：

- 註冊、登入及登出。
- 使用者資料讀取及更新。
- 帳戶建立、讀取及更新。
- 存款、提款及轉帳。
- 依帳戶與時間查詢交易紀錄。

開始實作端點前，必須先查閱舊專案對應 Controller／Flow、Service、DAO、Entity 及畫面，確認實際 request、response、驗證與錯誤行為。

### 7. 建立 Vue 前端

```text
frontend/src/
├── api/
├── components/
├── composables/
├── layouts/
├── router/
├── stores/
├── types/
└── views/
    ├── LoginView.vue
    ├── RegisterView.vue
    ├── AccountsView.vue
    ├── AccountDetailView.vue
    ├── TransferView.vue
    └── TransactionHistoryView.vue
```

Vue 負責畫面、表單狀態、API 呼叫及使用者操作流程。餘額、轉帳、唯一性與權限等規則仍由後端驗證。

### 8. 建立本機 Docker 環境

- 以 `compose.yaml` 管理 Oracle、Spring Boot、network、volume 與 health check。
- 新專案使用新的 Oracle volume。
- 不與舊 Oracle 容器共用 data volume。
- 新 schema 只複製舊 schema 結構，不匯入舊資料。

### 9. 正式部署

```text
Nginx / Load Balancer
├── /              Vue 靜態檔案
└── /api           Spring Boot container
                       ↓
                   Oracle Database
```

Spring Boot 使用可執行 Jar 或 Docker image，不部署 WAR 到外部 Tomcat。

## 六、功能實作順序

```text
建立新專案骨架
→ 建立全新 Oracle schema migration
→ 註冊與登入
→ 帳戶
→ 存款與提款
→ 轉帳
→ 交易紀錄與個人資料
→ 新舊系統人工比對
→ 確認新資料庫沒有舊資料
→ 正式切換
→ 舊系統轉為唯讀並在確認後下線
```

每個功能切片：

1. 完整閱讀本文件。
2. 分析舊系統對應畫面、Flow／Controller、Service、DAO、Entity 與資料表。
3. 在新後端實作相同行為。
4. 在 Vue 實作相同操作流程。
5. 啟動新舊系統，使用同一組輸入人工比對結果。
6. 記錄差異及已完成範圍。
7. 確認功能一致後才進入下一個切片。

## 七、目前階段驗收標準

- 新專案可獨立建置及啟動。
- 新 Oracle schema 可由 migration 從空白建立。
- Schema 結構符合舊專案，資料表名稱符合全大寫與底線規則。
- 新資料庫不含任何舊專案資料。
- 舊專案既有畫面操作在 Vue 有對應流程。
- REST API 的資料、驗證、錯誤情境及商業行為符合舊專案。
- 不加入本文件明確排除的額外設計。

## 八、後續優化

### 認證與安全

- HttpOnly、Secure 及 SameSite cookie。
- CSRF 防護。
- 登入失敗次數限制。
- CORS 限制。
- 密碼與 PIN 安全策略。
- 權限模型及安全稽核。

### 自動化測試

- 後端 Service 單元測試。
- Repository 與 REST API 整合測試。
- Spring Security 測試。
- Migration 測試。
- 轉帳與提款併發測試。
- Vue Vitest 元件測試。
- API mock 測試。
- Playwright E2E 測試。

以上優化項目不納入目前 Migration 實作範圍，除非使用者之後明確調整範圍。

## 九、原始文件

- `Codex存取2個專案注意事項.md`：跨專案存取方式及權限注意事項。
- `專案移轉分析.md`：新建專案與直接改造舊專案的比較及結論。
- `專案移轉規劃.md`：技術架構、資料策略及詳細步驟。


## 十、獨立專案更名決策（2026-10-21）

本節為最新決策，優先於前文歷史描述。專案目錄為 `/Users/jay/Documents/IntelliJProjects/OnlineBankingSystem`，專案名稱、Maven artifact/name 與 Spring application name 為 `OnlineBankingSystem`；Java 基礎 package 為 `com.OnlineBankingSystem`，啟動類為 `OnlineBankingSystemApplication`，Jar 為 `OnlineBankingSystem-0.0.1-SNAPSHOT.jar`。npm package 與 Compose project 依工具命名規則使用小寫 `onlinebankingsystem`。Oracle schema 為 `ONLINEBANKINGSYSTEM`，獨立 volume 預設為 `onlinebankingsystem_oracle_data`，不得連用來源專案的資料庫 volume。

頁首、頁尾、瀏覽器標題與路由預設標題依使用者要求統一為 `OnlineBankingSystem`。已查閱唯讀舊系統共用 `general.xhtml`、`screen.css` 及目前資源來源說明；品牌文字是本次需求指定的差異，保留既有版型、欄位、CSS 與未修改圖片。

副本保留全部程式、資源及功能文件，原版本控制資料、IDE VCS 狀態與舊建置產物不沿用；以新名稱重新建置並建立全新 Git root commit，GitHub 目標為 `https://github.com/jayliu005/OnlineBankingSystem`。過往功能紀錄僅作歷史參考，不代表此副本已連接或啟動資料庫；本次不操作任何既有容器或資料。
