package engine

import cin.CINParseResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * InputMethodEngine 單元測試
 */
class InputMethodEngineTest {

    private lateinit var engine: InputMethodEngine
    private lateinit var testTableData: CINParseResult

    @Before
    fun setup() {
        // 建立測試查表資料
        testTableData = createTestTableData()
        engine = InputMethodEngine(testTableData)
    }

    // ===== 基本輸入測試 =====

    @Test
    fun testProcessKey_validKey_success() {
        val result = engine.processKey('a')

        assertTrue(result)
        assertEquals("a", engine.getCurrentCode())
    }

    @Test
    fun testProcessKey_invalidKey_failure() {
        val result = engine.processKey('1')

        assertFalse(result)
        assertEquals("", engine.getCurrentCode())
    }

    @Test
    fun testProcessKey_multipleKeys_cumulativeCode() {
        engine.processKey('a')
        engine.processKey('b')
        engine.processKey('c')

        assertEquals("abc", engine.getCurrentCode())
    }

    @Test
    fun testProcessKey_uppercaseKey_convertedToLowercase() {
        engine.processKey('A')
        engine.processKey('B')

        assertEquals("ab", engine.getCurrentCode())
    }

    // ===== 候選字查詢測試 =====

    @Test
    fun testGetCandidates_singleKey_returnsCandidates() {
        engine.processKey('a')

        val candidates = engine.getCandidates()
        assertEquals(2, candidates.size)
        assertTrue(candidates.contains('日'))
        assertTrue(candidates.contains('曰'))
    }

    @Test
    fun testGetCandidates_multipleKeys_returnsCandidates() {
        engine.processKey('a')
        engine.processKey('a')

        val candidates = engine.getCandidates()
        assertEquals(1, candidates.size)
        assertEquals('昌', candidates[0])
    }

