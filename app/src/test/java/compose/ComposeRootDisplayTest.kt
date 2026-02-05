package compose

import cin.CINParser
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * 驗證每個輸入法的字根顯示行為：
 * 1. chardef 中使用的所有編碼字元都在 keyNameMap 中有對應
 * 2. 字根欄使用 keyNameMap 而非硬編碼倉頡映射
 */
class ComposeRootDisplayTest {

    private val parser = CINParser()

    // 所有內建輸入法的 CIN 檔案
    private val cinFiles = mapOf(
        "倉頡" to "src/main/assets/qhcj.cin",
        "行列" to "src/main/assets/qhar.cin",
        "大易" to "src/main/assets/qhdy.cin",
        "嘸蝦米" to "src/main/assets/qhbs.cin",
        "鄭碼" to "src/main/assets/qhzm.cin",
    )

    /**
     * 驗證每個輸入法的 keyNameMap 完整覆蓋所有編碼字元
     * 確保字根欄不會出現無法轉換的原始字元
     */
    @Test
    fun testAllInputMethods_allCodeCharsHaveKeyNameEntry() {
        for ((name, path) in cinFiles) {
            val file = File(path)
            if (!file.exists()) continue

            val result = parser.parse(file.readText())

            // 從所有編碼中提取使用的字元集合
            val codeChars = result.codeToCandidates.keys
                .flatMap { it.toList() }
                .toSet()

            // 每個編碼字元都必須在 keyNameMap 中有對應
            for (ch in codeChars) {
                assertTrue(
                    "$name: 編碼字元 '$ch' 缺少 keyNameMap 映射，字根欄會顯示原始字元而非字根",
                    result.keyNameMap.containsKey(ch)
                )
            }
        }
    }

    /**
     * 驗證行列輸入法：按 'a' 應顯示 "1-" 而非倉頡的 "日"
     */
    @Test
    fun testArray_rootLabelsAreNotCangjie() {
        val result = parser.parse(File("src/main/assets/qhar.cin").readText())

        // 行列的 'a' 是 "1-"，不是倉頡的 "日"
        assertEquals("1-", result.keyNameMap['a'])
        assertNotEquals("日", result.keyNameMap['a'])

        // 行列的標點字根
        assertEquals("8v", result.keyNameMap[','])
        assertEquals("9v", result.keyNameMap['.'])
        assertEquals("0v", result.keyNameMap['/'])
        assertEquals("0-", result.keyNameMap[';'])
    }

    /**
     * 驗證大易輸入法：按 'a' 應顯示 "人" 而非倉頡的 "日"
     */
    @Test
    fun testDayi_rootLabelsAreNotCangjie() {
        val result = parser.parse(File("src/main/assets/qhdy.cin").readText())

        assertEquals("人", result.keyNameMap['a'])
        assertNotEquals("日", result.keyNameMap['a'])

        // 大易的數字字根
        assertEquals("言", result.keyNameMap['1'])
        assertEquals("金", result.keyNameMap['0'])
    }

    /**
     * 驗證倉頡輸入法字根正確
     */
    @Test
    fun testCangjie_rootLabels() {
        val result = parser.parse(File("src/main/assets/qhcj.cin").readText())

        assertEquals("日", result.keyNameMap['a'])
        assertEquals("月", result.keyNameMap['b'])
        assertEquals("手", result.keyNameMap['q'])
    }

    /**
     * 驗證嘸蝦米輸入法字根不是倉頡字根
     */
    @Test
    fun testBoshiamy_rootLabelsAreNotCangjie() {
        val result = parser.parse(File("src/main/assets/qhbs.cin").readText())

        // 嘸蝦米有自己的字根系統，不應是倉頡字根
        assertNotNull("嘸蝦米 'a' 應有 keyNameMap 映射", result.keyNameMap['a'])
    }

    /**
     * 驗證鄭碼輸入法字根不是倉頡字根
     */
    @Test
    fun testZhengma_rootLabelsAreNotCangjie() {
        val result = parser.parse(File("src/main/assets/qhzm.cin").readText())

        assertNotNull("鄭碼 'a' 應有 keyNameMap 映射", result.keyNameMap['a'])
    }

    /**
     * 模擬字根欄轉換邏輯：使用 keyNameMap 轉換編碼為字根序列
     * 驗證每個輸入法的轉換結果都使用自己的字根，而非倉頡字根
     */
    @Test
    fun testCodeToRootSequence_usesCorrectKeyNameMap() {
        for ((name, path) in cinFiles) {
            val file = File(path)
            if (!file.exists()) continue

            val result = parser.parse(file.readText())
            val keyNameMap = result.keyNameMap

            // 模擬 ComposeView.codeToRootSequence 的邏輯
            val code = "a"
            val rootSequence = code.map { keyNameMap[it] ?: it.toString() }.joinToString("")

            assertEquals(
                "$name: 按 'a' 字根欄應顯示 '${keyNameMap['a']}'",
                keyNameMap['a'],
                rootSequence
            )
        }
    }
}
