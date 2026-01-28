package ui.keyboard

/**
 * 鍵盤布局配置 - 定義一個輸入法的完整鍵盤布局信息
 *
 * @param methodId 輸入法 ID（如 "cangjie"、"array"）
 * @param displayName 輸入法顯示名稱（如 "倉頡"、"行列"）
 * @param primaryLayout 主要鍵盤布局（倉頡、標點、英文等）
 * @param additionalKeyRows 額外的按鍵行列表（用於行列、大易等特殊輸入法）
 */
data class LayoutConfig(
    val methodId: String,
    val displayName: String,
    val primaryLayout: KeyboardLayout = KeyboardLayout.Cangjie,
    val additionalKeyRows: List<List<KeyButtonInfo>> = emptyList(),
)

/**
 * 單個按鍵信息 - 定義鍵盤上一個按鍵的信息
 *
 * @param key 按鍵標識符（如 'q'、'w'，或特殊鍵如 "sos"）
 * @param englishLabel 英文標籤（通常為字母）
 * @param chineseLabel 中文標籤（字根或符號）
 * @param alternativeChars 長按時顯示的替代字符列表
 * @param isSpecialKey 是否為特殊功能按鍵（非字母按鍵）
 */
data class KeyButtonInfo(
    val key: String,
    val englishLabel: String = "",
    val chineseLabel: String = "",
    val alternativeChars: List<String> = emptyList(),
    val isSpecialKey: Boolean = false,
)
