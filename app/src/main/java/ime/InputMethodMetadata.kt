package ime

/**
 * 輸入法元數據 - 描述單個輸入法的基本信息
 *
 * @param id 唯一識別碼（如 "cangjie", "array", "boshiamy" 等）
 * @param displayName 顯示名稱（如 "倉頡", "行列", "嘸蝦米" 等）
 * @param fileName 在 assets 目錄中的文件名（如 "qhcj.cin"）
 * @param order 輪替順序 (0, 1, 2, ...)
 */
data class InputMethodMetadata(
    val id: String,
    val displayName: String,
    val fileName: String,
    val order: Int
) {
    companion object {
        /**
         * 英文輸入法的特殊標記檔名（無需載入 CIN 檔案）
         */
        const val ENGLISH_NO_CIN = ""

        /**
         * 內建輸入法列表（按輪替順序）
         * 順序：倉頡 → 英文 → 行列 → 大易 → 嘸蝦米 → 鄭碼
         */
        val BUILTIN_METHODS = listOf(
            InputMethodMetadata("cangjie", "倉頡", "qhcj.cin", 0),
            InputMethodMetadata("english", "英文", ENGLISH_NO_CIN, 1),
            InputMethodMetadata("array", "行列", "qhar.cin", 2),
            InputMethodMetadata("dayi", "大易", "qhdy.cin", 3),
            InputMethodMetadata("boshiamy", "嘸蝦米", "qhbs.cin", 4),
            InputMethodMetadata("zhengma", "鄭碼", "qhzm.cin", 5)
        )

        /**
         * 檢查是否為英文輸入法（不需要 CIN 檔案）
         */
        fun isEnglishMethod(methodId: String): Boolean = methodId == "english"

        /**
         * 根據當前輸入法 ID 取得下一個輸入法
         */
        fun getNext(currentId: String): InputMethodMetadata {
            val current = BUILTIN_METHODS.find { it.id == currentId }
            val nextIndex = ((current?.order ?: 0) + 1) % BUILTIN_METHODS.size
            return BUILTIN_METHODS[nextIndex]
        }

        /**
         * 根據 ID 取得輸入法元數據
         */
        fun getById(id: String): InputMethodMetadata? {
            return BUILTIN_METHODS.find { it.id == id }
        }

        /**
         * 根據文件名取得輸入法元數據
         */
        fun getByFileName(fileName: String): InputMethodMetadata? {
            return BUILTIN_METHODS.find { it.fileName == fileName }
        }
    }
}
