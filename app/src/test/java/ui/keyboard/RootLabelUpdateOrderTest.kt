package ui.keyboard

import org.junit.Assert.*
import org.junit.Test

/**
 * 測試字根標籤更新的順序問題
 *
 * 修復方案：使用 updateLayoutAndRoots() 同時更新布局和字根
 * 這確保 setupKeyboard() 時兩者都已正確設置
 */
class RootLabelUpdateOrderTest {

    /**
     * 模擬 QwertyKeyboardView 的核心邏輯
     * 簡化版本，只保留關鍵的狀態和方法
     */
    class MockKeyboardView {
        var currentLayoutConfig: LayoutConfig = LayoutConfigs.CANGJIE
        var currentLayout: KeyboardLayout = KeyboardLayout.Cangjie
        var rootLabelMap: Map<Char, String> = getDefaultCangjieRoots()

        // 記錄每次 setupKeyboard 時的狀態
        data class SetupKeyboardState(
            val methodId: String,
            val rootLabelMap: Map<Char, String>
        )
        val setupKeyboardCalls = mutableListOf<SetupKeyboardState>()

        private fun getDefaultCangjieRoots(): Map<Char, String> = mapOf(
            'q' to "手", 'w' to "田", 'e' to "水", 'r' to "口", 't' to "廿",
            'y' to "卜", 'u' to "山", 'i' to "戈", 'o' to "人", 'p' to "心",
            'a' to "日", 's' to "尸", 'd' to "木", 'f' to "火", 'g' to "土",
            'h' to "竹", 'j' to "十", 'k' to "大", 'l' to "中",
            'z' to "重", 'x' to "難", 'c' to "金", 'v' to "女",
            'b' to "月", 'n' to "弓", 'm' to "一"
        )

        fun updateLayout(layoutConfig: LayoutConfig) {
            currentLayoutConfig = layoutConfig
            currentLayout = layoutConfig.primaryLayout
            setupKeyboard()
        }

        fun updateRootLabels(keyNameMap: Map<Char, String>) {
            rootLabelMap = if (keyNameMap.isEmpty()) {
                getDefaultCangjieRoots()
            } else {
                KeyNameResolver.resolve(keyNameMap, getDefaultCangjieRoots())
            }
            // 重新構建鍵盤以應用新的字根標籤
            if (currentLayout is KeyboardLayout.Cangjie) {
                setupKeyboard()
            }
        }

        /**
         * 同時更新布局配置和字根標籤 - 修復方案
         */
        fun updateLayoutAndRoots(layoutConfig: LayoutConfig, keyNameMap: Map<Char, String>) {
            // 先更新字根標籤
            rootLabelMap = if (keyNameMap.isEmpty()) {
                getDefaultCangjieRoots()
            } else {
                KeyNameResolver.resolve(keyNameMap, getDefaultCangjieRoots())
            }
            // 再更新布局配置
            currentLayoutConfig = layoutConfig
            currentLayout = layoutConfig.primaryLayout
            // 最後重建鍵盤（此時兩者都已正確設置）
            setupKeyboard()
        }

        private fun setupKeyboard() {
            // 記錄當前狀態
            setupKeyboardCalls.add(SetupKeyboardState(
                methodId = currentLayoutConfig.methodId,
                rootLabelMap = rootLabelMap.toMap()
            ))
        }

        fun getRootLabel(key: Char): String = rootLabelMap[key] ?: key.toString().uppercase()
    }

