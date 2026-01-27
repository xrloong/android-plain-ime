package engine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cin.CINParseResult
import table.TableManager
import table.TableLoadState

/**
 * 輸入法引擎管理器 - 整合 TableManager 的高階 API
 *
 * 提供完整的輸入法功能：
 * 1. 管理 TableManager 生命週期
 * 2. 自動處理查表載入狀態
 * 3. 提供統一的輸入介面
 * 4. 處理輸入法切換
 */
class InputMethodEngineManager {

    private val tableManager = TableManager()
    private var currentEngine: InputMethodEngine? = null

    private val _engineState = MutableLiveData<EngineState>()
    val engineState: LiveData<EngineState> = _engineState

    init {
        // 監聽 TableManager 狀態變化
        tableManager.state.observeForever { tableState ->
            handleTableStateChange(tableState)
        }
    }

    /**
     * 載入輸入法表格
     * @param key 輸入法識別鍵
     * @param inputStream 查表檔案的 InputStream
     */
    fun loadInputMethod(key: String, inputStream: java.io.InputStream) {
        tableManager.loadTable(key, inputStream)
    }

    /**
     * 處理按鍵輸入
     */
    fun processKey(key: Char): Boolean {
        val engine = currentEngine ?: return false
        return engine.processKey(key)
    }

    /**
     * 獲取當前編碼
     */
    fun getCurrentCode(): String {
        return currentEngine?.getCurrentCode() ?: ""
    }

    /**
     * 獲取候選字列表
     */
    fun getCandidates(): List<Char> {
        return currentEngine?.getCandidates() ?: emptyList()
    }

    /**
     * 選擇候選字
     */
    fun selectCandidate(index: Int): Char? {
        return currentEngine?.selectCandidate(index)
    }

    /**
     * 根據選字鍵選擇候選字
     */
    fun selectCandidateByKey(selectionKey: Char): Char? {
        return currentEngine?.selectCandidateByKey(selectionKey)
    }

    /**
     * Backspace
     */
    fun backspace(): Boolean {
        return currentEngine?.backspace() ?: false
    }

    /**
     * 清除輸入
     */
    fun clear() {
        currentEngine?.clear()
    }

    /**
     * 確認輸入（上屏）
     */
    fun commit(): String? {
        return currentEngine?.commit()
    }

    /**
     * 是否有輸入中的內容
     */
    fun hasInput(): Boolean {
        return currentEngine?.hasInput() ?: false
    }

    /**
     * 是否有候選字
     */
    fun hasCandidates(): Boolean {
        return currentEngine?.hasCandidates() ?: false
    }

    /**
     * 重試載入
     */
    fun retry() {
        tableManager.retry()
    }

    /**
     * 清除快取
     */
    fun clearCache() {
        tableManager.clearCache()
    }

    /**
     * 直接更新表格數據（用於輸入法切換）
     */
    fun updateTableData(tableData: CINParseResult) {
        currentEngine = InputMethodEngine(tableData)
        _engineState.postValue(
            EngineState.Ready(
                inputMethodName = tableData.displayName,
                totalChars = tableData.totalChars
            )
        )
    }

    /**
     * 取得當前表格數據
     */
    fun getCurrentTableData(): CINParseResult? {
        return currentEngine?.let { engine ->
            // 從引擎中取得當前使用的表格數據
            // 這需要在 InputMethodEngine 中公開表格數據
            null  // 暫時返回 null，後續可以優化
        }
    }

    // ===== 私有方法 =====

    private fun handleTableStateChange(tableState: TableLoadState) {
        when (tableState) {
            is TableLoadState.Loading -> {
                currentEngine = null
                _engineState.postValue(EngineState.Loading)
            }
            is TableLoadState.Success -> {
                currentEngine = InputMethodEngine(tableState.data)
                _engineState.postValue(
                    EngineState.Ready(
                        inputMethodName = tableState.data.chineseName,
                        totalChars = tableState.data.totalChars
                    )
                )
            }
            is TableLoadState.Error -> {
                currentEngine = null
                _engineState.postValue(
                    EngineState.Error(
                        message = tableState.message,
                        canRetry = true
                    )
                )
            }
        }
    }
}

/**
 * 輸入法引擎狀態
 */
sealed class EngineState {
    /**
     * 載入中
     */
    object Loading : EngineState()

    /**
     * 就緒（可以輸入）
     */
    data class Ready(
        val inputMethodName: String,
        val totalChars: Int
    ) : EngineState()

    /**
     * 錯誤
     */
    data class Error(
        val message: String,
        val canRetry: Boolean
    ) : EngineState()
}

