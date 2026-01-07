# 內建倉頡輸入法資源檔 (qhcj.cin) 使用指南

## 概述

專案已內建 qhcj.cin（倉頡輸入法查表檔），包含完整的倉頡字根定義。此檔案位於 `app/src/main/assets/qhcj.cin`，可在應用中直接載入使用。

## 檔案資訊

- **檔案名稱**: qhcj.cin
- **位置**: `app/src/main/assets/qhcj.cin`
- **格式**: CIN (Chinese Input Method) 格式
- **編碼**: UTF-8
- **字符數**: 約 27,000+ 個字符定義
- **輸入法名稱**: 倉頡 (CangJie)

## 元數據

```
%ename CangJie
%cname 倉頡
%tname 倉頡
%sname 仓颉
%encoding UTF-8
%selkey 1234567890
%space_style 4
```

## 使用方式

### 1. 基本載入

```kotlin
// 建立 TableLoader
val loader = TableLoader(context)

// 從 assets 載入內建倉頡
val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)

// 使用 TableManager 載入
val tableManager = TableManager()
tableManager.loadTable("cangjie", inputStream)
```

### 2. 使用 ViewModel（推薦）

```kotlin
class CangjieViewModel : ViewModel() {
    private val tableManager = TableManager()
    val tableState: LiveData<TableLoadState> = tableManager.state
    
    fun loadBuiltinCangjie(context: Context) {
        val loader = TableLoader(context)
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        tableManager.loadTable("cangjie", inputStream)
    }
    
    fun getCandidates(code: String): List<Char>? {
        return tableManager.getCandidates(code)
    }
}
```

### 3. 在 Activity/Fragment 中使用

```kotlin
class MainActivity : AppCompatActivity() {
    private val viewModel: CangjieViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 載入內建倉頡
        viewModel.loadBuiltinCangjie(this)
        
        // 監聽載入狀態
        viewModel.tableState.observe(this) { state ->
            when (state) {
                is TableLoadState.Loading -> {
                    // 顯示載入中
                }
                is TableLoadState.Success -> {
                    // 載入成功
                    val result = state.data
                    println("載入成功: ${result.chineseName}")
                    println("字符總數: ${result.totalChars}")
                }
                is TableLoadState.Error -> {
                    // 載入失敗
                    println("錯誤: ${state.message}")
                }
            }
        }
        
        // 查詢候選字
        val candidates = viewModel.getCandidates("a") // 查詢「日、曰」等字
    }
}
```

## 查詢範例

### 查詢候選字（編碼 → 字符）

```kotlin
// 查詢編碼 "a" 的候選字
val candidates = tableManager.getCandidates("a")
// 結果: [日, 曰]

// 查詢編碼 "aa" 的候選字
val candidates = tableManager.getCandidates("aa")
// 結果: [昌, 昍]

// 查詢編碼 "aaa" 的候選字
val candidates = tableManager.getCandidates("aaa")
// 結果: [晶, 晿]
```

### 反向查詢（字符 → 編碼）

```kotlin
// 查詢「日」的編碼
val code = tableManager.getCode('日')
// 結果: "a"

// 查詢「昌」的編碼
val code = tableManager.getCode('昌')
// 結果: "aa"

// 查詢「晶」的編碼
val code = tableManager.getCode('晶')
// 結果: "aaa"
```

## 整合測試

整合測試位於 `app/src/androidTest/java/integration/QhcjCINIntegrationTest.kt`，可驗證：

1. 從 assets 載入 qhcj.cin
2. 正確解析 CIN 格式
3. 元數據解析正確
4. 候選字查詢功能
5. 反向編碼查詢功能

執行整合測試：

```bash
./gradlew connectedAndroidTest
```

## 效能考量

- **首次載入**: 約 1-3 秒（背景執行緒）
- **快取機制**: 載入後自動快取，避免重複解析
- **記憶體使用**: 約 3-5 MB（視字符定義數量）
- **查詢速度**: O(1) 時間複雜度（HashMap）

## 支援的 CIN 指令

CINParser 已支援以下 CIN 指令：

- `%gen_inp` - 輸入法產生器
- `%ename` - 英文名稱
- `%cname` - 繁體中文名稱
- `%tname` - 繁體中文名稱（別名）
- `%sname` - 簡體中文名稱
- `%encoding` - 編碼格式
- `%selkey` - 選字鍵
- `%space_style` - 空白鍵行為
- `%keyname begin/end` - 字根名稱定義
- `%chardef begin/end` - 字符定義區段

## 檔案結構

```
qhcj.cin
├── 元數據定義 (%ename, %cname, etc.)
├── %keyname begin
│   └── 字根定義 (a=日, b=月, etc.)
├── %keyname end
├── %chardef begin
│   └── 字符定義 (27,000+ 行)
└── %chardef end
```

## 常見字根對應

| 字根 | 鍵位 | 範例字 |
|-----|-----|-------|
| 日 | a | 日、明、時 |
| 月 | b | 月、朋、肥 |
| 金 | c | 金、銀、銅 |
| 木 | d | 木、林、森 |
| 水 | e | 水、江、河 |
| 火 | f | 火、炎、燒 |
| 土 | g | 土、地、場 |
| 竹 | h | 竹、笑、笛 |
| 戈 | i | 戈、成、戰 |
| 十 | j | 十、古、千 |

## 故障排除

### 載入失敗

如果載入失敗，檢查：
1. `qhcj.cin` 是否存在於 `app/src/main/assets/`
2. 檔案編碼是否為 UTF-8
3. 檔案權限是否正確
4. 使用 `retry()` 方法重試

### 查詢結果為空

1. 確認已載入成功（狀態為 `TableLoadState.Success`）
2. 檢查編碼是否正確（倉頡使用 a-z 鍵位）
3. 確認字符是否在查表檔中

## 相關檔案

- `app/src/main/assets/qhcj.cin` - 倉頡 CIN 檔案
- `app/src/main/java/cin/CINParser.kt` - CIN 解析器
- `app/src/main/java/table/TableManager.kt` - 查表管理器
- `app/src/main/java/table/TableLoader.kt` - 檔案載入器
- `app/src/main/java/sample/CangjieViewModel.kt` - 示例 ViewModel
- `app/src/androidTest/java/integration/QhcjCINIntegrationTest.kt` - 整合測試

## 授權與來源

qhcj.cin 為開源倉頡輸入法查表檔，使用前請確認授權條款。

---

**更新日期**: 2026-01-07  
**版本**: 1.2

