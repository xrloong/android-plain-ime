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

    // ===== 輔助方法 =====

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

