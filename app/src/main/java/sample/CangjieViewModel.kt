package sample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import table.TableLoader
import table.TableManager
import table.TableLoadState

/**
 * 示例 ViewModel：展示如何載入內建倉頡輸入法
 */
class CangjieViewModel : ViewModel() {

    private val tableManager = TableManager()

    val tableState: LiveData<TableLoadState> = tableManager.state

    /**
     * 載入內建倉頡輸入法
     */
    fun loadBuiltinCangjie(context: Context) {
        val loader = TableLoader(context)
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        tableManager.loadTable("cangjie", inputStream)
    }

    /**
     * 查詢候選字
     */
    fun getCandidates(code: String): List<Char>? {
        return tableManager.getCandidates(code)
    }

    /**
     * 查詢編碼
     */
    fun getCode(char: Char): String? {
        return tableManager.getCode(char)
    }

    /**
     * 重試載入
     */
    fun retry() {
        tableManager.retry()
    }
}

