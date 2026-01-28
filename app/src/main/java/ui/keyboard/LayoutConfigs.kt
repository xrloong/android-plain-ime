package ui.keyboard

/**
 * 預定義的輸入法布局配置
 *
 * 為每種輸入法定義完整的鍵盤布局，包括字根映射和額外按鍵信息
 */
object LayoutConfigs {
    /**
     * 倉頡輸入法 - 標準 QWERTY 布局，無額外按鍵
     * 字根由 CINParseResult.keyNameMap 提供
     */
    val CANGJIE =
        LayoutConfig(
            methodId = "cangjie",
            displayName = "倉頡",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows = emptyList(),
        )

    /**
     * 嘸蝦米輸入法 - 標準 QWERTY 布局，無額外按鍵
     * 重用倉頡的 QWERTY 布局，只是字根不同（來自 CIN 文件）
     */
    val BOSHIAMY =
        LayoutConfig(
            methodId = "boshiamy",
            displayName = "嘸蝦米",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows = emptyList(),
        )

    /**
     * 鄭碼輸入法 - 標準 QWERTY 布局，無額外按鍵
     * 重用倉頡的 QWERTY 布局，只是字根不同（來自 CIN 文件）
     */
    val ZHENGMA =
        LayoutConfig(
            methodId = "zhengma",
            displayName = "鄭碼",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows = emptyList(),
        )

    /**
     * 行列輸入法 - 專用 3 行布局
     *
     * 鍵盤布局（3行）：
     *   行1: q w e r t y u i o p
     *   行2: a s d f g h j k l ;
     *   行3: z x c v b n m , . /
     *
     * 總計：26個字母 + 3個標點符號（;, ., /）
     * 注：字根由 CINParseResult.keyNameMap 提供，這裡定義的是布局結構
     */
    val ARRAY =
        LayoutConfig(
            methodId = "array",
            displayName = "行列",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows = emptyList(),
        )

    /**
     * 大易輸入法 - 專用 4 行布局
     *
     * 鍵盤布局（4行）：
     *   行1: 1 2 3 4 5 6 7 8 9 0
     *   行2: q w e r t y u i o p
     *   行3: a s d f g h j k l ;
     *   行4: z x c v b n m , . /
     *
     * 總計：10個數字 + 26個字母 + 4個標點符號（;, ,, ., /）
     * 注：字根由 CINParseResult.keyNameMap 提供，這裡定義的是布局結構
     */
    val DAYI =
        LayoutConfig(
            methodId = "dayi",
            displayName = "大易",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows = emptyList(),
        )

    /**
     * 根據輸入法 ID 獲取對應的布局配置
     *
     * @param methodId 輸入法 ID
     * @return 對應的 LayoutConfig，如果不存在則返回倉頡配置
     */
    fun getConfig(methodId: String): LayoutConfig =
        when (methodId) {
            "cangjie" -> CANGJIE
            "boshiamy" -> BOSHIAMY
            "zhengma" -> ZHENGMA
            "array" -> ARRAY
            "dayi" -> DAYI
            else -> CANGJIE // 默認返回倉頡
        }

    /**
     * 獲取所有可用的布局配置列表
     */
    fun getAllConfigs(): List<LayoutConfig> =
        listOf(
            CANGJIE,
            BOSHIAMY,
            ZHENGMA,
            ARRAY,
            DAYI,
        )
}
