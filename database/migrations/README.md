# Schema migrations

應用程式執行的 Flyway migration 位於：

`backend/src/main/resources/db/migration`

此處不保留重複 SQL，避免兩份 migration 發生差異。新 schema 僅建立結構，不包含舊系統資料。
