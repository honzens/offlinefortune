# OfflineFortune（完全離線 Android Java 專案）

## 功能
- Android 原生 Java 專案，無第三方網路 SDK。
- SQLite 本機資料庫。
- 首次啟動由 APK assets/database/seed_data.json 匯入資料。
- 可選籤詩系統、瀏覽籤號、查看原文、分類解釋與典故。
- 開啟籤詩時寫入本機歷史紀錄。

## 開啟方式
1. 解壓縮 ZIP。
2. 使用 Android Studio 選擇 `Open`，開啟 `OfflineFortune` 資料夾。
3. 使用 JDK 17。
4. 安裝 Android SDK Platform 36。
5. 等待 Gradle Sync，執行 app。

專案使用 Android Gradle Plugin 8.13.2 與 Gradle 8.13。若 Android Studio 提示下載 Gradle Wrapper，允許下載；也可在 Android Studio 的 Gradle 設定選擇相容的 Gradle 8.13。

## 籤詩資料
`app/src/main/assets/database/seed_data.json` 是內建資料來源。
目前只有兩筆「示範資料」，不是正式廟宇籤詩。請替換成已核對且可合法使用的籤詩、解釋與典故。
JSON 結構請保持 deities、categories、systems、poems、interpretations、story 欄位相容。

## 離線與權限
Manifest 沒有 INTERNET 權限，也沒有網路服務程式碼。所有查詢與紀錄都在裝置 SQLite 執行。

## 資料更新
目前 seed version 為 1。新增或修改內建資料時，請調整 `DBInitializer.DATA_VERSION`。
正式發布前應加入資料庫 migration 與匯入失敗回復測試。
