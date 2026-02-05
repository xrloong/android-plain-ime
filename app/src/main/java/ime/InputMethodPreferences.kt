package ime

import android.content.SharedPreferences

class InputMethodPreferences(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_ENABLED_METHODS = "enabled_methods"
        private const val KEY_METHOD_ORDER = "method_order"
        val DEFAULT_ENABLED = setOf("cangjie", "english")
    }

    fun getEnabledMethods(): Set<String> {
        val stored = prefs.getStringSet(KEY_ENABLED_METHODS, null)
        if (stored != null) return stored
        return DEFAULT_ENABLED
    }

    fun setEnabledMethods(enabled: Set<String>) {
        prefs.edit().putStringSet(KEY_ENABLED_METHODS, enabled).apply()
    }

    fun getMethodOrder(): List<String> {
        val stored = prefs.getString(KEY_METHOD_ORDER, null)
        if (stored != null) return stored.split(",")
        return InputMethodMetadata.BUILTIN_METHODS.map { it.id }
    }

    fun setMethodOrder(order: List<String>) {
        prefs.edit().putString(KEY_METHOD_ORDER, order.joinToString(",")).apply()
    }

    fun getOrderedEnabledMethods(): List<InputMethodMetadata> {
        val enabled = getEnabledMethods()
        val result = getMethodOrder()
            .filter { it in enabled }
            .mapNotNull { InputMethodMetadata.getById(it) }
        if (result.isEmpty()) {
            return DEFAULT_ENABLED.mapNotNull { InputMethodMetadata.getById(it) }
        }
        return result
    }
}
