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

        init {
            orientation = VERTICAL
            // 設置鍵盤背景色
            setBackgroundColor(0xFFEEEEEE.toInt())
            // 設置間距
            val paddingDp = 4
            val paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            // 初始化鍵盤佈局
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

        private fun setupKeyboard() {
            // 第一行：手 田 水 口 廿 卜 山 戈 人 心
            addKeyRow(
                listOf(
                    "q\n手",
                    "w\n田",
                    "e\n水",
                    "r\n口",
                    "t\n廿",
                    "y\n卜",
                    "u\n山",
                    "i\n戈",
                    "o\n人",
                    "p\n心",
                ),
            )

            // 第二行：日 尸 木 火 土 竹 十 大 中
            addKeyRow(
                listOf(
                    "a\n日",
                    "s\n尸",
                    "d\n木",
                    "f\n火",
                    "g\n土",
                    "h\n竹",
                    "j\n十",
                    "k\n大",
                    "l\n中",
                ),
            )

            // 第三行：重 難 金 女 月 弓 一
            addKeyRow(
                listOf(
                    "z\n重",
                    "x\n難",
                    "c\n金",
                    "v\n女",
                    "b\n月",
                    "n\n弓",
                    "m\n一",
                ),
            )

            // 第四行：功能鍵
            addFunctionKeyRow()
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

        private fun createKeyButton(key: String): android.widget.Button {
            val button = KeyButton(context)

            // 提取英文字母部分（用於發送按鍵事件）
            val englishKey = key.substringBefore('\n')

            // 顯示完整的文字（字母\n字根）
            button.text = key

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

            // Enter 鍵
            val enterButton = createFunctionKeyButton("確認", "ENTER")
            rowLayout.addView(enterButton)

            // Space 鍵
            val spaceButton = createFunctionKeyButton("空白", "SPACE")
            val heightDp = 56
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()
            spaceButton.layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    heightPx,
                    3f,
                )
            rowLayout.addView(spaceButton)

            // Backspace 鍵
            val backspaceButton = createFunctionKeyButton("⌫", "BACKSPACE")
            rowLayout.addView(backspaceButton)

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
                "a" -> listOf("@", "á", "à", "â", "ä", "ã")
                "e" -> listOf("é", "è", "ê", "ë")
                "i" -> listOf("í", "ì", "î", "ï")
                "o" -> listOf("ó", "ò", "ô", "ö", "õ")
                "u" -> listOf("ú", "ù", "û", "ü")
                "n" -> listOf("ñ")
                "c" -> listOf("ç")
                "s" -> listOf("$", "§")
                "." -> listOf(",", "!", "?", ":", ";")
                else -> emptyList()
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
