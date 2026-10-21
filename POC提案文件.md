# AI 輔助 Online Banking Legacy Migration PoC 提案

## 1. 提案摘要

本 Proof of Concept（PoC）提案說明如何以 AI 模型分析舊有 `onlinebanking` JSF 專案，將其畫面、Spring Web Flow、Service、DAO、Entity、驗證規則與資料庫行為，轉換為 `OnlineBankingSystem` 的 Spring Boot 後端與 Vue 前端程式碼。

本提案採用「AI 分析與產生、人工審核、編譯驗證、新舊系統人工比對」的受控流程。AI 不直接修改舊專案，也不直接將整個專案一次轉換；每次只處理一個可驗收的功能切片。

## 2. 轉換目標

### 2.1 來源系統

`onlinebanking` 是既有 Java 8 WAR 應用程式，主要技術包含 JSF／PrimeFaces、Spring Web Flow、Spring Security、XML 設定、JPA／Hibernate、DAO、Service 與 Oracle 資料庫。主要業務表為 `bankuser`、`account`、`transactionrec`。

### 2.2 目標系統

`OnlineBankingSystem` 使用 Java 21、Spring Boot 4.1.0、REST API、Spring Data JPA、Oracle JDBC、Vue、TypeScript、Vue Router、Pinia、Vite 與 Flyway。

### 2.3 功能目標

AI 輔助轉換的功能順序如下：

1. 註冊、登入與登出。
2. 個人資料讀取與修改。
3. 帳戶建立與列表。
4. 存款與提款。
5. 轉帳。
6. 交易紀錄查詢。
7. 新舊系統人工比對與差異修正。

## 3. 預期 PoC 產出

- 每個功能切片一份 Migration 行為規格。
- Legacy source-to-target 對照矩陣。
- Spring Boot REST API、Service、Entity 與 DTO。
- Vue view、route、API client、type 與 store。
- Flyway schema migration 與 schema 驗證結果。
- 新舊系統人工比對報告。
- AI 使用紀錄：模型、Prompt、輸入檔案、輸出檔案、人工決策與驗證結果。

本文件的 AI 模型選擇、轉換流程、Prompt、審核閘門、驗收案例、風險控制與結論，已獨立整理至 [AI選用模型轉換流程及輸出入方法.md](AI選用模型轉換流程及輸出入方法.md)。
