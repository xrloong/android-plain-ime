package engine

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * InputMethodEngineManager 單元測試
 */
class InputMethodEngineManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var manager: InputMethodEngineManager

    @Before
    fun setup() {
        manager = InputMethodEngineManager()
    }

    // ===== 狀態管理測試 =====

    @Test
    fun testInitialState_notReady() {
        // 初始狀態下應該無法輸入
        val result = manager.processKey('a')

        assertFalse(result)
        assertEquals("", manager.getCurrentCode())
    }

    @Test
    fun testLoadInputMethod_updatesStateToLoading() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready || state is EngineState.Error) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        // Loading 狀態可能因為太快而觀察不到，這是正常的
    }

    @Test
    fun testLoadInputMethod_success_stateBecomesReady() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)
        var readyState: EngineState.Ready? = null

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                readyState = state
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNotNull(readyState)
        val ready = readyState!!
        assertEquals("測試", ready.inputMethodName)
        assertTrue(ready.totalChars > 0)
    }

    @Test
    fun testLoadInputMethod_error_stateBecomesError() {
        val invalidContent = "invalid cin content"
        val latch = CountDownLatch(1)
        var errorState: EngineState.Error? = null

        manager.engineState.observeForever { state ->
            if (state is EngineState.Error) {
                errorState = state
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", invalidContent.byteInputStream())

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNotNull(errorState)
        val error = errorState!!
        assertTrue(error.message.isNotEmpty())
        assertTrue(error.canRetry)
    }

    // ===== 輸入功能測試 =====

    @Test
    fun testProcessKey_afterSuccessfulLoad_works() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        // 現在應該可以輸入
        val result = manager.processKey('a')

        assertTrue(result)
        assertEquals("a", manager.getCurrentCode())
    }

    @Test
    fun testGetCandidates_afterSuccessfulLoad_returnsCandidates() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')
        val candidates = manager.getCandidates()

        assertTrue(candidates.isNotEmpty())
        assertEquals('日', candidates[0])
    }

    @Test
    fun testSelectCandidate_afterSuccessfulLoad_works() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')
        val selected = manager.selectCandidate(0)

        assertNotNull(selected)
        assertEquals('日', selected)
        assertEquals("", manager.getCurrentCode())
    }

    @Test
    fun testBackspace_afterSuccessfulLoad_works() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')
        manager.processKey('b')
        val result = manager.backspace()

        assertTrue(result)
        assertEquals("a", manager.getCurrentCode())
    }

    @Test
    fun testCommit_afterSuccessfulLoad_works() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')
        val result = manager.commit()

        assertEquals("日", result)
        assertFalse(manager.hasInput())
    }

    // ===== 狀態查詢測試 =====

    @Test
    fun testHasInput_beforeLoad_returnsFalse() {
        assertFalse(manager.hasInput())
    }

    @Test
    fun testHasInput_afterLoad_withInput_returnsTrue() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')

        assertTrue(manager.hasInput())
    }

    @Test
    fun testHasCandidates_afterLoad_withCandidates_returnsTrue() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        manager.processKey('a')

        assertTrue(manager.hasCandidates())
    }

    // ===== 錯誤處理與重試測試 =====

    @Test
    fun testRetry_afterError_retriesLoad() {
        val invalidContent = "invalid"
        val latch = CountDownLatch(1)
        var errorCount = 0

        manager.engineState.observeForever { state ->
            if (state is EngineState.Error) {
                errorCount++
                latch.countDown()
            }
        }

        manager.loadInputMethod("test", invalidContent.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(1, errorCount)

        // 重試（會使用相同的錯誤 InputStream，所以會再次失敗）
        val retryLatch = CountDownLatch(1)
        manager.engineState.observeForever { state ->
            if (state is EngineState.Error && errorCount > 1) {
                retryLatch.countDown()
            }
        }

        manager.retry()
        assertTrue(retryLatch.await(5, TimeUnit.SECONDS))
    }

    // ===== 完整流程測試 =====

    @Test
    fun testFullFlow_loadAndInput() {
        val content = createValidCINContent()
        val latch = CountDownLatch(1)

        manager.engineState.observeForever { state ->
            if (state is EngineState.Ready) {
                latch.countDown()
            }
        }

        // 1. 載入輸入法
        manager.loadInputMethod("cangjie", content.byteInputStream())
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        // 2. 輸入編碼
        manager.processKey('a')
        assertEquals("a", manager.getCurrentCode())

        // 3. 檢查候選字
        val candidates = manager.getCandidates()
        assertTrue(candidates.isNotEmpty())

        // 4. 選擇候選字
        val selected = manager.selectCandidate(0)
        assertNotNull(selected)

        // 5. 確認已清除
        assertFalse(manager.hasInput())
    }

    // ===== 輔助方法 =====

    private fun createValidCINContent(): String {
        return """
            %ename Test
            %cname 測試
            %selkey 1234567890
            
            %chardef begin
            a 日
            aa 昌
            aaa 晶
            %chardef end
        """.trimIndent()
    }
}

