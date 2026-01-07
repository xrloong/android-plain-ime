package table

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class TableManager {
    private val cache = TableCache()
    private val _state = MutableLiveData<TableLoadState>()
    val state: LiveData<TableLoadState> = _state

    fun loadTable(path: String) {
        // TODO: background load, update _state
    }

    fun retry() {
        // TODO: retry logic
    }

    // ...查詢 API...
}


