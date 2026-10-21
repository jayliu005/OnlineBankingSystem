# AI 選用模型與 Legacy Migration 提案

## 1. 建議 AI 模型組合

本提案以 OpenAI 官方模型作為具體模型基準。實際使用時應以當時可用的模型 ID、權限與專案資安政策為準，並將模型版本記錄在 PoC 執行紀錄中。

| 模型 | 建議角色 | 使用階段 | 原因 |
| --- | --- | --- | --- |
| GPT-5.5 | Legacy 分析與架構審查模型 | 專案盤點、流程萃取、跨檔案規則判斷、差異審查 | 適合複雜推理、程式設計與跨檔案專業工作 |
| GPT-5-Codex | 受控程式碼代理模型 | Spring Boot、Vue、測試與文件的增量實作 | 專為 agentic coding 工作設計，適合在明確範圍內修改 repository |
| GPT-5.4 | 高量轉換與初步審查模型 | DTO、Mapper、基本 Controller、型別、測試樣板與格式檢查 | 在程式設計與成本之間取得平衡，適合可重複且規則明確的工作 |

### 1.1 模型使用原則

- GPT-5.5 產生行為規格與轉換決策，不直接取代人工核准。
- GPT-5-Codex 只可寫入 `OnlineBankingSystem`，不得寫入 `onlinebanking`。
- GPT-5.4 只處理已由 GPT-5.5 定義規格的局部轉換，不自行推測商業規則。
- 所有模型輸出都必須經過 Java 編譯、前端檢查、資料庫驗證與人工比對。
- 不將密碼、Token、正式連線資訊或不必要的生產資料放入模型上下文。

## 2. AI 轉換流程

```text
舊專案唯讀掃描
      ↓
GPT-5.5 建立功能與行為規格
      ↓
人工確認規格與轉換邊界
      ↓
GPT-5-Codex 產生 Spring Boot／Vue 增量程式碼
      ↓
GPT-5.4 執行局部程式碼審查與測試樣板產生
      ↓
Java、Vue、Flyway 與 API 驗證
      ↓
新舊系統使用相同輸入人工比對
      ↓
修正差異並記錄至 Migration 文件與歷程
```

## 3. 各階段具體做法

### 3.1 舊專案盤點

AI 讀取下列唯讀內容，建立可追溯的來源索引：

- JSF／PrimeFaces XHTML 頁面。
- `account-flow.xml`、`main-flow.xml` 及相關 Web Flow 設定。
- Controller／Flow action、Service、DAO、Entity。
- Bean Validation、`messages.properties` 與錯誤訊息。
- `database.sql`、JPA 設定、datasource 設定與既有測試。

輸出內容：

- 畫面與功能目錄。
- `畫面 → Flow → Service → DAO → Entity → 資料表` 對照表。
- 欄位、驗證規則、錯誤訊息與權限規則清單。
- 每一項規則的來源檔案與行號。

### 3.2 行為規格萃取

GPT-5.5 對每一個功能產生一份 Migration 規格，至少包含：

- 使用者操作順序。
- Request 欄位與型別。
- Response 欄位與不應回傳的敏感欄位。
- 驗證順序與錯誤訊息。
- 登入者與資料擁有權限制。
- 交易資料異動順序。
- 舊畫面文案、欄位順序與成功頁面內容。
- 新舊系統可接受的技術差異。

規格未經人工確認前，AI 不得開始產生該功能的正式程式碼。

### 3.3 Spring Boot 後端轉換

GPT-5-Codex 依已核准的規格，在新專案產生：

- Entity 與 `BANK_USER`、`ACCOUNT`、`TRANSACTION_REC` 對應。
- Repository、Service、Request／Response DTO。
- REST Controller 與錯誤處理。
- Jakarta Bean Validation。
- Spring Security 的目前相容行為。
- `@Transactional` 的餘額與交易紀錄更新流程。
- Flyway migration 或既有 migration 的必要調整。
- Service 與 Controller 測試樣板。

AI 不得自行新增舊系統沒有的欄位、表、索引、API 版本、分頁、locking 或 idempotency。

### 3.4 Vue 前端轉換

GPT-5-Codex 依舊 XHTML、CSS、圖片及行為規格產生：

- Vue route 與 view。
- API client、TypeScript type 與 Pinia state。
- 表單欄位、錯誤訊息、成功摘要與返回流程。
- 舊版版型、fieldset、按鈕配置、文案與必要圖片資源。
- Router guard 與登入狀態處理。

Vue 只負責畫面及操作流程；餘額、PIN、帳戶歸屬、唯一性與交易規則必須由後端再次驗證。

