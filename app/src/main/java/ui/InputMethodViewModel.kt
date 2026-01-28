package ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import engine.EngineState
import engine.InputMethodEngineManager
import table.TableLoader
import ui.keyboard.LayoutConfig
import ui.keyboard.LayoutConfigs

/**
 * 輸入法 ViewModel
 *
 * 負責：
 * 1. 連接 UI 與 Engine
 * 2. 處理輸入邏輯
 * 3. 管理狀態轉換
 */
class InputMethodViewModel : ViewModel() {
    private val engineManager = InputMethodEngineManager()

    private val _uiState = MutableLiveData<InputMethodUiState>()
    val uiState: LiveData<InputMethodUiState> = _uiState

    // 當前布局配置（用於支援多輸入法的不同按鍵布局）
    private val _currentLayoutConfig = MutableLiveData<LayoutConfig>()
    val currentLayoutConfig: LiveData<LayoutConfig> = _currentLayoutConfig

    // 當前字根標籤映射（用於顯示不同輸入法的字根）
    private val _currentKeyNameMap = MutableLiveData<Map<Char, String>>()
    val currentKeyNameMap: LiveData<Map<Char, String>> = _currentKeyNameMap

    // 當前輸入法名稱
    private val _currentInputMethodName = MutableLiveData<String>()
    val currentInputMethodName: LiveData<String> = _currentInputMethodName

    init {
        // 監聽 Engine 狀態
        engineManager.engineState.observeForever { engineState ->
            handleEngineStateChange(engineState)
        }

        // 初始狀態
        _uiState.value =
            InputMethodUiState(
                code = "",
                candidates = emptyList(),
                keyboardState = ui.keyboard.KeyboardState.Normal,
            )

        // 初始化布局配置為倉頡
        _currentLayoutConfig.value = LayoutConfigs.CANGJIE
        _currentInputMethodName.value = "倉頡"
    }

    /**
     * 載入輸入法
     */
    fun loadInputMethod(context: Context) {
        val loader = TableLoader(context)
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        engineManager.loadInputMethod("cangjie", inputStream)
    }

    /**
     * 處理按鍵輸入
     */
    fun handleKeyPress(key: String) {
        when (key) {
            "BACKSPACE" -> handleBackspace()
            "SPACE", "ENTER" -> handleCommit()
            else -> handleCharacterKey(key)
        }
    }

    /**
     * 處理候選字選擇
     */
    fun handleCandidateSelection(index: Int) {
        val selected = engineManager.selectCandidate(index)
        if (selected != null) {
            // 通知 UI 上屏
            _uiState.value =
                _uiState.value?.copy(
                    commitText = selected.toString(),
                    code = "",
                    candidates = emptyList(),
                )

            // 清除 commitText（避免重複上屏）
            _uiState.value = _uiState.value?.copy(commitText = null)
        }

        updateUiState()
    }

    /**
     * 重試載入
     */
    fun retry() {
        engineManager.retry()
    }

    /**
     * 切換到指定的輸入法
     *
     * @param methodId 輸入法 ID（如 "cangjie"、"array"）
     * @param keyNameMap 該輸入法的字根標籤映射
     * @param displayName 該輸入法的顯示名稱
     */
    fun switchInputMethod(
        methodId: String,
        keyNameMap: Map<Char, String>,
        displayName: String,
    ) {
        // 更新布局配置
        val layoutConfig = LayoutConfigs.getConfig(methodId)
        _currentLayoutConfig.value = layoutConfig

        // 更新字根標籤
        _currentKeyNameMap.value = keyNameMap

        // 更新輸入法名稱
        _currentInputMethodName.value = displayName
    }

    private fun handleCharacterKey(key: String) {
        if (key.length == 1 && key[0] in 'a'..'z') {
            engineManager.processKey(key[0])
            updateUiState()
        }
    }

    private fun handleBackspace() {
        if (engineManager.hasInput()) {
            engineManager.backspace()
            updateUiState()
        } else {
            // 通知 UI 刪除已輸入的文字
            _uiState.value = _uiState.value?.copy(deleteText = true)
            _uiState.value = _uiState.value?.copy(deleteText = false)
        }
    }

    private fun handleCommit() {
        val text = engineManager.commit()
        if (text != null) {
            _uiState.value =
                _uiState.value?.copy(
                    commitText = text,
                    code = "",
                    candidates = emptyList(),
                )

            // 清除 commitText
            _uiState.value = _uiState.value?.copy(commitText = null)
        }
    }

    private fun handleEngineStateChange(engineState: EngineState) {
        val keyboardState =
            when (engineState) {
                is EngineState.Loading -> {
                    ui.keyboard.KeyboardState.Loading
                }

                is EngineState.Ready -> {
                    ui.keyboard.KeyboardState.Normal
                }

                is EngineState.Error -> {
                    ui.keyboard.KeyboardState.Error(
                        message = engineState.message,
                        canRetry = engineState.canRetry,
                    )
                }
            }

        _uiState.value = _uiState.value?.copy(keyboardState = keyboardState)
    }

    private fun updateUiState() {
        val code = engineManager.getCurrentCode()
        val candidates = engineManager.getCandidates()

        _uiState.value =
            _uiState.value?.copy(
                code = code,
                candidates = candidates,
            )
    }
}

/**
 * 輸入法 UI 狀態
 */
data class InputMethodUiState(
    val code: String = "",
    val candidates: List<Char> = emptyList(),
    val keyboardState: ui.keyboard.KeyboardState = ui.keyboard.KeyboardState.Normal,
    val commitText: String? = null,
    val deleteText: Boolean = false,
)
