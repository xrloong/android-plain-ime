package table

sealed class TableLoadState {
    object Loading : TableLoadState()
    data class Success(val data: Any) : TableLoadState() // TODO: replace Any with actual data type
    data class Error(val message: String, val throwable: Throwable? = null, val retrySuggestion: String? = null) : TableLoadState()
}
