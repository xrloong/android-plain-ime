package idv.xrloong.plainime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import engine.EngineState
import engine.InputMethodEngineManager
import ime.InputMethodManager
import ime.InputMethodState
import table.TableLoader
import ui.InputMethodView
import ui.keyboard.KeyboardLayout
import ui.keyboard.KeyboardState

/**
 * 簡單輸入法服務
 *
 * 整合 InputMethodView 與 InputMethodEngineManager
 */
class SimpleInputMethodService : InputMethodService() {

    private lateinit var inputMethodView: InputMethodView
    private val engineManager = InputMethodEngineManager()
    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate() {
        super.onCreate()

        // 初始化輸入法管理器
        inputMethodManager = InputMethodManager(this)

        // 監聽 Engine 狀態
        engineManager.engineState.observeForever { state ->
            handleEngineStateChange(state)
        }

        // 監聽輸入法狀態（切換、載入、錯誤）
        inputMethodManager.currentMethod.observeForever { state ->
            handleInputMethodStateChange(state)
        }
    }

    override fun onCreateInputView(): View {
        // 建立輸入法視圖
        inputMethodView = InputMethodView(this)

        // 設置監聽器
        inputMethodView.setInputMethodListener(object : InputMethodView.InputMethodListener {
            override fun onKeyPressed(key: String) {
                handleKeyPress(key)
            }

            override fun onKeyLongPressed(key: String, alternatives: List<String>) {
                // TODO: 顯示彈窗選擇替代字符
            }

            override fun onCandidateSelected(candidate: Char, index: Int) {
                handleCandidateSelection(index)
            }

            override fun onRetryRequested() {
                loadInputMethod()
            }
        })

        // 載入輸入法
        loadInputMethod()

        // 確保視圖能夠接收 Window Insets（例如系統導航欄）
        inputMethodView.fitsSystemWindows = true

        return inputMethodView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // 更新 UI 狀態
        updateUI()
    }

    private fun loadInputMethod() {
        // InputMethodManager handles loading automatically in onCreate()
        // This method is called by onRetryRequested to retry failed load
        inputMethodManager.retry()
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "BACKSPACE" -> handleBackspace()
            "SPACE" -> handleSpace()
            "ENTER" -> handleEnter()
            "LAYOUT_TOGGLE" -> handleLayoutToggle()
            "SHIFT" -> handleShift()
            "COMMA" -> handleComma()
            "GLOBE" -> handleGlobe()
            "PERIOD" -> handlePeriod()
            else -> handleCharacterKey(key)
        }
        updateUI()
    }

    private fun handleCharacterKey(key: String) {
        val currentLayout = inputMethodView.getCurrentKeyboardLayout()

        if (key.length == 1 && key[0].lowercaseChar() in 'a'..'z') {
            // Check if in Cangjie mode - use engine for Cangjie encoding
            // In English mode - directly insert the letter
            if (currentLayout is ui.keyboard.KeyboardLayout.Cangjie) {
                engineManager.processKey(key[0].lowercaseChar())
            } else {
                currentInputConnection?.commitText(key, 1)
            }
        } else if (key.isNotEmpty()) {
            // 直接輸入字符（標點符號、數字等）
            currentInputConnection?.commitText(key, 1)
        }
    }

    private fun handleBackspace() {
        if (engineManager.hasInput()) {
            engineManager.backspace()
        } else {
            // 刪除已輸入的文字
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace() {
        if (engineManager.hasInput()) {
            // 有編碼時，上屏第一個候選字或編碼本身
            val text = engineManager.commit()
            if (text != null) {
                currentInputConnection?.commitText(text, 1)
            }
        } else {
            // 無編碼時，輸入空格
            currentInputConnection?.commitText(" ", 1)
        }
    }

    private fun handleEnter() {
        if (engineManager.hasInput()) {
            // 有編碼時，上屏第一個候選字或編碼本身
            val text = engineManager.commit()
            if (text != null) {
                currentInputConnection?.commitText(text, 1)
            }
        } else {
            // 無編碼時，插入換行
            currentInputConnection?.commitText("\n", 1)
        }
    }

    private fun handleLayoutToggle() {
        inputMethodView.toggleKeyboardLayout()
    }

    private fun handleShift() {
        // TODO: Implement shift functionality (uppercase/symbols toggle)
        // For now, do nothing - placeholder for future enhancement
    }

    private fun handleComma() {
        // Insert comma
        currentInputConnection?.commitText("，", 1)
    }

    private fun handleGlobe() {
        val currentLayout = inputMethodView.getCurrentKeyboardLayout()

        if (currentLayout is KeyboardLayout.Cangjie) {
            // 在中文鍵盤模式下 - 切換到下一個中文輸入法
            inputMethodManager.switchToNext()
        } else {
            // 從英文/標點模式切回中文
            inputMethodView.toggleInputMethod()
        }
    }

    private fun handlePeriod() {
        // Insert period
        currentInputConnection?.commitText("。", 1)
    }

    private fun handleCandidateSelection(index: Int) {
        val selected = engineManager.selectCandidate(index)
        if (selected != null) {
            currentInputConnection?.commitText(selected.toString(), 1)
        }
        updateUI()
    }

    private fun handleInputMethodStateChange(state: InputMethodState) {
        if (!::inputMethodView.isInitialized) return

        when (state) {
            is InputMethodState.Loading -> {
                inputMethodView.setKeyboardState(KeyboardState.Loading)
            }
            is InputMethodState.Ready -> {
                // 更新引擎的表格數據
                engineManager.updateTableData(state.data)
                // 清除編碼緩衝區（防止不同輸入法間編碼混淆）
                engineManager.clear()
                // 更新鍵盤字根標籤
                inputMethodView.updateKeyboardRootLabels(state.data.keyNameMap)
                // 更新空白鍵標籤（顯示輸入法名稱）
                val metadata = inputMethodManager.getCurrentMetadata()
                if (metadata != null) {
                    inputMethodView.updateSpaceBarLabel(metadata.displayName)
                }
                inputMethodView.setKeyboardState(KeyboardState.Normal)
            }
            is InputMethodState.Error -> {
                inputMethodView.setKeyboardState(
                    KeyboardState.Error(state.message, canRetry = true)
                )
            }
        }
    }

    private fun handleEngineStateChange(state: EngineState) {
        if (!::inputMethodView.isInitialized) return

        val keyboardState = when (state) {
            is EngineState.Loading -> KeyboardState.Loading
            is EngineState.Ready -> KeyboardState.Normal
            is EngineState.Error -> KeyboardState.Error(state.message, state.canRetry)
        }

        inputMethodView.setKeyboardState(keyboardState)
    }

    private fun updateUI() {
        if (!::inputMethodView.isInitialized) return

        val code = engineManager.getCurrentCode()
        val candidates = engineManager.getCandidates()

        inputMethodView.updateCode(code)
        inputMethodView.updateCandidates(candidates)
    }
}
