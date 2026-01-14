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
                    "編碼: $code"
                }
        }

        /**
         * 清除編碼
         */
        fun clear() {
            text = ""
        }
    }
