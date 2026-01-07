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
class KeyButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private var isPressed = false

    // 顏色配置（參考 Gboard 風格）
    private val normalBackgroundColor = 0xFFFFFFFF.toInt()
    private val pressedBackgroundColor = 0xFFE0E0E0.toInt()
    private val textColor = 0xFF000000.toInt()
    private val cornerRadius = 8f

    init {
        // 設置預設樣式
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        setTextColor(textColor)
        textSize = 14f  // 縮小以容納兩行文字
        setPadding(4, 4, 4, 4)

        // 設置文字居中
        gravity = android.view.Gravity.CENTER

        // 允許多行文字
        maxLines = 2
        setLines(2)
    }

    override fun onDraw(canvas: Canvas) {
        // 繪製按鍵背景
        rectF.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat()
        )

        paint.color = if (isPressed) pressedBackgroundColor else normalBackgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // 繪製邊框
        paint.color = 0xFFCCCCCC.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // 繪製文字
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

