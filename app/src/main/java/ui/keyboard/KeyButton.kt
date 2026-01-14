package ui.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

/**
 * 自定義按鍵視圖
 *
 * 負責：
 * 1. 繪製按鍵外觀
 * 2. 處理按下/釋放狀態
 * 3. 支援按鍵動畫效果
 */
class KeyButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatButton(context, attrs, defStyleAttr) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF = RectF()

        private var isPressed = false

        private val cornerRadius = 8f

        init {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            val textColorId = resources.getIdentifier("key_text", "color", context.packageName)
            setTextColor(resources.getColor(textColorId, null))
            textSize = 14f
            setPadding(4, 4, 4, 4)

            gravity = android.view.Gravity.CENTER

            maxLines = 2
            setLines(2)
        }

        override fun onDraw(canvas: Canvas) {
            rectF.set(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat(),
            )

            val normalBgColor =
                resources.getColor(
                    resources.getIdentifier("key_background", "color", context.packageName),
                    null,
                )
            val pressedBgColor =
                resources.getColor(
                    resources.getIdentifier("key_pressed", "color", context.packageName),
                    null,
                )
            val borderColor = resources.getColor(android.R.color.darker_gray, null)

            paint.color = if (isPressed) pressedBgColor else normalBgColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

            super.onDraw(canvas)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    invalidate()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressed = false
                    invalidate()
                }
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun setPressed(pressed: Boolean) {
            super.setPressed(pressed)
            isPressed = pressed
            invalidate()
        }
    }
