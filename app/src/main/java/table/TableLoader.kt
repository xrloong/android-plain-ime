package table

import android.content.Context
import java.io.InputStream

/**
 * 查表檔案載入器，負責從不同來源載入 CIN 檔案
 */
class TableLoader(private val context: Context) {

    /**
     * 從 assets 載入 CIN 檔案
     * @param fileName assets 中的檔案名稱，如 "qhcj.cin"
     * @return InputStream
     */
    fun loadFromAssets(fileName: String): InputStream {
        return context.assets.open(fileName)
    }

    /**
     * 從檔案路徑載入 CIN 檔案
     * @param filePath 完整的檔案路徑
     * @return InputStream
     */
    fun loadFromFile(filePath: String): InputStream {
        return java.io.File(filePath).inputStream()
    }

    companion object {
        /**
         * 內建的倉頡輸入法檔案名稱
         */
        const val BUILTIN_CANGJIE = "qhcj.cin"
    }
}

