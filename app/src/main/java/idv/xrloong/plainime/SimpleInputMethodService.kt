package idv.xrloong.plainime

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button

class SimpleInputMethodService : InputMethodService() {
    override fun onCreateInputView(): View {
        val parent = window?.window?.decorView as? ViewGroup
        val keyboardView = LayoutInflater.from(this).inflate(R.layout.keyboard_view, parent, false)
        keyboardView.findViewById<Button>(R.id.key_a).setOnClickListener { currentInputConnection.commitText("A", 1) }
        keyboardView.findViewById<Button>(R.id.key_b).setOnClickListener { currentInputConnection.commitText("B", 1) }
        keyboardView.findViewById<Button>(R.id.key_c).setOnClickListener { currentInputConnection.commitText("C", 1) }
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // TODO: 初始化輸入法狀態
    }
}
