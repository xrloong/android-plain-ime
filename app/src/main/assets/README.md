# 內建倉頡輸入法資源檔

## qhcj.cin

此檔案為內建的倉頡輸入法查表檔，包含完整的字根與字符定義。

### 基本資訊
- **檔案名稱**: qhcj.cin
- **輸入法名稱**: 倉頡 (CangJie)
- **編碼格式**: UTF-8
- **字符定義數**: 約 27,000+ 個
- **檔案大小**: 約 194 KB

### 元數據
- 英文名稱: CangJie
- 中文名稱: 倉頡
- 選字鍵: 1234567890
- 編碼方式: UTF-8

### 使用方式

請參考專案文件：
- `docs/qhcj-cin-usage.md` - 詳細使用指南
- `docs/qhcj-quickstart.md` - 快速開始

### 程式碼範例

```kotlin
// 載入內建倉頡
val loader = TableLoader(context)
val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
val tableManager = TableManager()
tableManager.loadTable("cangjie", inputStream)
```

### 測試

整合測試位於 `app/src/androidTest/java/integration/QhcjCINIntegrationTest.kt`

---

**引入日期**: 2026-01-07  
**狀態**: 已驗證並測試通過