    @Test
    fun testGetCandidates_noMatch_returnsEmpty() {
        engine.processKey('x')
        engine.processKey('y')
        engine.processKey('z')

        val candidates = engine.getCandidates()
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun testGetCandidates_noInput_returnsEmpty() {
        val candidates = engine.getCandidates()

        assertTrue(candidates.isEmpty())
    }

    // ===== 選擇候選字測試 =====

    @Test
    fun testSelectCandidate_validIndex_returnsCharacter() {
        engine.processKey('a')

        val selected = engine.selectCandidate(0)

        assertNotNull(selected)
        assertTrue(selected == '日' || selected == '曰')
        // 選擇後應清除狀態
        assertEquals("", engine.getCurrentCode())
        assertTrue(engine.getCandidates().isEmpty())
    }

    @Test
    fun testSelectCandidate_invalidIndex_returnsNull() {
        engine.processKey('a')

        val selected = engine.selectCandidate(99)

        assertNull(selected)
        // 狀態不應改變
        assertEquals("a", engine.getCurrentCode())
    }

    @Test
    fun testSelectCandidate_negativeIndex_returnsNull() {
        engine.processKey('a')

        val selected = engine.selectCandidate(-1)

        assertNull(selected)
    }

    @Test
    fun testSelectCandidateByKey_validKey_returnsCharacter() {
        engine.processKey('a')

        val selected = engine.selectCandidateByKey('1')

        assertNotNull(selected)
        assertTrue(selected == '日' || selected == '曰')
    }

    @Test
    fun testSelectCandidateByKey_invalidKey_returnsNull() {
        engine.processKey('a')

        val selected = engine.selectCandidateByKey('a')

        assertNull(selected)
    }

    @Test
    fun testSelectCandidateByKey_keyMapping() {
        engine.processKey('a')

        // 測試選字鍵映射
        val candidates = engine.getCandidates()

        // '1' 應該選擇第一個候選字（索引 0）
        val selected1 = engine.selectCandidateByKey('1')
        assertEquals(candidates[0], selected1)

        // 重新輸入測試 '2'
        engine.processKey('a')
        val selected2 = engine.selectCandidateByKey('2')
        assertEquals(candidates[1], selected2)
    }

    // ===== Backspace 測試 =====

    @Test
    fun testBackspace_withInput_removesLastCharacter() {
        engine.processKey('a')
        engine.processKey('b')
        engine.processKey('c')

        val result = engine.backspace()

        assertTrue(result)
        assertEquals("ab", engine.getCurrentCode())
    }

    @Test
    fun testBackspace_emptyInput_returnsFalse() {
        val result = engine.backspace()

        assertFalse(result)
    }

    @Test
    fun testBackspace_updatesCandidates() {
        engine.processKey('a')
        engine.processKey('a')

        // "aa" 有候選字
        assertTrue(engine.hasCandidates())

        engine.backspace()

        // "a" 也有候選字
        assertEquals("a", engine.getCurrentCode())
        assertTrue(engine.hasCandidates())
    }

    // ===== Clear 測試 =====

    @Test
    fun testClear_resetsAllState() {
        engine.processKey('a')
        engine.processKey('b')

        engine.clear()

        assertEquals("", engine.getCurrentCode())
        assertTrue(engine.getCandidates().isEmpty())
        assertFalse(engine.hasInput())
        assertFalse(engine.hasCandidates())
    }

    // ===== 狀態查詢測試 =====

    @Test
    fun testHasInput_withInput_returnsTrue() {
        engine.processKey('a')

        assertTrue(engine.hasInput())
    }

    @Test
    fun testHasInput_noInput_returnsFalse() {
        assertFalse(engine.hasInput())
    }

    @Test
    fun testHasCandidates_withCandidates_returnsTrue() {
        engine.processKey('a')

        assertTrue(engine.hasCandidates())
    }

    @Test
    fun testHasCandidates_noCandidates_returnsFalse() {
        engine.processKey('x')
        engine.processKey('y')
        engine.processKey('z')

        assertFalse(engine.hasCandidates())
    }

    @Test
    fun testGetFirstCandidate_withCandidates_returnsFirst() {
        engine.processKey('a')

        val first = engine.getFirstCandidate()

        assertNotNull(first)
        val candidates = engine.getCandidates()
        assertEquals(candidates[0], first)
    }

    @Test
    fun testGetFirstCandidate_noCandidates_returnsNull() {
        val first = engine.getFirstCandidate()

        assertNull(first)
    }

    // ===== Commit 測試 =====

    @Test
    fun testCommit_withCandidates_returnsFirstCandidate() {
        engine.processKey('a')

        val firstCandidate = engine.getFirstCandidate()
        val result = engine.commit()

        assertEquals(firstCandidate.toString(), result)
        // Commit 後應清除狀態
        assertFalse(engine.hasInput())
    }

    @Test
    fun testCommit_noCandidates_returnsCode() {
        engine.processKey('x')
        engine.processKey('y')
        engine.processKey('z')

        val result = engine.commit()

        assertEquals("xyz", result)
        assertFalse(engine.hasInput())
    }

    @Test
    fun testCommit_noInput_returnsNull() {
        val result = engine.commit()

        assertNull(result)
    }

    // ===== 完整輸入流程測試 =====

    @Test
    fun testFullInputFlow_selectCandidate() {
        // 輸入 "a"
        engine.processKey('a')
        assertEquals("a", engine.getCurrentCode())
        assertTrue(engine.hasCandidates())

        // 選擇第一個候選字
        val selected = engine.selectCandidate(0)
        assertNotNull(selected)

        // 狀態應重置
        assertEquals("", engine.getCurrentCode())
        assertFalse(engine.hasInput())
        assertFalse(engine.hasCandidates())
    }

    @Test
    fun testFullInputFlow_commit() {
        // 輸入 "aa"
        engine.processKey('a')
        engine.processKey('a')
        assertEquals("aa", engine.getCurrentCode())
        assertTrue(engine.hasCandidates())

        // 直接 commit
        val result = engine.commit()
        assertEquals("昌", result)

        // 狀態應重置
        assertFalse(engine.hasInput())
    }

    @Test
    fun testFullInputFlow_backspaceAndModify() {
        // 輸入 "abc"
        engine.processKey('a')
        engine.processKey('b')
        engine.processKey('c')

        // Backspace 兩次
        engine.backspace()
        engine.backspace()
        assertEquals("a", engine.getCurrentCode())

        // 重新輸入
        engine.processKey('a')
        assertEquals("aa", engine.getCurrentCode())

        // Commit
        val result = engine.commit()
        assertEquals("昌", result)
    }

    // ===== 非字母字根鍵測試（行列、大易等） =====

    @Test
    fun testProcessKey_arrayRootKeys_allAccepted() {
        // 行列輸入法有 30 個字根鍵：a-z + ,./;
        val arrayEngine = InputMethodEngine(createArrayTestTableData())

        // 驗證所有 keyNameMap 中的字根鍵都被接受
        val arrayKeyNameMap = createArrayTestTableData().keyNameMap
        for (key in arrayKeyNameMap.keys) {
            arrayEngine.clear()
            val result = arrayEngine.processKey(key)
            assertTrue("Root key '$key' should be accepted", result)
            assertEquals("Code buffer should contain '$key'", key.toString(), arrayEngine.getCurrentCode())
        }
    }

    @Test
    fun testProcessKey_arrayPunctuationRoots_acceptedAsRootKeys() {
        val arrayEngine = InputMethodEngine(createArrayTestTableData())

        // 這些是行列輸入法的字根，不應直接輸出
        for (key in listOf(',', '.', '/', ';')) {
            arrayEngine.clear()
            val result = arrayEngine.processKey(key)
            assertTrue("'$key' should be accepted as root key in Array", result)
        }
    }

    @Test
    fun testProcessKey_dayiDigitRoots_acceptedAsRootKeys() {
        val dayiEngine = InputMethodEngine(createDayiTestTableData())

        // 大易輸入法的數字字根
        for (key in '0'..'9') {
            dayiEngine.clear()
            val result = dayiEngine.processKey(key)
            assertTrue("'$key' should be accepted as root key in Dayi", result)
        }
    }

    @Test
    fun testProcessKey_nonRootKey_rejected() {
        val arrayEngine = InputMethodEngine(createArrayTestTableData())

        // 行列輸入法不使用數字作為字根
        val result = arrayEngine.processKey('1')
        assertFalse("'1' should be rejected in Array", result)
    }

    @Test
    fun testProcessKey_arrayCommaRoot_lookupCandidates() {
        val arrayEngine = InputMethodEngine(createArrayTestTableData())

        arrayEngine.processKey(',')

        assertEquals(",", arrayEngine.getCurrentCode())
        val candidates = arrayEngine.getCandidates()
        assertTrue("Comma root should have candidates", candidates.isNotEmpty())
        assertTrue(candidates.contains('火'))
    }

    // ===== 輔助方法 =====

    private fun createArrayTestTableData(): CINParseResult {
        // 模擬行列輸入法的 keyNameMap (a-z + ,./;)
        val keyNameMap = mutableMapOf<Char, String>()
        for (c in 'a'..'z') keyNameMap[c] = c.toString()
        keyNameMap[','] = "8v"
        keyNameMap['.'] = "9v"
        keyNameMap['/'] = "0v"
        keyNameMap[';'] = "0-"

        return CINParseResult(
            charToCode = mapOf('火' to ",", '米' to ","),
            codeToCandidates = mapOf("," to listOf('火', '米')),
            metadata = mapOf("ename" to "Array", "cname" to "行列"),
            keyNameMap = keyNameMap
        )
    }

    private fun createDayiTestTableData(): CINParseResult {
        // 模擬大易輸入法的 keyNameMap (a-z + 0-9 + ,./;)
        val keyNameMap = mutableMapOf<Char, String>()
        for (c in 'a'..'z') keyNameMap[c] = c.toString()
        for (c in '0'..'9') keyNameMap[c] = c.toString()
        keyNameMap[','] = "力"
        keyNameMap['.'] = "點"
        keyNameMap['/'] = "竹"
        keyNameMap[';'] = "虫"

        return CINParseResult(
            charToCode = mapOf('力' to ","),
            codeToCandidates = mapOf("," to listOf('力')),
            metadata = mapOf("ename" to "DaYi", "cname" to "大易"),
            keyNameMap = keyNameMap
        )
    }

    private fun createTestTableData(): CINParseResult {
        val charToCode = mapOf(
            '日' to "a",
            '曰' to "a",
            '昌' to "aa",
            '晶' to "aaa"
        )

        val codeToCandidates = mapOf(
            "a" to listOf('日', '曰'),
            "aa" to listOf('昌'),
            "aaa" to listOf('晶')
        )

        return CINParseResult(
            charToCode = charToCode,
            codeToCandidates = codeToCandidates,
            metadata = mapOf(
                "ename" to "Test",
                "cname" to "測試",
                "selkey" to "1234567890"
            )
        )
    }
}

