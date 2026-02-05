package ime

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * InputMethodPreferences 單元測試
 *
 * 使用 FakeSharedPreferences 來避免對 Android 框架的依賴
 */
class InputMethodPreferencesTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var preferences: InputMethodPreferences

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        preferences = InputMethodPreferences(fakePrefs)
    }

    // ===== getEnabledMethods 測試 =====

    @Test
    fun testGetEnabledMethods_default_returnsCangieAndEnglish() {
        val enabled = preferences.getEnabledMethods()

        assertEquals(setOf("cangjie", "english"), enabled)
    }

    // ===== setEnabledMethods 測試 =====

    @Test
    fun testSetEnabledMethods_persistsAndReturnsCorrectly() {
        val newEnabled = setOf("cangjie", "array", "dayi")
        preferences.setEnabledMethods(newEnabled)

        val result = preferences.getEnabledMethods()
        assertEquals(newEnabled, result)
    }

    // ===== getMethodOrder 測試 =====

    @Test
    fun testGetMethodOrder_default_returnsBuiltinOrder() {
        val order = preferences.getMethodOrder()

        val expectedOrder = InputMethodMetadata.BUILTIN_METHODS.map { it.id }
        assertEquals(expectedOrder, order)
    }

    // ===== setMethodOrder 測試 =====

    @Test
    fun testSetMethodOrder_persistsAndReturnsCorrectly() {
        val newOrder = listOf("english", "cangjie", "array")
        preferences.setMethodOrder(newOrder)

        val result = preferences.getMethodOrder()
        assertEquals(newOrder, result)
    }

    // ===== getOrderedEnabledMethods 測試 =====

    @Test
    fun testGetOrderedEnabledMethods_filtersAndOrdersCorrectly() {
        // 啟用 cangjie, array, english
        preferences.setEnabledMethods(setOf("cangjie", "array", "english"))
        // 設定順序：english 在前，然後 array，最後 cangjie
        preferences.setMethodOrder(listOf("english", "array", "cangjie"))

        val result = preferences.getOrderedEnabledMethods()

        assertEquals(3, result.size)
        assertEquals("english", result[0].id)
        assertEquals("array", result[1].id)
        assertEquals("cangjie", result[2].id)
    }

    @Test
    fun testGetOrderedEnabledMethods_emptyEnabled_fallsBackToDefault() {
        // 設定一個空的啟用集合（不含任何有效的輸入法 ID）
        preferences.setEnabledMethods(setOf("nonexistent"))

        val result = preferences.getOrderedEnabledMethods()

        // 應該回退到 DEFAULT_ENABLED
        val defaultIds = InputMethodPreferences.DEFAULT_ENABLED
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.id in defaultIds })
    }

    // ===== FakeSharedPreferences 實作 =====

    /**
     * 最小化的 SharedPreferences 假實作，用於單元測試。
     * 僅實作 InputMethodPreferences 使用到的方法。
     */
    private class FakeSharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()

        override fun getString(key: String?, defValue: String?): String? {
            return if (data.containsKey(key)) data[key] as? String else defValue
        }

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
            if (!data.containsKey(key)) return defValues
            @Suppress("UNCHECKED_CAST")
            val stored = data[key] as? Set<String> ?: return defValues
            return stored.toMutableSet()
        }

        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun getAll(): MutableMap<String, *> = data.toMutableMap()

        override fun edit(): SharedPreferences.Editor = FakeEditor(data)

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {}

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {}
    }

    private class FakeEditor(private val data: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            if (key != null) pending[key] = values?.toSet()
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) pending[key] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clear = true
            return this
        }

        override fun commit(): Boolean {
            applyChanges()
            return true
        }

        override fun apply() {
            applyChanges()
        }

        private fun applyChanges() {
            if (clear) data.clear()
            for ((key, value) in pending) {
                if (value == null) {
                    data.remove(key)
                } else {
                    data[key] = value
                }
            }
        }
    }
}
