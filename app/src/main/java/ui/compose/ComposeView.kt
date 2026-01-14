package ui.compose

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView

/**
 * 編碼顯示視圖（Composing View）
 *
 * 顯示當前輸入的編碼
 */
class ComposeView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : TextView(context, attrs, defStyleAttr) {
        init {
            textSize = 20f
            val textColorId = resources.getIdentifier("compose_text", "color", context.packageName)
            setTextColor(resources.getColor(textColorId, null))
            gravity = Gravity.CENTER_VERTICAL or Gravity.START

            val padding = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4
            setPadding(padding, padding / 2, padding, padding / 2)

            val bgColorId = resources.getIdentifier("compose_background", "color", context.packageName)
            setBackgroundColor(resources.getColor(bgColorId, null))

            minHeight = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        }

        /**
         * 設置當前編碼
         */
        fun setCode(code: String) {
            text =
                if (code.isEmpty()) {
                    ""
                } else {
                    val rootSequence = codeToRootSequence(code)
                    "字根: $rootSequence"
                }
        }

        /**
         * 清除編碼
         */
        fun clear() {
            text = ""
        }

        private fun codeToRootSequence(code: String): String {
            val keyToRootMap =
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

            return code
                .map { key ->
                    keyToRootMap[key.lowercaseChar()] ?: key.toString()
                }.joinToString("")
        }
    }
