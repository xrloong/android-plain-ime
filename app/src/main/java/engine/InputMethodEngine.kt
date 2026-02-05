package engine

import cin.CINParseResult

/**
 * 輸入法引擎 - 處理輸入邏輯與查表
 *
 * 負責：
 * 1. 處理按鍵輸入，累積編碼
 * 2. 查詢候選字
 * 3. 管理輸入狀態（編碼緩衝區、候選字列表）
 * 4. 處理確認與清除操作
 */
class InputMethodEngine(
    private val tableData: CINParseResult
) {
    // 當前編碼緩衝區
    private val codeBuffer = StringBuilder()

    // 當前候選字列表
    private var candidates: List<Char> = emptyList()

    /**
     * 處理按鍵輸入
     * @param key 按下的鍵（a-z）
     * @return 是否成功處理（有效的輸入鍵）
     */
    fun processKey(key: Char): Boolean {
        if (!isValidKey(key)) {
            return false
        }

        // 將按鍵加入編碼緩衝區
        codeBuffer.append(key.lowercaseChar())

        // 更新候選字
        updateCandidates()

        return true
    }

    /**
     * 獲取當前編碼
     */
    fun getCurrentCode(): String {
        return codeBuffer.toString()
    }

    /**
     * 獲取當前候選字列表
     */
    fun getCandidates(): List<Char> {
        return candidates
    }

    /**
     * 選擇候選字（根據索引）
     * @param index 候選字索引（0-based）
     * @return 選中的字符，如果索引無效則返回 null
     */
    fun selectCandidate(index: Int): Char? {
        if (index < 0 || index >= candidates.size) {
            return null
        }

        val selected = candidates[index]

        // 清除狀態
        clear()

        return selected
    }

    /**
     * 選擇候選字（根據選字鍵）
     * @param selectionKey 選字鍵（1-9, 0）
     * @return 選中的字符，如果無效則返回 null
     */
    fun selectCandidateByKey(selectionKey: Char): Char? {
        val index = getIndexFromSelectionKey(selectionKey)
        return if (index >= 0) {
            selectCandidate(index)
        } else {
            null
        }
    }

    /**
     * 刪除最後一個編碼字符（Backspace）
     * @return 是否成功刪除
     */
    fun backspace(): Boolean {
        if (codeBuffer.isEmpty()) {
            return false
        }

        codeBuffer.deleteAt(codeBuffer.length - 1)
        updateCandidates()

        return true
    }

    /**
     * 清除所有輸入狀態
     */
    fun clear() {
        codeBuffer.clear()
        candidates = emptyList()
    }

    /**
     * 是否有輸入中的編碼
     */
    fun hasInput(): Boolean {
        return codeBuffer.isNotEmpty()
    }

    /**
     * 是否有候選字
     */
    fun hasCandidates(): Boolean {
        return candidates.isNotEmpty()
    }

    /**
     * 獲取第一個候選字（用於直接上屏）
     */
    fun getFirstCandidate(): Char? {
        return candidates.firstOrNull()
    }

    /**
     * 確認輸入（上屏第一個候選字或編碼本身）
     * @return 要上屏的文字，如果無內容則返回 null
     */
    fun commit(): String? {
        if (codeBuffer.isEmpty()) {
            return null
        }

        val result = if (hasCandidates()) {
            // 有候選字，上屏第一個候選字
            getFirstCandidate()?.toString()
        } else {
            // 無候選字，上屏編碼本身
            getCurrentCode()
        }

        clear()
        return result
    }

    // ===== 私有輔助方法 =====

    private fun updateCandidates() {
        val currentCode = getCurrentCode()
        candidates = if (currentCode.isNotEmpty()) {
            tableData.getCandidates(currentCode)
        } else {
            emptyList()
        }
    }

    private fun isValidKey(key: Char): Boolean {
        return if (tableData.keyNameMap.isNotEmpty()) {
            key in tableData.keyNameMap
        } else {
            key.lowercaseChar() in 'a'..'z'
        }
    }

    private fun getIndexFromSelectionKey(selectionKey: Char): Int {
        return when (selectionKey) {
            '1' -> 0
            '2' -> 1
            '3' -> 2
            '4' -> 3
            '5' -> 4
            '6' -> 5
            '7' -> 6
            '8' -> 7
            '9' -> 8
            '0' -> 9
            else -> -1
        }
    }
}

