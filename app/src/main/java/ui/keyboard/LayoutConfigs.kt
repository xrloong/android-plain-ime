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
     * 行列輸入法 - QWERTY 布局 + 額外按鍵
     *
     * 額外按鍵：[SOS] [END]
     * 位置：在標點符號欄上方、功能欄下方
     */
    val ARRAY =
        LayoutConfig(
            methodId = "array",
            displayName = "行列",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows =
                listOf(
                    listOf(
                        KeyButtonInfo(
                            key = "array_sos",
                            englishLabel = "",
                            chineseLabel = "SOS",
                            isSpecialKey = true,
                        ),
                        KeyButtonInfo(
                            key = "array_end",
                            englishLabel = "",
                            chineseLabel = "END",
                            isSpecialKey = true,
                        ),
                    ),
                ),
        )

    /**
     * 大易輸入法 - QWERTY 布局 + 額外按鍵
     *
     * 額外按鍵：[自動] [手動]
     * 位置：在標點符號欄上方、功能欄下方
     */
    val DAYI =
        LayoutConfig(
            methodId = "dayi",
            displayName = "大易",
            primaryLayout = KeyboardLayout.Cangjie,
            additionalKeyRows =
                listOf(
                    listOf(
                        KeyButtonInfo(
                            key = "dayi_auto",
                            englishLabel = "",
                            chineseLabel = "自動",
                            isSpecialKey = true,
                        ),
                        KeyButtonInfo(
                            key = "dayi_manual",
                            englishLabel = "",
                            chineseLabel = "手動",
                            isSpecialKey = true,
                        ),
                    ),
                ),
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
