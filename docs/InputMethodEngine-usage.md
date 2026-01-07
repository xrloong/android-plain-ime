# InputMethodEngine 使用指南

## 概述

InputMethodEngine 是輸入法引擎的核心元件，負責處理輸入邏輯與查表。本指南將說明如何使用 InputMethodEngine 和 InputMethodEngineManager。

---

## 架構

```
InputMethodEngineManager (整合層)
    ├── TableManager (查表管理)
    │   └── CINParser (解析器)
    └── InputMethodEngine (輸入邏輯)
```

---

## 快速開始

### 1. 基本設置

```kotlin
// 建立 Manager
val manager = InputMethodEngineManager()

// 監聽狀態
manager.engineState.observe(this) { state ->
    when (state) {
        is EngineState.Loading -> {
            // 顯示載入中
        }
        is EngineState.Ready -> {
            // 準備就緒，可以開始輸入
            println("輸入法：${state.inputMethodName}")
            println("字符數：${state.totalChars}")
        }
        is EngineState.Error -> {
            // 顯示錯誤
            println("錯誤：${state.message}")
            if (state.canRetry) {
                // 可以重試
            }
        }
    }
}
```

### 2. 載入輸入法

```kotlin
// 從 assets 載入內建倉頡
val loader = TableLoader(context)
val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
manager.loadInputMethod("cangjie", inputStream)
```

### 3. 處理輸入

```kotlin
// 輸入按鍵
val success = manager.processKey('a')
if (success) {
    // 獲取當前編碼
    val code = manager.getCurrentCode()
    println("當前編碼：$code")
    
    // 獲取候選字
    val candidates = manager.getCandidates()
    println("候選字：$candidates")
}
```

### 4. 選擇候選字

```kotlin
// 方法 1: 根據索引選擇
val selected = manager.selectCandidate(0)
if (selected != null) {
    commitText(selected.toString())
}

// 方法 2: 根據選字鍵選擇（1-9, 0）
val selected = manager.selectCandidateByKey('1')
if (selected != null) {
    commitText(selected.toString())
}
```

---

## 完整輸入流程

### 情境 1：輸入「日」字

```kotlin
// 1. 輸入 'a'
manager.processKey('a')
// getCurrentCode() = "a"
// getCandidates() = [日, 曰]

// 2. 選擇第一個候選字
val char = manager.selectCandidate(0)
// char = '日'

// 3. 上屏
commitText(char.toString())
// getCurrentCode() = "" (已清除)
```

### 情境 2：使用選字鍵

```kotlin
// 1. 輸入 'a'
manager.processKey('a')

// 2. 按 '1' 鍵選擇第一個候選字
val char = manager.selectCandidateByKey('1')

// 3. 上屏
commitText(char.toString())
```

### 情境 3：輸入「昌」字

```kotlin
// 1. 輸入 'a'
manager.processKey('a')
// getCandidates() = [日, 曰]

// 2. 輸入第二個 'a'
manager.processKey('a')
// getCurrentCode() = "aa"
// getCandidates() = [昌]

// 3. 選擇候選字
val char = manager.selectCandidate(0)
// char = '昌'

// 4. 上屏
commitText(char.toString())
```

### 情境 4：使用 Commit 直接上屏

```kotlin
// 1. 輸入編碼
manager.processKey('a')
manager.processKey('a')

// 2. 直接 commit（自動選擇第一個候選字）
val text = manager.commit()
// text = "昌"

// 3. 上屏
if (text != null) {
    commitText(text)
}
```

---

## API 參考

### InputMethodEngineManager

#### 載入與管理
```kotlin
// 載入輸入法
fun loadInputMethod(key: String, inputStream: InputStream)

// 重試載入
fun retry()

// 清除快取
fun clearCache()

// 監聽狀態
val engineState: LiveData<EngineState>
```

#### 輸入處理
```kotlin
// 處理按鍵（a-z）
fun processKey(key: Char): Boolean

// 獲取當前編碼
fun getCurrentCode(): String

// 獲取候選字列表
fun getCandidates(): List<Char>
```

#### 選字功能
```kotlin
// 根據索引選擇（0-based）
fun selectCandidate(index: Int): Char?

// 根據選字鍵選擇（1-9, 0）
fun selectCandidateByKey(selectionKey: Char): Char?
```

#### 編輯操作
```kotlin
// 刪除最後一個編碼字符
fun backspace(): Boolean

// 清除所有輸入
fun clear()

// 確認輸入（上屏第一個候選字或編碼本身）
fun commit(): String?
```

#### 狀態查詢
```kotlin
// 是否有輸入中的編碼
fun hasInput(): Boolean

// 是否有候選字
fun hasCandidates(): Boolean
```

### EngineState