    @Test
    fun testUpdateLayoutAndRoots_dayiRootsAndLayoutCorrect() {
        val view = MockKeyboardView()

        // 模擬大易的 keyNameMap
        val dayiKeyNameMap = mapOf(
            'a' to "人", 'b' to "馬", 'c' to "七", 'd' to "日", 'e' to "一",
            'f' to "土", 'g' to "手", 'h' to "鳥", 'i' to "木", 'j' to "月",
            'k' to "立", 'l' to "女", 'm' to "雨", 'n' to "魚", 'o' to "口",
            'p' to "耳", 'q' to "石", 'r' to "工", 's' to "革", 't' to "糸",
            'u' to "艸", 'v' to "禾", 'w' to "山", 'x' to "水", 'y' to "火",
            'z' to "心"
        )

        // 使用原子更新方法
        view.updateLayoutAndRoots(LayoutConfigs.DAYI, dayiKeyNameMap)

        // setupKeyboard 只被調用一次
        assertEquals("setupKeyboard should be called once", 1, view.setupKeyboardCalls.size)

        // 驗證 setupKeyboard 時使用的是大易布局和大易字根
        assertEquals("Setup should use Dayi layout", "dayi", view.setupKeyboardCalls[0].methodId)
        assertEquals("Setup should use Dayi roots for 'a'", "人", view.setupKeyboardCalls[0].rootLabelMap['a'])
        assertEquals("Setup should use Dayi roots for 'q'", "石", view.setupKeyboardCalls[0].rootLabelMap['q'])

        // 驗證最終狀態是正確的
        assertEquals("Final rootLabelMap should have Dayi roots for 'a'", "人", view.getRootLabel('a'))
        assertEquals("Final rootLabelMap should have Dayi roots for 'q'", "石", view.getRootLabel('q'))
        assertEquals("Final layoutConfig should be Dayi", "dayi", view.currentLayoutConfig.methodId)
    }

    @Test
    fun testUpdateLayoutAndRoots_arrayRootsAndLayoutCorrect() {
        val view = MockKeyboardView()

        // 模擬行列的 keyNameMap
        val arrayKeyNameMap = mapOf(
            'a' to "1-", 'b' to "5v", 'c' to "3v", 'd' to "3-", 'e' to "3^",
            'f' to "4-", 'g' to "5-", 'h' to "6-", 'i' to "8^", 'j' to "7-",
            'k' to "8-", 'l' to "9-", 'm' to "7v", 'n' to "6v", 'o' to "9^",
            'p' to "0^", 'q' to "1^", 'r' to "4^", 's' to "2-", 't' to "5^",
            'u' to "7^", 'v' to "4v", 'w' to "2^", 'x' to "2v", 'y' to "6^",
            'z' to "1v"
        )

        // 使用原子更新方法
        view.updateLayoutAndRoots(LayoutConfigs.ARRAY, arrayKeyNameMap)

        // setupKeyboard 只被調用一次
        assertEquals("setupKeyboard should be called once", 1, view.setupKeyboardCalls.size)

        // 驗證 setupKeyboard 時使用的是行列布局和行列字根
        assertEquals("Setup should use Array layout", "array", view.setupKeyboardCalls[0].methodId)
        assertEquals("Setup should use Array roots for 'a'", "1-", view.setupKeyboardCalls[0].rootLabelMap['a'])
        assertEquals("Setup should use Array roots for 'q'", "1^", view.setupKeyboardCalls[0].rootLabelMap['q'])

        // 驗證最終狀態是正確的
        assertEquals("Final rootLabelMap should have Array roots for 'a'", "1-", view.getRootLabel('a'))
        assertEquals("Final rootLabelMap should have Array roots for 'q'", "1^", view.getRootLabel('q'))
        assertEquals("Final layoutConfig should be Array", "array", view.currentLayoutConfig.methodId)
    }

    @Test
    fun testOldMethod_showsBug() {
        // 這個測試演示舊方法的問題：分開調用時，第一次 setupKeyboard 使用錯誤的配置
        val view = MockKeyboardView()

        val dayiKeyNameMap = mapOf(
            'a' to "人", 'q' to "石"
        )

        // 舊方法：先更新字根，再更新布局（分開調用）
        view.updateRootLabels(dayiKeyNameMap)  // 字根更新，但 layout 還是 cangjie
        view.updateLayout(LayoutConfigs.DAYI)  // 布局更新為 dayi

        // 會調用兩次 setupKeyboard
        assertEquals("setupKeyboard should be called twice with old method", 2, view.setupKeyboardCalls.size)

        // 第一次 setupKeyboard：字根是大易的，但布局是倉頡的（錯誤！）
        assertEquals("First setup has wrong layout (cangjie instead of dayi)",
            "cangjie", view.setupKeyboardCalls[0].methodId)

        // 第二次 setupKeyboard：兩者都是大易的（正確）
        assertEquals("Second setup has correct layout",
            "dayi", view.setupKeyboardCalls[1].methodId)
    }
}
