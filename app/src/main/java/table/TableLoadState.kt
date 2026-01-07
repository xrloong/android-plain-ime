package table

import cin.CINParseResult

sealed class TableLoadState {
    object Loading : TableLoadState()
    data class Success(val data: CINParseResult) : TableLoadState()
    data class Error(val message: String, val throwable: Throwable? = null, val retrySuggestion: String? = null) : TableLoadState()
}