```kotlin
sealed class EngineState {
    // 載入中
    object Loading
    
    // 就緒（可以輸入）
    data class Ready(
        val inputMethodName: String,  // 輸入法名稱
        val totalChars: Int            // 總字符數
    )
    
    // 錯誤
    data class Error(
        val message: String,           // 錯誤訊息
        val canRetry: Boolean          // 是否可重試
    )
}
```

---

## 進階使用

### 處理鍵盤事件

```kotlin
fun handleKeyPress(key: String) {
    when {
        // 字母鍵
        key.length == 1 && key[0] in 'a'..'z' -> {
            if (manager.processKey(key[0])) {
                updateUI()
            }
        }
        
        // 選字鍵
        key in "1234567890" -> {
            val selected = manager.selectCandidateByKey(key[0])
            if (selected != null) {
                commitText(selected.toString())
            }
        }
        
        // Backspace
        key == "BACKSPACE" -> {
            if (manager.hasInput()) {
                manager.backspace()
                updateUI()
            } else {
                // 刪除已輸入的文字
                deleteText(1)
            }
        }
        
        // Enter/Space
        key == "ENTER" || key == "SPACE" -> {
            val text = manager.commit()
            if (text != null) {
                commitText(text)
            }
        }
        
        // Escape
        key == "ESCAPE" -> {
            manager.clear()
            updateUI()
        }
    }
}

fun updateUI() {
    val code = manager.getCurrentCode()
    val candidates = manager.getCandidates()
    
    // 更新編碼顯示
    codeTextView.text = code
    
    // 更新候選字列表
    candidateAdapter.submitList(candidates)
}
```

### 多輸入法切換

```kotlin
class InputMethodSwitcher {
    private val manager = InputMethodEngineManager()
    
    fun switchTo(inputMethod: String) {
        val loader = TableLoader(context)
        val inputStream = when (inputMethod) {
            "cangjie" -> loader.loadFromAssets("qhcj.cin")
            "array" -> loader.loadFromAssets("array.cin")
            "boshiamy" -> loader.loadFromAssets("boshiamy.cin")
            else -> return
        }
        
        manager.loadInputMethod(inputMethod, inputStream)
    }
}
```

### 錯誤處理

```kotlin
manager.engineState.observe(this) { state ->
    when (state) {
        is EngineState.Error -> {
            // 顯示錯誤訊息
            showErrorDialog(
                message = state.message,
                onRetry = if (state.canRetry) {
                    { manager.retry() }
                } else {
                    null
                }
            )
        }
        else -> { /* ... */ }
    }
}
```

---

## 測試

### 單元測試範例

```kotlin
@Test
fun testInputFlow() {
    // 建立測試 Engine
    val tableData = createTestTableData()
    val engine = InputMethodEngine(tableData)
    
    // 測試輸入
    engine.processKey('a')
    assertEquals("a", engine.getCurrentCode())
    
    // 測試候選字
    val candidates = engine.getCandidates()
    assertTrue(candidates.isNotEmpty())
    
    // 測試選字
    val selected = engine.selectCandidate(0)
    assertNotNull(selected)
    assertEquals("", engine.getCurrentCode())
}
```

---

## 效能考量

### 查詢效能
- 候選字查詢時間：O(1)（HashMap）
- 編碼緩衝區操作：O(1)

### 記憶體使用
- Engine 本身：< 1 KB
- 查表資料：3-5 MB（取決於輸入法）

### 最佳實踐
1. **重用 Engine**：不要每次輸入都建立新 Engine
2. **即時清理**：選字或 commit 後自動清除狀態
3. **狀態監聽**：使用 LiveData 監聽狀態變化

---

## 常見問題

### Q: 如何處理無候選字的情況？
A: 使用 `hasCandidates()` 檢查，或 `commit()` 會自動上屏編碼本身。

```kotlin
if (manager.hasCandidates()) {
    // 顯示候選字列表
} else {
    // 提示無候選字
}
```

### Q: 如何實作自動上屏？
A: 使用 `commit()` 方法，會自動選擇第一個候選字。

```kotlin
val text = manager.commit()
if (text != null) {
    commitText(text)
}
```

### Q: 選字鍵如何映射？
A: 1-9 對應索引 0-8，0 對應索引 9。

```kotlin
'1' -> index 0
'2' -> index 1
...
'9' -> index 8
'0' -> index 9
```

### Q: 如何處理大小寫？
A: Engine 會自動將大寫轉為小寫。

```kotlin
engine.processKey('A')  // 自動轉為 'a'
```

---

## 相關文件

- [Phase 1.3 完成報告](phase-1.3-completion-report.md)
- [qhcj.cin 使用指南](qhcj-cin-usage.md)
- [測試指南](testing-guide.md)

---

**更新日期**: 2026-01-07  
**版本**: 1.3

