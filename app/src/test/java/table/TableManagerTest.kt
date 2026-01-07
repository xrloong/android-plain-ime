package table

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TableManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var manager: TableManager

    @Before
    fun setup() {
        manager = TableManager()
    }

    @Test
    fun testLoadTable_success_updatesStateToLoading() {
        val content = createValidCINContent()
        val inputStream = content.byteInputStream()

        manager.loadTable("test", inputStream)

        // 初始狀態應該是 Loading
        val state = manager.state.value
        assertTrue(state is TableLoadState.Loading || state is TableLoadState.Success)
    }

    @Test
    fun testLoadTable_success_updatesStateToSuccess() {
        val content = createValidCINContent()
        val inputStream = content.byteInputStream()
        val latch = CountDownLatch(1)
        var successState: TableLoadState.Success? = null

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                successState = state
                latch.countDown()
            }
        }

        manager.loadTable("test", inputStream)

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNotNull(successState)
        assertTrue(successState!!.data.totalChars > 0)
    }

    @Test
    fun testLoadTable_invalidCIN_updatesStateToError() {
        val content = "invalid content without chardef"
        val inputStream = content.byteInputStream()
        val latch = CountDownLatch(1)
        var errorState: TableLoadState.Error? = null

        manager.state.observeForever { state ->
            if (state is TableLoadState.Error) {
                errorState = state
                latch.countDown()
            }
        }

        manager.loadTable("test", inputStream)

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNotNull(errorState)
        val error = errorState!!
        assertTrue(error.message.contains("未找到任何字符定義"))
        assertNotNull(error.retrySuggestion)
    }

    @Test
    fun testLoadTable_cache_doesNotReloadSameTable() {
        val content = createValidCINContent()

        // 第一次載入
        manager.loadTable("test", content.byteInputStream())
        Thread.sleep(100) // 等待載入完成

        val firstState = manager.state.value
        assertTrue(firstState is TableLoadState.Success)

        // 第二次載入相同的表
        manager.loadTable("test", content.byteInputStream())

        val secondState = manager.state.value
        assertTrue(secondState is TableLoadState.Success)
        // 應該直接從快取返回，不進入 Loading 狀態
    }

    @Test
    fun testGetCandidates_beforeLoad_returnsNull() {
        val candidates = manager.getCandidates("a")
        assertNull(candidates)
    }

    @Test
    fun testGetCandidates_afterSuccessfulLoad_returnsResults() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                latch.countDown()
            }
        }

        manager.loadTable("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        val candidates = manager.getCandidates("a")
        assertNotNull(candidates)
        assertTrue(candidates!!.isNotEmpty())
        assertEquals('日', candidates[0])
    }

    @Test
    fun testGetCode_afterSuccessfulLoad_returnsCode() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                latch.countDown()
            }
        }

        manager.loadTable("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        val code = manager.getCode('日')
        assertNotNull(code)
        assertEquals("a", code)
    }

    @Test
    fun testGetCode_nonExistentChar_returnsNull() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                latch.countDown()
            }
        }

        manager.loadTable("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        val code = manager.getCode('神')
        assertNull(code)
    }

    @Test
    fun testRetry_afterError_retriesLoad() {
        val invalidContent = "invalid"
        val latch = CountDownLatch(1)
        var errorCount = 0

        manager.state.observeForever { state ->
            if (state is TableLoadState.Error) {
                errorCount++
                latch.countDown()
            }
        }

        manager.loadTable("test", invalidContent.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(1, errorCount)

        // 重試應該再次觸發載入
        val retryLatch = CountDownLatch(1)
        manager.state.observeForever { state ->
            if (state is TableLoadState.Error && errorCount > 1) {
                retryLatch.countDown()
            }
        }

        manager.retry()
        // retry 會使用相同的錯誤 InputStream，所以應該再次失敗
        assertTrue(retryLatch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun testClearCache_removesAllCachedData() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                latch.countDown()
            }
        }

        manager.loadTable("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        // 清除快取
        manager.clearCache()

        // 再次載入應該重新載入
        val secondLatch = CountDownLatch(1)
        var loadingCount = 0

        manager.state.observeForever { state ->
            if (state is TableLoadState.Loading) {
                loadingCount++
            }
            if (state is TableLoadState.Success && loadingCount > 0) {
                secondLatch.countDown()
            }
        }

        manager.loadTable("test2", content.byteInputStream())
        assertTrue(secondLatch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun testLoadTable_multipleTables_cachesIndependently() {
        val content1 = """
            %chardef begin
            a 日
            %chardef end
        """.trimIndent()

        val content2 = """
            %chardef begin
            b 月
            %chardef end
        """.trimIndent()

        val latch = CountDownLatch(2)

        manager.state.observeForever { state ->
            if (state is TableLoadState.Success) {
                latch.countDown()
            }
        }

        manager.loadTable("table1", content1.byteInputStream())
        Thread.sleep(100)
        manager.loadTable("table2", content2.byteInputStream())

        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    private fun createValidCINContent(): String {
        return """
            %chardef begin
            a 日
            aa 昌
            aaa 晶
            b 月
            ba 肥
            bb 朋
            c 金
            %chardef end
        """.trimIndent()
    }
}
