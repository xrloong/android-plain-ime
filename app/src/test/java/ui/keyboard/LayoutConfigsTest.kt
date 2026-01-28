package ui.keyboard

import org.junit.Assert.*
import org.junit.Test

/**
 * LayoutConfigs 的單元測試
 *
 * 測試所有 5 種輸入法的布局配置
 */
class LayoutConfigsTest {
    @Test
    fun testLayoutConfigs_allMethods_haveConfig() {
        // 驗證所有 5 種輸入法都有配置
        val methods = listOf("cangjie", "boshiamy", "zhengma", "array", "dayi")
        for (methodId in methods) {
            val config = LayoutConfigs.getConfig(methodId)
            assertNotNull("Method $methodId should have a config", config)
            assertEquals("MethodId should match", methodId, config.methodId)
        }
    }

    @Test
    fun testCangjieLayout_noAdditionalKeys() {
        // 倉頡：沒有額外按鍵
        val config = LayoutConfigs.getConfig("cangjie")
        assertTrue("Cangjie should have no additional keys", config.additionalKeyRows.isEmpty())
        assertEquals("Cangjie should use Cangjie layout", KeyboardLayout.Cangjie, config.primaryLayout)
    }

    @Test
    fun testBoshiamyLayout_noAdditionalKeys() {
        // 嘸蝦米：沒有額外按鍵
        val config = LayoutConfigs.getConfig("boshiamy")
        assertTrue("Boshiamy should have no additional keys", config.additionalKeyRows.isEmpty())
        assertEquals("Boshiamy should use Cangjie layout", KeyboardLayout.Cangjie, config.primaryLayout)
    }

    @Test
    fun testZhengmaLayout_noAdditionalKeys() {
        // 鄭碼：沒有額外按鍵
        val config = LayoutConfigs.getConfig("zhengma")
        assertTrue("Zhengma should have no additional keys", config.additionalKeyRows.isEmpty())
        assertEquals("Zhengma should use Cangjie layout", KeyboardLayout.Cangjie, config.primaryLayout)
    }

    @Test
    fun testArrayLayout_noAdditionalKeys() {
        // 行列：沒有額外按鍵（已簡化）
        val config = LayoutConfigs.getConfig("array")
        assertTrue("Array should have no additional keys", config.additionalKeyRows.isEmpty())
        assertEquals("Array should use Cangjie layout", KeyboardLayout.Cangjie, config.primaryLayout)
        assertEquals("Array methodId should be 'array'", "array", config.methodId)
    }

    @Test
    fun testDayiLayout_noAdditionalKeys() {
        // 大易：沒有額外按鍵（已簡化）
        val config = LayoutConfigs.getConfig("dayi")
        assertTrue("Dayi should have no additional keys", config.additionalKeyRows.isEmpty())
        assertEquals("Dayi should use Cangjie layout", KeyboardLayout.Cangjie, config.primaryLayout)
        assertEquals("Dayi methodId should be 'dayi'", "dayi", config.methodId)
    }

    @Test
    fun testUnknownMethod_returnsCanjieAsDefault() {
        // 未知輸入法：返回倉頡配置作為默認值
        val config = LayoutConfigs.getConfig("unknown_method")
        assertNotNull("Unknown method should return default config", config)
        assertEquals("Should default to cangjie", "cangjie", config.methodId)
    }

    @Test
    fun testGetAllConfigs_returns5Configs() {
        // 獲取所有配置
        val allConfigs = LayoutConfigs.getAllConfigs()
        assertEquals("Should have 5 input methods", 5, allConfigs.size)

        val methodIds = allConfigs.map { it.methodId }
        assertTrue("Should contain cangjie", methodIds.contains("cangjie"))
        assertTrue("Should contain boshiamy", methodIds.contains("boshiamy"))
        assertTrue("Should contain zhengma", methodIds.contains("zhengma"))
        assertTrue("Should contain array", methodIds.contains("array"))
        assertTrue("Should contain dayi", methodIds.contains("dayi"))
    }

    @Test
    fun testLayoutConfig_displayNames() {
        // 驗證顯示名稱
        assertEquals("Cangjie display name", "倉頡", LayoutConfigs.CANGJIE.displayName)
        assertEquals("Boshiamy display name", "嘸蝦米", LayoutConfigs.BOSHIAMY.displayName)
        assertEquals("Zhengma display name", "鄭碼", LayoutConfigs.ZHENGMA.displayName)
        assertEquals("Array display name", "行列", LayoutConfigs.ARRAY.displayName)
        assertEquals("Dayi display name", "大易", LayoutConfigs.DAYI.displayName)
    }
}
