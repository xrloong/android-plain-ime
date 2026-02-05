package ime

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cin.CINParser
import cin.CINParseResult
import table.TableLoader
import java.util.concurrent.Executors

/**
 * 輸入法狀態 - 描述當前輸入法的加載狀態
 */
sealed class InputMethodState {
    /**
     * 正在加載輸入法
     */
    data class Loading(val methodId: String) : InputMethodState()

    /**
     * 輸入法已就緒
     */
    data class Ready(val methodId: String, val data: CINParseResult) : InputMethodState()

    /**
     * 英文輸入法已就緒（不需要 CIN 數據）
     */
    data class EnglishReady(val methodId: String) : InputMethodState()

    /**
     * 加載出錯
     */
    data class Error(val methodId: String, val message: String) : InputMethodState()
}

/**
 * 輸入法管理器 - 負責管理多個輸入法的加載、切換、緩存
 */
class InputMethodManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("plain_ime_prefs", Context.MODE_PRIVATE)
    private val tableLoader = TableLoader(context)
    private val cinParser = CINParser()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val inputMethodPreferences = InputMethodPreferences(prefs)

    // 已加載的輸入法緩存 (methodId -> CINParseResult)
    private val loadedMethods = mutableMapOf<String, CINParseResult>()

    // 可用的輸入法列表（根據資源文件中實際存在的輸入法）
    private var availableMethods = listOf<InputMethodMetadata>()

    // 當前選定的輸入法 ID
    private var currentMethodId: String = loadPreferredMethod()

    // 當前輸入法狀態（已就緒、加載中、錯誤）
    private val _currentMethod = MutableLiveData<InputMethodState>()
    val currentMethod: LiveData<InputMethodState> = _currentMethod

    init {
        // 發現可用的輸入法
        discoverAvailableMethods()
        // 加載當前選定的輸入法
        loadCurrentMethod()
        // 後台預加載其他輸入法
        preloadOtherMethods()
    }

    /**
     * 發現可用的輸入法（檢查 assets 中哪些 CIN 文件存在）
     * 英文輸入法不需要 CIN 檔案，永遠可用
     */
    private fun discoverAvailableMethods() {
        val allAvailable = InputMethodMetadata.BUILTIN_METHODS.filter { method ->
            if (InputMethodMetadata.isEnglishMethod(method.id)) return@filter true
            try {
                context.assets.open(method.fileName).use { true }
            } catch (e: Exception) {
                false
            }
        }

        // Filter by user-enabled preferences, respecting user-defined order
        val orderedEnabled = inputMethodPreferences.getOrderedEnabledMethods()
        availableMethods = orderedEnabled.filter { enabled ->
            allAvailable.any { it.id == enabled.id }
        }

        if (availableMethods.isEmpty()) {
            availableMethods = listOf(allAvailable.firstOrNull() ?: InputMethodMetadata.BUILTIN_METHODS[0])
        }

        if (availableMethods.find { it.id == currentMethodId } == null) {
            currentMethodId = availableMethods[0].id
            savePreferredMethod(currentMethodId)
        }
    }

    /**
     * 加載當前選定的輸入法
     */
    private fun loadCurrentMethod() {
        singleThreadExecutor.execute {
            try {
                val method = InputMethodMetadata.getById(currentMethodId)
                    ?: InputMethodMetadata.BUILTIN_METHODS[0]

                // 英文輸入法不需要加載 CIN 檔案
                if (InputMethodMetadata.isEnglishMethod(method.id)) {
                    _currentMethod.postValue(InputMethodState.EnglishReady(method.id))
                    return@execute
                }

                // 檢查是否已緩存
                val cached = loadedMethods[method.id]
                if (cached != null) {
                    _currentMethod.postValue(InputMethodState.Ready(method.id, cached))
                    return@execute
                }

                _currentMethod.postValue(InputMethodState.Loading(method.id))

                val inputStream = tableLoader.loadFromAssets(method.fileName)
                val content = inputStream.bufferedReader().use { it.readText() }
                if (content.isBlank()) {
                    throw Exception("文件內容為空: ${method.fileName}")
                }

                val parseResult = cinParser.parse(content)
                loadedMethods[method.id] = parseResult
                _currentMethod.postValue(InputMethodState.Ready(method.id, parseResult))
            } catch (e: Exception) {
                _currentMethod.postValue(
                    InputMethodState.Error(currentMethodId, "加載失敗: ${e.message}")
                )
            }
        }
    }

    /**
     * 後台預加載其他輸入法
     */
    private fun preloadOtherMethods() {
        singleThreadExecutor.execute {
            for (method in availableMethods) {
                if (method.id == currentMethodId || loadedMethods.containsKey(method.id)) {
                    continue
                }
                // 英文輸入法不需要預加載
                if (InputMethodMetadata.isEnglishMethod(method.id)) {
                    continue
                }

                try {
                    val inputStream = tableLoader.loadFromAssets(method.fileName)
                    val content = inputStream.bufferedReader().use { it.readText() }
                    if (content.isBlank()) continue

                    val parseResult = cinParser.parse(content)
                    loadedMethods[method.id] = parseResult
                } catch (e: Exception) {
                    // 預加載失敗不影響使用，延遲加載
                }
            }
        }
    }

    /**
     * 切換到下一個輸入法
     */
    fun switchToNext() {
        if (availableMethods.size <= 1) {
            return  // 只有一個輸入法，無需切換
        }

        val currentMethod = InputMethodMetadata.getById(currentMethodId)
            ?: InputMethodMetadata.BUILTIN_METHODS[0]

        // 計算下一個可用輸入法的索引
        val currentIndex = availableMethods.indexOfFirst { it.id == currentMethod.id }
        val nextIndex = (currentIndex + 1) % availableMethods.size
        val nextMethod = availableMethods[nextIndex]

        switchTo(nextMethod.id)
    }

    /**
     * 切換到指定的輸入法
     */
    fun switchTo(methodId: String) {
        if (currentMethodId == methodId) {
            return
        }

        // 檢查該輸入法是否可用
        if (availableMethods.find { it.id == methodId } == null) {
            return
        }

        currentMethodId = methodId
        savePreferredMethod(methodId)
        loadCurrentMethod()
    }

    /**
     * 取得當前輸入法的表格數據
     */
    fun getCurrentTableData(): CINParseResult? {
        return loadedMethods[currentMethodId]
    }

    /**
     * 取得當前輸入法的元數據
     */
    fun getCurrentMetadata(): InputMethodMetadata? {
        return InputMethodMetadata.getById(currentMethodId)
    }

    /**
     * 取得所有可用的輸入法列表
     */
    fun getAvailableMethods(): List<InputMethodMetadata> {
        return availableMethods.toList()
    }

    /**
     * 取得當前輸入法 ID
     */
    fun getCurrentMethodId(): String {
        return currentMethodId
    }

    /**
     * 重試加載失敗的輸入法
     */
    fun retry() {
        loadCurrentMethod()
    }

    fun reloadPreferences() {
        discoverAvailableMethods()
        // If current method was disabled, switch to the first available
        if (availableMethods.find { it.id == currentMethodId } == null) {
            currentMethodId = availableMethods[0].id
            savePreferredMethod(currentMethodId)
            loadCurrentMethod()
        }
    }

    /**
     * 清除所有緩存（用於重啟時）
     */
    fun clearCache() {
        loadedMethods.clear()
    }

    /**
     * 從 SharedPreferences 中讀取用戶偏好的輸入法
     */
    private fun loadPreferredMethod(): String {
        return prefs.getString("preferred_input_method", "cangjie") ?: "cangjie"
    }

    /**
     * 保存用戶偏好的輸入法到 SharedPreferences
     */
    private fun savePreferredMethod(methodId: String) {
        prefs.edit().putString("preferred_input_method", methodId).apply()
    }
}
