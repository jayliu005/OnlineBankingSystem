# 舊 schema 結構參考

來源為舊專案唯讀檔案及舊 Oracle schema metadata：

`/Users/jay/Documents/IntelliJProjects/onlinebanking/onlinebanking/src/database.sql`

| 舊表名 | 新表名 | 主鍵 | 外鍵／唯一鍵 |
| --- | --- | --- | --- |
| `bankuser` | `BANK_USER` | `USER_ID` | `USER_NAME` 唯一 |
| `account` | `ACCOUNT` | `ACCOUNT_ID` | `USER_ID` → `BANK_USER.USER_ID` |
| `transactionrec` | `TRANSACTION_REC` | `TRAN_ID` | `ACCOUNT_ID_FROM`、`ACCOUNT_ID_TO` → `ACCOUNT.ACCOUNT_ID` |

新 migration 保留舊欄位的 Oracle 型別、長度、nullable、主鍵、外鍵與唯一性，只將表名及欄位名統一為全大寫底線格式。舊 DDL 的 `DROP TABLE` 與任何資料均不複製。

舊 `BaseEntity` 使用未指定 generator 的 `@GeneratedValue`。唯讀檢查舊 Oracle schema 後，確認三類 Entity 共用 `HIBERNATE_SEQUENCE`，因此新 migration 以 V004 建立同名 sequence。舊 `database.sql` 未包含此物件。
