package kotlinx.coroutines.rx.internal

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RxCoroutineScopeTest {

    @Test
    fun testDispatchDirect() = runBlocking<Unit> {
        val deferred = RxCoroutineScope.async { Thread.currentThread() }
        assertTrue(deferred.isCompleted)
        assertEquals(Thread.currentThread(), deferred.await())
    }

    @Test
    fun testChildFailWillNotCancelScope() = runBlocking<Unit> {
        RxCoroutineScope.async { throw RuntimeException() }
        val deferred = RxCoroutineScope.async { "hello" }
        assertEquals("hello", deferred.await())
    }

}