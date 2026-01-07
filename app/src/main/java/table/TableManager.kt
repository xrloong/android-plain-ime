package table

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cin.CINParser
import cin.CINParseException
import java.io.InputStream
import java.util.concurrent.Executors

class TableManager {
    private val cache = TableCache()
    private val parser = CINParser()
    private val executor = Executors.newSingleThreadExecutor()

    private val _state = MutableLiveData<TableLoadState>()
    val state: LiveData<TableLoadState> = _state

    private var lastLoadRequest: LoadRequest? = null

    /**
     * 載入 CIN 查表檔
     * @param key 查表識別鍵（如 "cangjie"）
     * @param inputStream CIN 檔案的 InputStream
     */
    fun loadTable(key: String, inputStream: InputStream) {
        lastLoadRequest = LoadRequest(key, inputStream)
        performLoad(key, inputStream)
    }

    /**
     * 重試上次失敗的載入
     */
    fun retry() {
        val request = lastLoadRequest
        if (request != null) {
            performLoad(request.key, request.inputStream)
        }
    }

    private fun performLoad(key: String, inputStream: InputStream) {
        // 檢查快取
        if (cache.contains(key)) {
            _state.postValue(TableLoadState.Success(cache.get(key)!!))
            return
        }

        // 設置為載入中狀態
        _state.postValue(TableLoadState.Loading)

        // 背景執行緒載入
        executor.execute {
            try {
                val content = inputStream.bufferedReader().use { it.readText() }
                val result = parser.parse(content)

                // 儲存到快取
                cache.put(key, result)

                // 更新狀態為成功
                _state.postValue(TableLoadState.Success(result))
            } catch (e: CINParseException) {
                _state.postValue(
                    TableLoadState.Error(
                        message = "CIN 檔案解析失敗：${e.message}",
                        throwable = e,
                        retrySuggestion = "請檢查 CIN 檔案格式是否正確，確認包含 %chardef 區段"
                    )
                )
            } catch (e: Exception) {
                _state.postValue(
                    TableLoadState.Error(
                        message = "載入查表資料時發生錯誤：${e.message}",
                        throwable = e,
                        retrySuggestion = "請檢查檔案是否存在且可讀取，然後點擊重試"
                    )
                )
            }
        }
    }

    /**
     * 查詢編碼對應的候選字（僅在 Success 狀態下可用）
     */
    fun getCandidates(code: String): List<Char>? {
        val currentState = _state.value
        return if (currentState is TableLoadState.Success) {
            currentState.data.getCandidates(code)
        } else {
            null
        }
    }

    /**
     * 查詢字符對應的編碼（僅在 Success 狀態下可用）
     */
    fun getCode(char: Char): String? {
        val currentState = _state.value
        return if (currentState is TableLoadState.Success) {
            currentState.data.getCode(char)
        } else {
            null
        }
    }

    /**
     * 清除快取
     */
    fun clearCache() {
        cache.clear()
    }

    private data class LoadRequest(val key: String, val inputStream: InputStream)
}


