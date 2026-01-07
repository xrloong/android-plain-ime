package integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cin.CINParser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import table.TableLoader

/**
 * 整合測試：驗證內建倉頡 CIN 檔案可以正確載入與解析
 */
@RunWith(AndroidJUnit4::class)
class QhcjCINIntegrationTest {

    private lateinit var parser: CINParser
    private lateinit var loader: TableLoader

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        parser = CINParser()
        loader = TableLoader(context)
    }

    @Test
    fun testLoadQhcjFromAssets() {
        // 從 assets 載入 qhcj.cin
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        val content = inputStream.bufferedReader().use { it.readText() }

        assertNotNull(content)
        assertTrue(content.isNotEmpty())
        assertTrue(content.contains("%chardef"))
    }

    @Test
    fun testParseQhcjCIN() {
        // 載入並解析 qhcj.cin
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        val content = inputStream.bufferedReader().use { it.readText() }

        val result = parser.parse(content)

        // 驗證基本資訊
        assertNotNull(result)
        assertTrue(result.totalChars > 0)

        // 驗證元數據
        assertEquals("CangJie", result.englishName)
        assertEquals("倉頡", result.chineseName)
        assertEquals("1234567890", result.selectionKeys)

        println("成功載入倉頡輸入法，共 ${result.totalChars} 個字符定義")
    }

    @Test
    fun testQhcjCandidatesQuery() {
        // 載入並解析 qhcj.cin
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        val content = inputStream.bufferedReader().use { it.readText() }
        val result = parser.parse(content)

        // 測試查詢功能
        // 日 = a
        val candidatesA = result.getCandidates("a")
        assertTrue(candidatesA.isNotEmpty())
        assertTrue(candidatesA.contains('日') || candidatesA.contains('曰'))

        // 昌 = aa
        val candidatesAA = result.getCandidates("aa")
        assertTrue(candidatesAA.isNotEmpty())
        assertTrue(candidatesAA.contains('昌'))

        // 晶 = aaa
        val candidatesAAA = result.getCandidates("aaa")
        assertTrue(candidatesAAA.isNotEmpty())
        assertTrue(candidatesAAA.contains('晶'))

        println("查詢測試通過:")
        println("  a -> ${candidatesA.take(5)}")
        println("  aa -> ${candidatesAA.take(5)}")
        println("  aaa -> ${candidatesAAA.take(5)}")
    }

    @Test
    fun testQhcjCodeQuery() {
        // 載入並解析 qhcj.cin
        val inputStream = loader.loadFromAssets(TableLoader.BUILTIN_CANGJIE)
        val content = inputStream.bufferedReader().use { it.readText() }
        val result = parser.parse(content)

        // 測試反向查詢
        val codeFor日 = result.getCode('日')
        assertNotNull(codeFor日)
        assertEquals("a", codeFor日)

        val codeFor昌 = result.getCode('昌')
        assertNotNull(codeFor昌)
        assertEquals("aa", codeFor昌)

        val codeFor晶 = result.getCode('晶')
        assertNotNull(codeFor晶)
        assertEquals("aaa", codeFor晶)

        println("反向查詢測試通過:")
        println("  日 -> $codeFor日")
        println("  昌 -> $codeFor昌")
        println("  晶 -> $codeFor晶")
    }
}

