package ui.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import idv.xrloong.plainime.R
import ime.InputMethodMetadata
import java.util.Collections

data class InputMethodItem(
    val metadata: InputMethodMetadata,
    var enabled: Boolean
)

class InputMethodItemAdapter(
    private val items: MutableList<InputMethodItem>,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<InputMethodItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dragHandle: ImageView = view.findViewById(R.id.dragHandle)
        val methodName: TextView = view.findViewById(R.id.methodName)
        val enableSwitch: SwitchCompat = view.findViewById(R.id.enableSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_input_method, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.methodName.text = item.metadata.displayName
        holder.enableSwitch.setOnCheckedChangeListener(null)
        holder.enableSwitch.isChecked = item.enabled
        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Prevent disabling the last enabled method
            if (!isChecked && items.count { it.enabled } <= 1) {
                holder.enableSwitch.isChecked = true
                return@setOnCheckedChangeListener
            }
            item.enabled = isChecked
            onChanged()
        }
        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onStartDrag(holder)
            }
            false
        }
    }

    override fun getItemCount() = items.size

    fun moveItem(from: Int, to: Int) {
        Collections.swap(items, from, to)
        notifyItemMoved(from, to)
        onChanged()
    }

    fun getItems(): List<InputMethodItem> = items.toList()
}
