package cin

import org.junit.Assert.*
import org.junit.Test

class CharFrequencyTableTest {

    @Test
    fun testParse_basic() {
        val content = "的\n是\n不\n我"
        val table = CharFrequencyTable.parse(content)

        assertEquals(4, table.size)
        assertEquals(0, table.getRank('的'))
        assertEquals(1, table.getRank('是'))
        assertEquals(2, table.getRank('不'))
        assertEquals(3, table.getRank('我'))
    }

    @Test
    fun testGetRank_unknownChar_returnsMaxValue() {
        val content = "的\n是"
        val table = CharFrequencyTable.parse(content)

        assertEquals(Int.MAX_VALUE, table.getRank('龍'))
    }

    @Test
    fun testParse_skipsComments() {
        val content = "# 這是註解\n的\n# 另一行註解\n是"
        val table = CharFrequencyTable.parse(content)

        assertEquals(2, table.size)
        assertEquals(0, table.getRank('的'))
        assertEquals(1, table.getRank('是'))
    }

    @Test
    fun testParse_skipsEmptyLines() {
        val content = "的\n\n是\n\n不"
        val table = CharFrequencyTable.parse(content)

        assertEquals(3, table.size)
        assertEquals(0, table.getRank('的'))
        assertEquals(1, table.getRank('是'))
        assertEquals(2, table.getRank('不'))
    }

    @Test
    fun testSortCandidates_basic() {
        val content = "的\n是\n不\n我\n一"
        val table = CharFrequencyTable.parse(content)

        val sorted = table.sortCandidates(listOf('一', '我', '的'))
        assertEquals(listOf('的', '我', '一'), sorted)
    }

    @Test
    fun testSortCandidates_unknownCharsAtEnd() {
        val content = "的\n是"
        val table = CharFrequencyTable.parse(content)

        val sorted = table.sortCandidates(listOf('龍', '是', '鳳', '的'))
        assertEquals('的', sorted[0])
        assertEquals('是', sorted[1])
        // 未知字排在後方
        assertTrue(sorted.indexOf('龍') > 1)
        assertTrue(sorted.indexOf('鳳') > 1)
    }

    @Test
    fun testSortCandidates_stableSort() {
        val content = "的\n是"
        val table = CharFrequencyTable.parse(content)

        // 龍 和 鳳 都不在字頻表中，應保持原始順序
        val sorted = table.sortCandidates(listOf('龍', '鳳', '的'))
        assertEquals(listOf('的', '龍', '鳳'), sorted)
    }

    @Test
    fun testEmpty_doesNotSort() {
        val table = CharFrequencyTable.EMPTY

        assertEquals(0, table.size)
        assertEquals(Int.MAX_VALUE, table.getRank('的'))
    }

    @Test
    fun testParse_duplicateCharsIgnored() {
        val content = "的\n是\n的"
        val table = CharFrequencyTable.parse(content)

        assertEquals(2, table.size)
        assertEquals(0, table.getRank('的'))
    }
}
