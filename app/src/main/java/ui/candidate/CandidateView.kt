package ui.candidate

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 候選字視圖
 *
 * 負責：
 * 1. 顯示候選字列表
 * 2. 處理候選字點擊
 * 3. 支援橫向滾動
 */
class CandidateView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : HorizontalScrollView(context, attrs, defStyleAttr) {
        private val candidateLayout: LinearLayout
        private var candidateClickListener: CandidateClickListener? = null

        init {
            val bgColor =
                resources.getColor(
                    resources.getIdentifier("candidate_background", "color", context.packageName),
                    null,
                )
            setBackgroundColor(bgColor)

            candidateLayout =
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams =
                        LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.MATCH_PARENT,
                        )
                    gravity = Gravity.CENTER_VERTICAL
                    // Reduced vertical padding to prevent text cutoff
                    val horizontalPadding = (16 * resources.displayMetrics.density).toInt()
                    val verticalPadding = (4 * resources.displayMetrics.density).toInt()
                    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                }

            addView(candidateLayout)
        }

        /**
         * 設置候選字列表
         */
        fun setCandidates(candidates: List<Char>) {
            candidateLayout.removeAllViews()

            if (candidates.isEmpty()) {
                // 無候選字時顯示提示
                addEmptyView()
                return
            }

            candidates.forEachIndexed { index, candidate ->
                val candidateView = createCandidateView(candidate, index)
                candidateLayout.addView(candidateView)

                // 添加分隔線（最後一個除外）
                if (index < candidates.size - 1) {
                    candidateLayout.addView(createDivider())
                }
            }

            // 滾動到開始位置
            post { scrollTo(0, 0) }
        }

        /**
         * 設置候選字點擊監聽器
         */
        fun setCandidateClickListener(listener: CandidateClickListener) {
            this.candidateClickListener = listener
        }

        private fun createCandidateView(
            candidate: Char,
            index: Int,
        ): TextView =
            TextView(context).apply {
                text = candidate.toString()
                textSize = 24f
                val textColorId = resources.getIdentifier("candidate_text", "color", context.packageName)
                setTextColor(resources.getColor(textColorId, null))
                gravity = Gravity.CENTER

                // Reduced padding to prevent text cutoff
                val horizontalPadding = (12 * resources.displayMetrics.density).toInt()
                val verticalPadding = (4 * resources.displayMetrics.density).toInt()
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

                layoutParams =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )

                // 設置點擊事件
                setOnClickListener {
                    candidateClickListener?.onCandidateClick(candidate, index)
                }

                // 設置點擊效果
                isClickable = true
                isFocusable = true
                background = createRippleDrawable()
            }

        private fun createDivider(): android.view.View =
            android.view.View(context).apply {
                layoutParams =
                    LinearLayout
                        .LayoutParams(
                            1,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ).apply {
                            topMargin = 8
                            bottomMargin = 8
                        }
                val dividerColorId = resources.getIdentifier("candidate_divider", "color", context.packageName)
                setBackgroundColor(resources.getColor(dividerColorId, null))
            }

        private fun addEmptyView() {
            val emptyView =
                TextView(context).apply {
                    text = "無候選字"
                    textSize = 16f
                    val emptyTextColorId = resources.getIdentifier("candidate_empty_text", "color", context.packageName)
                    setTextColor(resources.getColor(emptyTextColorId, null))
                    gravity = Gravity.CENTER

                    // Reduced padding to prevent text cutoff
                    val horizontalPadding = (16 * resources.displayMetrics.density).toInt()
                    val verticalPadding = (4 * resources.displayMetrics.density).toInt()
                    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                }
            candidateLayout.addView(emptyView)
        }

        private fun createRippleDrawable(): android.graphics.drawable.Drawable {
            // 簡單的選中效果
            val stateListDrawable = android.graphics.drawable.StateListDrawable()

            val pressedDrawable = android.graphics.drawable.ColorDrawable(0xFFE0E0E0.toInt())
            stateListDrawable.addState(
                intArrayOf(android.R.attr.state_pressed),
                pressedDrawable,
            )

            val normalDrawable = android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            stateListDrawable.addState(
                intArrayOf(),
                normalDrawable,
            )

            return stateListDrawable
        }

        /**
         * 候選字點擊監聽器
         */
        interface CandidateClickListener {
            fun onCandidateClick(
                candidate: Char,
                index: Int,
            )
        }
    }
