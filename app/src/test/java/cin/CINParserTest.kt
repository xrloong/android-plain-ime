package cin

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CINParserTest {
    private lateinit var parser: CINParser

    @Before
    fun setup() {
        parser = CINParser()
    }

    @Test
    fun testParse_validCIN_basic() {
        val content = """
            %chardef begin
            a 日
            b 月
            c 金
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(3, result.totalChars)
        assertEquals("a", result.getCode('日'))
        assertEquals("b", result.getCode('月'))
        assertEquals("c", result.getCode('金'))
        assertEquals(listOf('日'), result.getCandidates("a"))
        assertEquals(listOf('月'), result.getCandidates("b"))
        assertEquals(listOf('金'), result.getCandidates("c"))
    }

    @Test
    fun testParse_multipleCandidatesForSameCode() {
        val content = """
            %chardef begin
            aa 昌
            aa 晶
            aa 明
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        val candidates = result.getCandidates("aa")
        assertEquals(3, candidates.size)
        assertTrue(candidates.contains('昌'))
        assertTrue(candidates.contains('晶'))
        assertTrue(candidates.contains('明'))
    }

    @Test
    fun testParse_withComments() {
        val content = """
            # 這是註解
            %chardef begin
            # 日字根
            a 日
            # 月字根
            b 月
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(2, result.totalChars)
        assertEquals("a", result.getCode('日'))
        assertEquals("b", result.getCode('月'))
    }

    @Test
    fun testParse_withKeyname() {
        val content = """
            %keyname begin
            a 日
            b 月
            %keyname end
            
            %chardef begin
            a 日
            b 月
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(2, result.totalChars)
        assertEquals("a", result.getCode('日'))
    }

    @Test
    fun testParse_withMultipleControlDirectives() {
        val content = """
            %gen_inp
            %ename Cangjie
            %cname 倉頡
            
            %chardef begin
            a 日
            b 月
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(2, result.totalChars)
    }

    @Test
    fun testParse_emptyContent_throwsException() {
        val exception = assertThrows(CINParseException::class.java) {
            parser.parse("")
        }
        assertTrue(exception.message!!.contains("為空"))
    }

    @Test
    fun testParse_blankContent_throwsException() {
        val exception = assertThrows(CINParseException::class.java) {
            parser.parse("   \n  \n  ")
        }
        assertTrue(exception.message!!.contains("為空"))
    }

    @Test
    fun testParse_noChardef_throwsException() {
        val content = """
            %keyname begin
            a 日
            %keyname end
        """.trimIndent()

        val exception = assertThrows(CINParseException::class.java) {
            parser.parse(content)
        }
        assertTrue(exception.message!!.contains("未找到任何字符定義"))
    }

    @Test
    fun testParse_invalidFormat_missingChar_throwsException() {
        val content = """
            %chardef begin
            a
            %chardef end
        """.trimIndent()

        val exception = assertThrows(CINParseException::class.java) {
            parser.parse(content)
        }
        assertTrue(exception.message!!.contains("格式錯誤"))
    }

    @Test
    fun testParse_invalidFormat_emptyLine_ignored() {
        val content = """
            %chardef begin
            a 日
            
            b 月
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(2, result.totalChars)
    }

    @Test
    fun testGetCandidates_nonExistentCode_returnsEmptyList() {
        val content = """
            %chardef begin
            a 日
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(emptyList<Char>(), result.getCandidates("xyz"))
    }

    @Test
    fun testGetCode_nonExistentChar_returnsNull() {
        val content = """
            %chardef begin
            a 日
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertNull(result.getCode('神'))
    }

    @Test
    fun testParse_complexCIN_withRealData() {
        val content = """
            # 倉頡測試
            %gen_inp
            %ename Cangjie
            %cname 倉頡
            %tname 倉頡
            %sname 仓颉
            %encoding UTF-8
            %selkey 1234567890
            %space_style 4
            
            %keyname begin
            a 日
            b 月
            c 金
            %keyname end
            
            %chardef begin
            a 日
            aa 昌
            aaa 晶
            ab 明
            b 月
            ba 肥
            bb 朋
            c 金
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(8, result.totalChars)
        assertEquals("a", result.getCode('日'))
        assertEquals("aa", result.getCode('昌'))
        assertEquals("aaa", result.getCode('晶'))
        assertEquals(listOf('明'), result.getCandidates("ab"))
        assertEquals(listOf('朋'), result.getCandidates("bb"))

        // 驗證元數據
        assertEquals("Cangjie", result.englishName)
        assertEquals("倉頡", result.chineseName)
        assertEquals("1234567890", result.selectionKeys)
    }

    @Test
    fun testParse_withTabSeparator() {
        val content = """
            %chardef begin
            a	日
            aa	昌
            aaa	晶
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals(3, result.totalChars)
        assertEquals("a", result.getCode('日'))
        assertEquals("aa", result.getCode('昌'))
        assertEquals("aaa", result.getCode('晶'))
    }

    @Test
    fun testParse_metadata_defaults() {
        val content = """
            %chardef begin
            a 日
            %chardef end
        """.trimIndent()

        val result = parser.parse(content)

        assertEquals("Unknown", result.englishName)
        assertEquals("未知", result.chineseName)
        assertEquals("1234567890", result.selectionKeys)
    }
}
