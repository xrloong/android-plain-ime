package ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import ui.keyboard.KeyButton

/**
 * QWERTY 鍵盤視圖
 *
 * 負責：
 * 1. 顯示 QWERTY 鍵盤佈局
 * 2. 處理按鍵點擊事件
 * 3. 支援長按彈窗顯示標點符號
 * 4. 根據狀態顯示載入、錯誤提示
 */
class QwertyKeyboardView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        private var keyClickListener: KeyClickListener? = null
        private var currentLayout: KeyboardLayout = KeyboardLayout.Cangjie
        private var currentLayoutConfig: LayoutConfig = LayoutConfigs.CANGJIE
        private var rootLabelMap: Map<Char, String> = getDefaultCangjieRoots()
        private var additionalKeyRows: List<List<KeyButtonInfo>> = emptyList()
        private var spaceButton: android.widget.Button? = null

        init {
            orientation = VERTICAL
            val keyboardBgColor =
                resources.getColor(
                    resources.getIdentifier("keyboard_background", "color", context.packageName),
                    null,
                )
            setBackgroundColor(keyboardBgColor)
            val paddingDp = 4
            val paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            setupKeyboard()
        }

        /**
         * 設置按鍵點擊監聽器
         */
        fun setKeyClickListener(listener: KeyClickListener) {
            this.keyClickListener = listener
        }

        /**
         * 設置鍵盤狀態
         */
        fun setKeyboardState(state: KeyboardState) {
            when (state) {
                is KeyboardState.Normal -> {
                    enableAllKeys()
                }

                is KeyboardState.Loading -> {
                    disableAllKeys()
                    showLoadingIndicator()
                }

                is KeyboardState.Error -> {
                    showErrorMessage(state.message, state.canRetry)
                }
            }
        }

        /**
         * Toggle between Cangjie and Punctuation layouts
         */
        fun toggleLayout() {
            currentLayout =
                when (currentLayout) {
                    is KeyboardLayout.Cangjie -> KeyboardLayout.Punctuation
                    is KeyboardLayout.Punctuation -> KeyboardLayout.Cangjie
                    is KeyboardLayout.English -> KeyboardLayout.Punctuation
                }
            setupKeyboard()
        }

        /**
         * Toggle between English and Cangjie input methods
         */
        fun toggleInputMethod() {
            currentLayout =
                when (currentLayout) {
                    is KeyboardLayout.Cangjie -> KeyboardLayout.English
                    is KeyboardLayout.English -> KeyboardLayout.Cangjie
                    is KeyboardLayout.Punctuation -> KeyboardLayout.English
                }
            setupKeyboard()
        }

        /**
         * Get current layout
         */
        fun getCurrentLayout(): KeyboardLayout = currentLayout

        /**
         * 更新布局配置 - 用於切換輸入法時更新整個鍵盤布局
         * 包括額外按鍵、主要布局等
         */
        fun updateLayout(layoutConfig: LayoutConfig) {
            currentLayoutConfig = layoutConfig
            additionalKeyRows = layoutConfig.additionalKeyRows
            currentLayout = layoutConfig.primaryLayout
            setupKeyboard()
        }

        /**
         * 更新字根標籤 - 用於切換輸入法時動態改變鍵盤上的字根顯示
         * 應用 Fallback 機制確保所有按鍵都有標籤
         */
        fun updateRootLabels(keyNameMap: Map<Char, String>) {
            rootLabelMap =
                if (keyNameMap.isEmpty()) {
                    getDefaultCangjieRoots()
                } else {
                    // 應用 Fallback 機制
                    KeyNameResolver.resolve(keyNameMap, getDefaultCangjieRoots())
                }
            // 重新構建鍵盤以應用新的字根標籤
            if (currentLayout is KeyboardLayout.Cangjie) {
                setupKeyboard()
            }
        }

        /**
         * 更新 Space 按鈕的標籤 - 用於顯示當前輸入法名稱
         */
        fun updateSpaceBarLabel(displayName: String) {
            spaceButton?.text = displayName
        }

        /**
         * 取得字根標籤 - 如果 keyNameMap 為空，使用預設倉頡字根
         */
        private fun getRootLabel(key: Char): String = rootLabelMap[key] ?: key.toString().uppercase()

        /**
         * 倉頡輸入法的預設字根標籤
         */
        private fun getDefaultCangjieRoots(): Map<Char, String> =
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
                // 標點符號和數字的默認標籤（用於 Fallback）
                ';' to ";",
                ',' to ",",
                '.' to ".",
                '/' to "/",
                '0' to "0",
                '1' to "1",
                '2' to "2",
                '3' to "3",
                '4' to "4",
                '5' to "5",
                '6' to "6",
                '7' to "7",
                '8' to "8",
                '9' to "9",
            )

        private fun setupKeyboard() {
            removeAllViews() // Clear before rebuilding

            // 根據輸入法類型選擇對應的布局
            when {
                currentLayoutConfig.methodId == "array" -> {
                    setupArrayLayout()
                }

                currentLayoutConfig.methodId == "dayi" -> {
                    setupDayiLayout()
                }

                else -> {
                    when (currentLayout) {
                        is KeyboardLayout.Cangjie -> setupCangjieLayout()
                        is KeyboardLayout.Punctuation -> setupPunctuationLayout()
                        is KeyboardLayout.English -> setupEnglishLayout()
                    }
                }
            }

            // 添加額外按鍵行（如果有）
            addAdditionalKeyRows()

            addFunctionKeyRow()
        }

        /**
         * 為行列、大易等輸入法添加額外的按鍵行
         */
        private fun addAdditionalKeyRows() {
            for (row in additionalKeyRows) {
                addAdditionalKeyRow(row)
            }
        }

        /**
         * 添加單個額外按鍵行
         */
        private fun addAdditionalKeyRow(keyRow: List<KeyButtonInfo>) {
            val rowLayout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            val heightDp = 56
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()

            for (keyInfo in keyRow) {
                val button = KeyButton(context)
                button.text = keyInfo.chineseLabel
                button.layoutParams = LinearLayout.LayoutParams(0, heightPx, 1f)
                button.setOnClickListener {
                    keyClickListener?.onKeyClick(keyInfo.key)
                }
                rowLayout.addView(button)
            }

            addView(rowLayout)
        }

        private fun setupCangjieLayout() {
            // 第一行：q w e r t y u i o p
            addKeyRow(
                listOf(
                    "q\n${getRootLabel('q')}",
                    "w\n${getRootLabel('w')}",
                    "e\n${getRootLabel('e')}",
                    "r\n${getRootLabel('r')}",
                    "t\n${getRootLabel('t')}",
                    "y\n${getRootLabel('y')}",
                    "u\n${getRootLabel('u')}",
                    "i\n${getRootLabel('i')}",
                    "o\n${getRootLabel('o')}",
                    "p\n${getRootLabel('p')}",
                ),
            )

            // 第二行：a s d f g h j k l
            addKeyRow(
                listOf(
                    "a\n${getRootLabel('a')}",
                    "s\n${getRootLabel('s')}",
                    "d\n${getRootLabel('d')}",
                    "f\n${getRootLabel('f')}",
                    "g\n${getRootLabel('g')}",
                    "h\n${getRootLabel('h')}",
                    "j\n${getRootLabel('j')}",
                    "k\n${getRootLabel('k')}",
                    "l\n${getRootLabel('l')}",
                ),
            )

            // 第三行：Shift + z x c v b n m + Backspace
            val row3Layout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            // Shift key
            val shiftButton = createFunctionKeyButton("⇧", "SHIFT")
            row3Layout.addView(shiftButton)

            // z-m keys
            for (key in listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')) {
                val keyButton = createKeyButton("$key\n${getRootLabel(key)}")
                row3Layout.addView(keyButton)
            }

            // Backspace key (moved from function row)
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            row3Layout.addView(backspaceButton)

            addView(row3Layout)
        }

        /**
         * 行列輸入法布局（3行）
         * 行1: q w e r t y u i o p
         * 行2: a s d f g h j k l ;
         * 行3: z x c v b n m , . /
         */
        private fun setupArrayLayout() {
            // 第一行：q w e r t y u i o p
            addKeyRow(
                listOf(
                    "q\n${getRootLabel('q')}",
                    "w\n${getRootLabel('w')}",
                    "e\n${getRootLabel('e')}",
                    "r\n${getRootLabel('r')}",
                    "t\n${getRootLabel('t')}",
                    "y\n${getRootLabel('y')}",
                    "u\n${getRootLabel('u')}",
                    "i\n${getRootLabel('i')}",
                    "o\n${getRootLabel('o')}",
                    "p\n${getRootLabel('p')}",
                ),
            )

            // 第二行：a s d f g h j k l ;
            addKeyRow(
                listOf(
                    "a\n${getRootLabel('a')}",
                    "s\n${getRootLabel('s')}",
                    "d\n${getRootLabel('d')}",
                    "f\n${getRootLabel('f')}",
                    "g\n${getRootLabel('g')}",
                    "h\n${getRootLabel('h')}",
                    "j\n${getRootLabel('j')}",
                    "k\n${getRootLabel('k')}",
                    "l\n${getRootLabel('l')}",
                    ";\n${getRootLabel(';')}",
                ),
            )

            // 第三行：Shift + z x c v b n m , . / + Backspace
            val row3Layout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            // Shift key
            val shiftButton = createFunctionKeyButton("⇧", "SHIFT")
            row3Layout.addView(shiftButton)

            // z-m keys
            for (key in listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')) {
                val keyButton = createKeyButton("$key\n${getRootLabel(key)}")
                row3Layout.addView(keyButton)
            }

            // 標點符號：, . /
            for (key in listOf(',', '.', '/')) {
                val keyButton = createKeyButton("$key\n${getRootLabel(key)}")
                row3Layout.addView(keyButton)
            }

            // Backspace key
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            row3Layout.addView(backspaceButton)

            addView(row3Layout)
        }

        /**
         * 大易輸入法布局（4行）
         * 行1: 1 2 3 4 5 6 7 8 9 0
         * 行2: q w e r t y u i o p
         * 行3: a s d f g h j k l ;
         * 行4: z x c v b n m , . /
         */
        private fun setupDayiLayout() {
            // 第一行：1 2 3 4 5 6 7 8 9 0
            addKeyRow(
                listOf(
                    "1\n${getRootLabel('1')}",
                    "2\n${getRootLabel('2')}",
                    "3\n${getRootLabel('3')}",
                    "4\n${getRootLabel('4')}",
                    "5\n${getRootLabel('5')}",
                    "6\n${getRootLabel('6')}",
                    "7\n${getRootLabel('7')}",
                    "8\n${getRootLabel('8')}",
                    "9\n${getRootLabel('9')}",
                    "0\n${getRootLabel('0')}",
                ),
            )

            // 第二行：q w e r t y u i o p
            addKeyRow(
                listOf(
                    "q\n${getRootLabel('q')}",
                    "w\n${getRootLabel('w')}",
                    "e\n${getRootLabel('e')}",
                    "r\n${getRootLabel('r')}",
                    "t\n${getRootLabel('t')}",
                    "y\n${getRootLabel('y')}",
                    "u\n${getRootLabel('u')}",
                    "i\n${getRootLabel('i')}",
                    "o\n${getRootLabel('o')}",
                    "p\n${getRootLabel('p')}",
                ),
            )

            // 第三行：a s d f g h j k l ;
            addKeyRow(
                listOf(
                    "a\n${getRootLabel('a')}",
                    "s\n${getRootLabel('s')}",
                    "d\n${getRootLabel('d')}",
                    "f\n${getRootLabel('f')}",
                    "g\n${getRootLabel('g')}",
                    "h\n${getRootLabel('h')}",
                    "j\n${getRootLabel('j')}",
                    "k\n${getRootLabel('k')}",
                    "l\n${getRootLabel('l')}",
                    ";\n${getRootLabel(';')}",
                ),
            )

            // 第四行：Shift + z x c v b n m , . / + Backspace
            val row4Layout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            // Shift key
            val shiftButton = createFunctionKeyButton("⇧", "SHIFT")
            row4Layout.addView(shiftButton)

            // z-m keys
            for (key in listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')) {
                val keyButton = createKeyButton("$key\n${getRootLabel(key)}")
                row4Layout.addView(keyButton)
            }

            // 標點符號：, . /
            for (key in listOf(',', '.', '/')) {
                val keyButton = createKeyButton("$key\n${getRootLabel(key)}")
                row4Layout.addView(keyButton)
            }

            // Backspace key
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            row4Layout.addView(backspaceButton)

            addView(row4Layout)
        }

        private fun setupPunctuationLayout() {
            // Row 1: Common Chinese punctuation (10 keys)
            addKeyRow(
                listOf(
                    "，\n，",
                    "。\n。",
                    "？\n？",
                    "！\n！",
                    "：\n：",
                    "；\n；",
                    "（\n（",
                    "）\n）",
                    "「\n「",
                    "」\n」",
                ),
            )

            // Row 2: Additional punctuation (9 keys)
            addKeyRow(
                listOf(
                    "—\n—",
                    "～\n～",
                    "『\n『",
                    "』\n』",
                    "【\n【",
                    "】\n】",
                    "《\n《",
                    "》\n》",
                    "、\n、",
                ),
            )

            // Row 3: Shift + Numbers + Backspace
            val row3Layout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            // Shift key
            val shiftButton = createFunctionKeyButton("⇧", "SHIFT")
            row3Layout.addView(shiftButton)

            // Number keys
            for (key in listOf("1\n1", "2\n2", "3\n3", "4\n4", "5\n5", "6\n6", "7\n7")) {
                val keyButton = createKeyButton(key)
                row3Layout.addView(keyButton)
            }

            // Backspace key
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            row3Layout.addView(backspaceButton)

            addView(row3Layout)
        }

        private fun setupEnglishLayout() {
            // Row 1: Q-P
            addKeyRow(
                listOf(
                    "q\nQ",
                    "w\nW",
                    "e\nE",
                    "r\nR",
                    "t\nT",
                    "y\nY",
                    "u\nU",
                    "i\nI",
                    "o\nO",
                    "p\nP",
                ),
            )

            // Row 2: A-L
            addKeyRow(
                listOf(
                    "a\nA",
                    "s\nS",
                    "d\nD",
                    "f\nF",
                    "g\nG",
                    "h\nH",
                    "j\nJ",
                    "k\nK",
                    "l\nL",
                ),
            )

            // Row 3: Shift + Z-M + Backspace
            val row3Layout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                }

            // Shift key
            val shiftButton = createFunctionKeyButton("⇧", "SHIFT")
            row3Layout.addView(shiftButton)

            // z-m keys
            for (key in listOf("z\nZ", "x\nX", "c\nC", "v\nV", "b\nB", "n\nN", "m\nM")) {
                val keyButton = createKeyButton(key)
                row3Layout.addView(keyButton)
            }

            // Backspace key
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            row3Layout.addView(backspaceButton)

            addView(row3Layout)
        }

        private fun addKeyRow(keys: List<String>) {
            val rowLayout =
                LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT,
                        )
                }

            for (key in keys) {
                val keyButton = createKeyButton(key)
                rowLayout.addView(keyButton)
            }

            addView(rowLayout)
        }

        private fun createKeyButton(key: String): KeyButton {
            val button = KeyButton(context)

            val englishKey = key.substringBefore('\n')
            val chineseRoot = key.substringAfter('\n')

            // Set dual text for custom rendering
            button.setDualText(englishKey.uppercase(), chineseRoot)

            // 設置按鍵尺寸（高度 56dp 以容納兩行文字）
            val heightDp = 56
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()
            button.layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    heightPx,
                    1f,
                )

            // 設置點擊事件（只發送英文字母）
            button.setOnClickListener {
                keyClickListener?.onKeyClick(englishKey)
            }

            // 設置長按事件（用於標點符號）
            button.setOnLongClickListener {
                val alternatives = getAlternativeKeys(englishKey)
                if (alternatives.isNotEmpty()) {
                    keyClickListener?.onKeyLongPress(englishKey, alternatives)
                    true
                } else {
                    false
                }
            }

            return button
        }

        private fun addFunctionKeyRow() {
            val rowLayout = LinearLayout(context)
            rowLayout.orientation = HORIZONTAL
            rowLayout.layoutParams =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                )

            val heightDp = 56
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()

            // 1. ?123 button (switch to number/punctuation layout)
            val numberButton = createFunctionKeyButton("?123", "LAYOUT_TOGGLE")
            rowLayout.addView(numberButton)

            // 2. Comma button
            val commaButton = createFunctionKeyButton("，", "COMMA")
            rowLayout.addView(commaButton)

            // 3. Globe button (language switch)
            val globeButton = createFunctionKeyButton("🌐", "GLOBE")
            rowLayout.addView(globeButton)

            // 4. Settings button
            val settingsButton = createFunctionKeyButton("⚙️", "SETTINGS")
            rowLayout.addView(settingsButton)

            // 5. Space button with current input method name (2x width)
            spaceButton = createFunctionKeyButton("倉頡", "SPACE")
            spaceButton!!.layoutParams = LinearLayout.LayoutParams(0, heightPx, 2f)
            rowLayout.addView(spaceButton)

            // 6. Period button
            val periodButton = createFunctionKeyButton("。", "PERIOD")
            rowLayout.addView(periodButton)

            // 7. Return button
            val returnButton = createFunctionKeyButton("↵", "ENTER")
            rowLayout.addView(returnButton)

            addView(rowLayout)
        }

        private fun createFunctionKeyButton(
            label: String,
            key: String,
        ): android.widget.Button {
            val button = KeyButton(context)
            button.text = label

            // 設置按鍵尺寸（高度 56dp）
            val heightDp = 56
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()
            button.layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    heightPx,
                    1f,
                )

            button.setOnClickListener {
                keyClickListener?.onKeyClick(key)
            }

            return button
        }

        /**
         * 獲取按鍵的替代字符（長按顯示）
         */
        private fun getAlternativeKeys(key: String): List<String> =
            when (key) {
                // Cangjie layout alternatives (accented characters)
                "a" -> {
                    listOf("@", "á", "à", "â", "ä", "ã")
                }

                "e" -> {
                    listOf("é", "è", "ê", "ë")
                }

                "i" -> {
                    listOf("í", "ì", "î", "ï")
                }

                "o" -> {
                    listOf("ó", "ò", "ô", "ö", "õ")
                }

                "u" -> {
                    listOf("ú", "ù", "û", "ü")
                }

                "n" -> {
                    listOf("ñ")
                }

                "c" -> {
                    listOf("ç")
                }

                "s" -> {
                    listOf("$", "§")
                }

                // Punctuation layout alternatives (Chinese punctuation)
                "，" -> {
                    listOf("、", ",")
                }

                "。" -> {
                    listOf("．", "·", ".")
                }

                "？" -> {
                    listOf("?")
                }

                "！" -> {
                    listOf("!")
                }

                "：" -> {
                    listOf(":")
                }

                "；" -> {
                    listOf(";")
                }

                "（" -> {
                    listOf("【", "(")
                }

                "）" -> {
                    listOf("】", ")")
                }

                "「" -> {
                    listOf(
                        "『",
                        "\"",
                        """)
                "」" -> listOf("』", "\"", """,
                    )
                }

                "—" -> {
                    listOf("–", "-", "－")
                }

                "～" -> {
                    listOf("~")
                }

                "『" -> {
                    listOf("「", "[", "'")
                }

                "』" -> {
                    listOf("」", "]", "'")
                }

                "【" -> {
                    listOf("（", "{")
                }

                "】" -> {
                    listOf("）", "}")
                }

                "《" -> {
                    listOf("〈", "<")
                }

                "》" -> {
                    listOf("〉", ">")
                }

                "、" -> {
                    listOf("，", "/", "\\")
                }

                else -> {
                    emptyList()
                }
            }

        private fun enableAllKeys() {
            // TODO: 啟用所有按鍵
        }

        private fun disableAllKeys() {
            // TODO: 禁用所有按鍵
        }

        private fun showLoadingIndicator() {
            // TODO: 顯示載入指示器
        }

        private fun showErrorMessage(
            message: String,
            canRetry: Boolean,
        ) {
            // TODO: 顯示錯誤訊息與重試按鈕
            // 暫時不實作，由 InputMethodView 處理
        }

        /**
         * 按鍵點擊監聽器
         */
        interface KeyClickListener {
            /**
             * 按鍵點擊
             */
            fun onKeyClick(key: String)

            /**
             * 按鍵長按
             */
            fun onKeyLongPress(
                key: String,
                alternatives: List<String>,
            )
        }
    }

/**
 * 鍵盤狀態
 */
sealed class KeyboardState {
    /**
     * 正常狀態
     */
    object Normal : KeyboardState()

    /**
     * 載入中
     */
    object Loading : KeyboardState()

    /**
     * 錯誤狀態
     */
    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : KeyboardState()
}
