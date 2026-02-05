package ui.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import idv.xrloong.plainime.R
import ime.InputMethodMetadata
import ime.InputMethodPreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var adapter: InputMethodItemAdapter
    private lateinit var preferences: InputMethodPreferences
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("plain_ime_prefs", Context.MODE_PRIVATE)
        preferences = InputMethodPreferences(prefs)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val enabledSet = preferences.getEnabledMethods()
        val orderedIds = preferences.getMethodOrder()

        // Build items list: ordered IDs first, then any missing ones
        val items = mutableListOf<InputMethodItem>()
        for (id in orderedIds) {
            val meta = InputMethodMetadata.getById(id) ?: continue
            items.add(InputMethodItem(meta, id in enabledSet))
        }
        // Add any methods not in the saved order (e.g., newly added)
        for (meta in InputMethodMetadata.BUILTIN_METHODS) {
            if (items.none { it.metadata.id == meta.id }) {
                items.add(InputMethodItem(meta, meta.id in enabledSet))
            }
        }

        adapter = InputMethodItemAdapter(items, { holder ->
            itemTouchHelper?.startDrag(holder)
        }, {
            savePreferences()
        })

        recyclerView.adapter = adapter

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper!!.attachToRecyclerView(recyclerView)
    }

    private fun savePreferences() {
        val items = adapter.getItems()
        preferences.setMethodOrder(items.map { it.metadata.id })
        preferences.setEnabledMethods(items.filter { it.enabled }.map { it.metadata.id }.toSet())
    }
}
