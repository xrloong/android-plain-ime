package ui.keyboard

import org.junit.Assert.*
import org.junit.Test

/**
 * KeyNameResolver 的單元測試
 *
 * 測試字根標籤的 Fallback 機制
 */
class KeyNameResolverTest {
    @Test
    fun testResolve_completeKeyNameMap_noFallback() {
        // 完整的字根映射，不需要 Fallback
        val input =
            mapOf(
                'q' to "手",
                'w' to "廿",
                'e' to "水",
            )

        val result = KeyNameResolver.resolve(input)
        assertEquals("Should preserve provided values", "手", result['q'])
        assertEquals("Should preserve provided values", "廿", result['w'])
        assertEquals("Should preserve provided values", "水", result['e'])
    }

    @Test
    fun testResolve_missingKeys_usesFallback() {
        // 缺失的字根：使用倉頡作為 Fallback
        val input = mapOf('q' to "手")
        val result = KeyNameResolver.resolve(input)

        assertEquals("Should preserve provided values", "手", result['q'])
        assertNotNull("Should have fallback for w", result['w'])
    }

    @Test
    fun testResolve_emptyInput_usesFallback() {
        // 空的輸入：使用 Fallback（倉頡參考）
        val input = emptyMap<Char, String>()
        val result = KeyNameResolver.resolve(input)

        // 應該使用倉頡參考字根
        assertEquals("Should use cangjie fallback for q", "手", result['q'])
        assertEquals("Should use cangjie fallback for w", "田", result['w'])
    }

    @Test
    fun testResolve_allKeys_haveLabels() {
        // 驗證所有鍵盤按鍵都有標籤
        val input = mapOf('a' to "測試")
        val result = KeyNameResolver.resolve(input)

        // 驗證所有 QWERTY 鍵都有標籤
        val keyboardChars =
            setOf(
                'q',
                'w',
                'e',
                'r',
                't',
                'y',
                'u',
                'i',
                'o',
                'p',
                'a',
                's',
                'd',
                'f',
                'g',
                'h',
                'j',
                'k',
                'l',
                'z',
                'x',
                'c',
                'v',
                'b',
                'n',
                'm',
            )

        for (char in keyboardChars) {
            assertNotNull("Key $char should have a label", result[char])
            assertFalse("Label should not be empty", result[char]?.isEmpty() ?: true)
        }
    }

    @Test
    fun testResolve_preservesProvidedValues() {
        // 提供的值應該被保留，不被 Fallback 覆蓋
        val input =
            mapOf(
                'q' to "自定義Q",
                'w' to "自定義W",
            )
        val result = KeyNameResolver.resolve(input)

        assertEquals("Should preserve custom q label", "自定義Q", result['q'])
        assertEquals("Should preserve custom w label", "自定義W", result['w'])
    }

    @Test
    fun testWithFallback_extensionFunction() {
        // 測試擴展函數語法糖
        val input = mapOf('a' to "日")
        // 直接調用 resolve 方法
        val result = KeyNameResolver.resolve(input)

        assertNotNull("Should have result", result)
        assertEquals("Should preserve provided value", "日", result['a'])
    }

    @Test
    fun testResolve_customFallback() {
        // 測試自定義 Fallback 映射
        val input = mapOf('q' to "手")
        val customFallback =
            mapOf(
                'w' to "自定義W",
                'e' to "自定義E",
            )

        val result = KeyNameResolver.resolve(input, customFallback)
        assertEquals("Should use custom fallback", "自定義W", result['w'])
        assertEquals("Should use custom fallback", "自定義E", result['e'])
    }
}
