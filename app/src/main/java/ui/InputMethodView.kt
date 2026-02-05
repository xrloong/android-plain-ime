package ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ui.candidate.CandidateView
import ui.compose.ComposeView
import ui.keyboard.KeyboardLayout
import ui.keyboard.KeyboardState
import ui.keyboard.LayoutConfig
import ui.keyboard.QwertyKeyboardView

/**
 * 輸入法主視圖
 *
 * 整合所有 UI 元件：
 * 1. 編碼顯示
 * 2. 候選字列表
 * 3. QWERTY 鍵盤
 * 4. 載入/錯誤提示
 */
class InputMethodView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        private val composeView: ComposeView
        private val candidateView: CandidateView
        private val keyboardView: QwertyKeyboardView
        private val loadingView: ProgressBar
        private val errorView: LinearLayout
        private val errorMessageView: TextView
        private val retryButton: Button

        private var inputMethodListener: InputMethodListener? = null
        private var viewModel: InputMethodViewModel? = null

        init {
            orientation = VERTICAL

            // 設置背景色（與鍵盤背景一致，確保導航列區域不透明）
            val keyboardBgColor =
                resources.getColor(
                    resources.getIdentifier("keyboard_background", "color", context.packageName),
                    null,
                )
            setBackgroundColor(keyboardBgColor)

            // 編碼顯示
            composeView =
                ComposeView(context).apply {
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT,
                        )
                }
            addView(composeView)

            // 候選字視圖
            candidateView =
                CandidateView(context).apply {
                    val heightDp = 48
                    val heightPx = (heightDp * resources.displayMetrics.density).toInt()
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            heightPx,
                        )

                    setCandidateClickListener(
                        object : CandidateView.CandidateClickListener {
                            override fun onCandidateClick(
                                candidate: Char,
                                index: Int,
                            ) {
                                inputMethodListener?.onCandidateSelected(candidate, index)
                            }
                        },
                    )
                }
            addView(candidateView)

            // 載入指示器
            loadingView =
                ProgressBar(context).apply {
                    layoutParams =
                        LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT,
                        ).apply {
                            gravity = android.view.Gravity.CENTER
                        }
                    visibility = GONE
                }
            addView(loadingView)

            // 錯誤視圖
            errorView =
                LinearLayout(context).apply {
                    orientation = VERTICAL
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT,
                        )
                    setPadding(32, 16, 32, 16)
                    visibility = GONE

                    // 錯誤訊息
                    errorMessageView =
                        TextView(context).apply {
                            textSize = 16f
                            setTextColor(0xFFD32F2F.toInt())
                            gravity = android.view.Gravity.CENTER
                        }
                    addView(errorMessageView)

                    // 重試按鈕
                    retryButton =
                        Button(context).apply {
                            text = "重試"
                            layoutParams =
                                LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT,
                                ).apply {
                                    gravity = android.view.Gravity.CENTER
                                    topMargin = 16
                                }
                            setOnClickListener {
                                inputMethodListener?.onRetryRequested()
                            }
                        }
                    addView(retryButton)
                }
            addView(errorView)

            // QWERTY 鍵盤
            keyboardView =
                QwertyKeyboardView(context).apply {
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT,
                        )

                    setKeyClickListener(
                        object : QwertyKeyboardView.KeyClickListener {
                            override fun onKeyClick(key: String) {
                                inputMethodListener?.onKeyPressed(key)
                            }

                            override fun onKeyLongPress(
                                key: String,
                                alternatives: List<String>,
                            ) {
                                inputMethodListener?.onKeyLongPressed(key, alternatives)
                            }
                        },
                    )
                }
            addView(keyboardView)

            // 處理 Window Insets（系統導航欄等）
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                setPadding(paddingLeft, paddingTop, paddingRight, navBarInsets.bottom)
                insets
            }
        }

        /**
         * 設置輸入法監聽器
         */
        fun setInputMethodListener(listener: InputMethodListener) {
            this.inputMethodListener = listener
        }

        /**
         * 設置 ViewModel 並綁定觀察器
         * 用於監聽輸入法切換、布局變化等
         */
        fun setViewModel(
            vm: InputMethodViewModel,
            lifecycleOwner: LifecycleOwner,
        ) {
            this.viewModel = vm
            setupViewModelObservers(lifecycleOwner)
        }

        /**
         * 設置 ViewModel 觀察器
         * 監聽布局配置、字根標籤、輸入法名稱的變化
         */
        private fun setupViewModelObservers(lifecycleOwner: LifecycleOwner) {
            viewModel?.let { vm ->
                // 監聽布局配置變化
                vm.currentLayoutConfig.observe(lifecycleOwner) { layoutConfig ->
                    onLayoutConfigChanged(layoutConfig)
                }

                // 監聽字根標籤變化
                vm.currentKeyNameMap.observe(lifecycleOwner) { keyNameMap ->
                    keyboardView.updateRootLabels(keyNameMap)
                }

                // 監聽輸入法名稱變化
                vm.currentInputMethodName.observe(lifecycleOwner) { name ->
                    keyboardView.updateSpaceBarLabel(name)
                }
            }
        }

        /**
         * 布局配置變化時的處理
         */
        private fun onLayoutConfigChanged(layoutConfig: LayoutConfig) {
            keyboardView.updateLayout(layoutConfig)
        }

        /**
         * 更新編碼顯示
         */
        fun updateCode(code: String) {
            composeView.setCode(code)
        }

        /**
         * 更新候選字列表
         */
        fun updateCandidates(candidates: List<Char>) {
            candidateView.setCandidates(candidates)
        }

        /**
         * 設置鍵盤狀態
         */
        fun setKeyboardState(state: KeyboardState) {
            when (state) {
                is KeyboardState.Normal -> {
                    loadingView.visibility = GONE
                    errorView.visibility = GONE
                    keyboardView.visibility = VISIBLE
                    keyboardView.setKeyboardState(state)
                }

                is KeyboardState.Loading -> {
                    loadingView.visibility = VISIBLE
                    errorView.visibility = GONE
                    keyboardView.visibility = VISIBLE
                    keyboardView.setKeyboardState(state)
                }

                is KeyboardState.Error -> {
                    loadingView.visibility = GONE
                    errorView.visibility = VISIBLE
                    keyboardView.visibility = VISIBLE

                    errorMessageView.text = state.message
                    retryButton.visibility = if (state.canRetry) VISIBLE else GONE

                    keyboardView.setKeyboardState(state)
                }
            }
        }

        /**
         * 清除所有顯示
         */
        fun clear() {
            composeView.clear()
            candidateView.setCandidates(emptyList())
        }

        /**
         * Toggle keyboard layout (Cangjie <-> Punctuation)
         */
        fun toggleKeyboardLayout() {
            keyboardView.toggleLayout()
        }

        /**
         * Toggle input method (English <-> Cangjie)
         */
        fun toggleInputMethod() {
            keyboardView.toggleInputMethod()
        }

        /**
         * Get current keyboard layout
         */
        fun getCurrentKeyboardLayout(): KeyboardLayout = keyboardView.getCurrentLayout()

        /**
         * 更新鍵盤字根標籤
         */
        fun updateKeyboardRootLabels(keyNameMap: Map<Char, String>) {
            keyboardView.updateRootLabels(keyNameMap)
            composeView.updateKeyNameMap(keyNameMap)
        }

        /**
         * 更新空白鍵標籤（顯示輸入法名稱）
         */
        fun updateSpaceBarLabel(displayName: String) {
            keyboardView.updateSpaceBarLabel(displayName)
        }

        /**
         * 更新鍵盤布局配置
         * 用於切換輸入法時更新整個鍵盤布局結構
         */
        fun updateLayoutConfig(layoutConfig: LayoutConfig) {
            keyboardView.updateLayout(layoutConfig)
        }

        /**
         * 輸入法監聽器
         */
        interface InputMethodListener {
            /**
             * 按鍵按下
             */
            fun onKeyPressed(key: String)

            /**
             * 按鍵長按
             */
            fun onKeyLongPressed(
                key: String,
                alternatives: List<String>,
            )

            /**
             * 候選字選擇
             */
            fun onCandidateSelected(
                candidate: Char,
                index: Int,
            )

            /**
             * 重試請求
             */
            fun onRetryRequested()
        }
    }
