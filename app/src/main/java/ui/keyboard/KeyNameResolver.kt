package ui.keyboard

/**
 * 字根標籤解析器 - 為缺失的字根提供 Fallback 值
 *
 * 解析優先級：
 * 1. 使用提供的 keyNameMap 中的值
 * 2. 如果缺失，嘗試從參考字根表（倉頡）查詢
 * 3. 如果還缺失，使用英文字母
 * 4. 如果還缺失，使用 "?"
 */
object KeyNameResolver {
    /**
     * 倉頡的參考字根 - 用作 Fallback 的參考
     */
    private val CANGJIE_REFERENCE_ROOTS =
        mapOf(
            'q' to "手",
            'w' to "田",
            'e' to "水",
            'r' to "口",
            't' to "廿",
            'y' to "卜",
            'u' to "山",
            'i' to "戈",
            'o' to "人",
            'p' to "心",
            'a' to "日",
            's' to "尸",
            'd' to "木",
            'f' to "火",
            'g' to "土",
            'h' to "竹",
            'j' to "十",
            'k' to "大",
            'l' to "中",
            'z' to "重",
            'x' to "難",
            'c' to "金",
            'v' to "女",
            'b' to "月",
            'n' to "弓",
            'm' to "一",
        )

    /**
     * 標準 QWERTY 鍵盤的所有字母鍵
     */
    private val KEYBOARD_CHARS =
        setOf(
            'q',
            'w',
            'e',
            'r',
            't',
            'y',
            'u',
            'i',
            'o',
            'p',
            'a',
            's',
            'd',
            'f',
            'g',
            'h',
            'j',
            'k',
            'l',
            'z',
            'x',
            'c',
            'v',
            'b',
            'n',
            'm',
        )

    /**
     * 解析字根標籤，應用 Fallback 策略
     *
     * @param keyNameMap 從 CIN 文件解析得到的字根標籤映射
     * @param fallbackMap 備選字根映射（默認使用倉頡參考）
     * @return 完整的字根標籤映射，所有鍵盤按鍵都有標籤
     */
    fun resolve(
        keyNameMap: Map<Char, String>,
        fallbackMap: Map<Char, String> = CANGJIE_REFERENCE_ROOTS,
    ): Map<Char, String> {
        val result = keyNameMap.toMutableMap()

        for (char in KEYBOARD_CHARS) {
            if (!result.containsKey(char)) {
                // 策略 1: 從 fallbackMap（倉頡）查詢
                val fallbackValue = fallbackMap[char]
                if (fallbackValue != null) {
                    result[char] = fallbackValue
                } else {
                    // 策略 2: 使用英文字母
                    result[char] = char.toString().uppercase()
                }
            }
        }

        return result
    }

    /**
     * 為 Map 擴展函數：快速應用 Fallback
     */
    fun Map<Char, String>.withFallback(fallbackMap: Map<Char, String> = CANGJIE_REFERENCE_ROOTS): Map<Char, String> =
        resolve(this, fallbackMap)
}
