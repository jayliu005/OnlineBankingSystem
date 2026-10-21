# 個人資料查詢／修改 Migration

## 舊系統分析

此功能切片以舊專案下列內容作為唯讀行為基準：

- `userInfo.xhtml`：個人資料欄位、編輯表單、驗證與 Update User Profile 操作。
- `userInfoUpdated.xhtml`：更新成功後的個人資料摘要與返回首頁／登出操作。
- `account-flow.xml`：`userInfo` 與 `infoUpdated` 流程。
- `UserServiceImpl.updateUserInfo`：以目前登入使用者的 `BankUser` 物件更新資料。
- `BankUser`：欄位型別與 Bean Validation 規則。

## 舊系統行為

- 只能讀取與修改目前登入使用者的個人資料。
- `userName`、密碼及使用者 ID 不在編輯範圍。
- 可編輯 First Name、Last Name、Middle Initial、Gender、Date of Birth、Street、City、State、Zip Code、Phone 與 Email。
- First／Last Name 長度為 `2–50`；Middle Initial 最多 1 字元。
- Gender 使用 `M` 或 `F`；出生日期必須是過去日期。
- Street 長度為 `2–100`；City／State 長度為 `2–40`。
- Zip 使用五碼或 ZIP+4 格式；Phone 與 Email 遵循註冊時相同格式驗證。
- 更新成功後顯示 Full Name、Gender、Date of Birth、Address、Phone 與 Email。

## 新系統實作

### REST API

| Method | Path | 行為 |
| --- | --- | --- |
| `GET` | `/api/user/profile` | 讀取目前登入使用者完整個人資料，不回傳密碼 |
| `PUT` | `/api/user/profile` | 驗證並更新目前登入使用者可編輯欄位 |

`PUT` request 不包含 `userName`、ID 或密碼；更新成功回傳完整 profile response，`middleInitial` 空白時儲存為 `NULL`。

### 後端

- `UserProfileService` 以 session principal 的 user name 查詢使用者，避免透過 request body 指定他人 ID。
- `BankUser.updateProfile` 只更新舊畫面允許編輯的欄位。
- 使用 Jakarta Bean Validation 複製註冊畫面的欄位長度、日期、Zip、Phone、Email 與 Gender 規則。
- Profile response 不包含 BCrypt 密碼或帳戶資料。

### Vue

- 新增 `/user/profile`，帳戶首頁 User Profile 按鈕已啟用。
- 畫面沿用舊系統 `Please Edit Your Profile` 欄位順序、綠色 fieldset、Cancel 與 Update User Profile 操作。
- 更新成功後沿用 `User Profile updated successfully!` 摘要畫面，提供 Full Name、Gender、Date of Birth、Address、Phone、Email、Back to Home 與 Logout。
- 載入與更新錯誤會顯示 API message 及欄位錯誤。

## 驗證結果

- Java 21 `mvn test` 成功，共 10 筆測試通過。
- Java 21 `mvn package -DskipTests` 成功產生新版 Jar。
- Node 24 format、lint、型別檢查及 production build 全數成功。
- Oracle 23 Free 通過 Flyway 與 Hibernate schema validation。
- 實際註冊測試使用者後，GET profile 回傳完整資料；PUT 修改姓名、Middle Initial、性別、出生日期、地址、電話與 Email 成功，空白 Middle Initial 儲存為 `NULL`。
- 非法姓名、Gender、Email 與 Phone 回傳 `400`；未登入讀取與修改回傳 `401`；Vite proxy 讀取回傳 `200`。
- 驗證用使用者、Cookie 與資料已清除，資料庫恢復為原有 `1:0:0`。

## 差異與後續

- JSF inplace editor 與 Web Flow success state 改為 Vue 表單狀態與 route，欄位、文案、驗證與成功摘要維持一致。
- 新系統以獨立 profile response 隔離密碼欄位，且由 principal 強制限制本人資料；這是 REST 實作所需的安全邊界，不改變畫面行為。
- 本階段不加入修改密碼、頭像、額外個資欄位或審計紀錄。
