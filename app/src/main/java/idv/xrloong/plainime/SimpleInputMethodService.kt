package idv.xrloong.plainime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import engine.EngineState
import engine.InputMethodEngineManager
import table.TableLoader
import ui.InputMethodView
import ui.keyboard.KeyboardState

/**
 * 簡單輸入法服務
 *
 * 整合 InputMethodView 與 InputMethodEngineManager
 */
class SimpleInputMethodService : InputMethodService() {

    private lateinit var inputMethodView: InputMethodView
    private val engineManager = InputMethodEngineManager()

    override fun onCreate() {
        super.onCreate()

        // 監聽 Engine 狀態
        engineManager.engineState.observeForever { state ->
            handleEngineStateChange(state)
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

        return inputMethodView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // 更新 UI 狀態
        updateUI()
    }

    private fun loadInputMethod() {
        try {
            val loader = TableLoader(this)
            val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
            engineManager.loadInputMethod("cangjie", inputStream)
        } catch (e: Exception) {
            // 處理載入錯誤
            inputMethodView.setKeyboardState(
                KeyboardState.Error("無法載入輸入法: ${e.message}", true)
            )
        }
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "BACKSPACE" -> handleBackspace()
            "SPACE" -> handleSpace()
            "ENTER" -> handleEnter()
            else -> handleCharacterKey(key)
        }
        updateUI()
    }

    private fun handleCharacterKey(key: String) {
        if (key.length == 1 && key[0].lowercaseChar() in 'a'..'z') {
            engineManager.processKey(key[0].lowercaseChar())
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
            // 無編碼時，發送 Enter 鍵
            sendDefaultEditorAction(true)
        }
    }

    private fun handleCandidateSelection(index: Int) {
        val selected = engineManager.selectCandidate(index)
        if (selected != null) {
            currentInputConnection?.commitText(selected.toString(), 1)
        }
        updateUI()
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