## 4. 建議 Prompt 契約

每次模型執行都應提供固定格式的上下文：

```text
角色：你是 Legacy Java to Spring Boot/Vue Migration Engineer。
來源專案：/path/to/onlinebanking，只能讀取。
目標專案：/path/to/OnlineBankingSystem，只能在此產生修改。
目前功能：<單一功能切片>
已核准行為規格：<貼上該功能 Migration 規格>
來源檔案：<列出檔案與行號>
禁止事項：不得移轉資料、不得新增未核准領域設計、不得修改來源專案。
輸出要求：先列出假設與檔案變更，再產生程式碼，最後列出驗證指令與未解決差異。
```

模型輸出不得只提供程式碼，必須同時提供：

- 使用的來源檔案。
- 採用的行為規則。
- 變更檔案清單。
- 可能的相容性風險。
- 建議的編譯、測試與人工比對指令。

## 5. 驗證與人工審核閘門

每一個功能切片都必須通過以下閘門：

| 閘門 | 驗證內容 | 通過條件 |
| --- | --- | --- |
| G1 來源分析 | 來源檔案、Flow、Service、DAO、Entity 已列出 | 規格可追溯至舊專案 |
| G2 規格審核 | 輸入、輸出、錯誤與資料異動 | 人工確認行為規格 |
| G3 程式碼審核 | 變更範圍與安全邊界 | 只修改 OnlineBankingSystem 專案 |
| G4 編譯檢查 | Java、Vue、Flyway | Build、lint、schema validation 通過 |
| G5 行為測試 | 正常與失敗情境 | 相同輸入得到相容結果 |
| G6 文件紀錄 | Migration 文件與操作歷程 | 差異、指令與結果已記錄 |

建議基本驗證指令：

```bash
cd OnlineBankingSystem/backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean verify

cd ../frontend
npm run format
npm run lint
npm run build
```

## 6. PoC 驗收案例

PoC 以「註冊 → 建立帳戶 → 存款 → 提款／轉帳 → 查詢交易紀錄」作為端到端主案例，並補充以下失敗案例：

- 未登入存取受保護 API。
- 錯誤密碼或帳戶 PIN。
- 不屬於目前使用者的來源帳戶。
- 不存在的目的帳戶。
- 金額超過舊系統上限。
- 餘額不足。
- 同一帳戶轉帳。
- 交易日期範圍倒置。

驗收結果應同時記錄 HTTP status、畫面訊息、資料庫餘額、交易紀錄數量與新舊差異。

## 7. 風險與控制措施

| 風險 | 控制措施 |
| --- | --- |
| AI 遺漏隱藏在 XML 或 Service 的規則 | 要求輸出來源檔案與行號，並由人工審核規格 |
| AI 產生看似合理但不相容的 API | 先建立行為規格，再以相同輸入比對舊系統 |
| AI 誤改舊專案 | 舊專案唯讀、目錄權限隔離、每次檢查 Git status |
| AI 洩漏密碼或生產資料 | 輸入前遮罩秘密，只使用空白新 schema 或合成測試資料 |
| AI 擴大設計範圍 | Prompt 固定禁止新增欄位、API 與架構功能 |
| 轉帳或餘額邏輯產生資料錯誤 | 使用獨立 Oracle、交易邊界驗證與資料庫結果檢查 |
| 模型版本變更造成輸出差異 | 記錄模型 ID、日期、Prompt 版本與輸出摘要 |

## 8. 結論與建議

建議採用 GPT-5.5、GPT-5-Codex 與 GPT-5.4 的分工模式，將 AI 定位為「可追溯的分析與程式碼協作工具」，而不是一次性自動轉換器。

第一個實作 PoC 可選擇「註冊、登入與登出」作為垂直切片，因為它同時涵蓋 JSF 表單、Web Flow、Spring Security、Service、DAO、密碼雜湊、Session、錯誤訊息與 Vue Router guard。成功後再依序處理帳戶、存提款、轉帳、交易紀錄與個人資料。

本提案不改變既定 Migration 邊界：`onlinebanking` 維持唯讀、`OnlineBankingSystem` 承接所有新程式碼、新資料庫保持空白且獨立，並以本機建置與新舊行為比對作為完成標準。

## 9. 官方模型參考

- [OpenAI Models](https://developers.openai.com/api/docs/models)
- [GPT-5.5](https://developers.openai.com/api/docs/models/gpt-5.5)
- [GPT-5-Codex](https://developers.openai.com/api/docs/models/gpt-5-codex)
- [GPT-5.4](https://developers.openai.com/api/docs/models/gpt-5.4)
