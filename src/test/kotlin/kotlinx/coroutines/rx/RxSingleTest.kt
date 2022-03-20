package kotlinx.coroutines.rx

import io.mockk.*
import junit.framework.TestCase.*
import kotlinx.coroutines.*
import org.junit.Test
import rx.Single
import rx.functions.Action0

class RxSingleTest {

    @Test
    fun testSingleAwaitSingleSuccess() = runBlocking<Unit> {
        assertEquals(1, Single.just(1).awaitSingle())
    }

    @Test(expected = RuntimeException::class)
    fun testSingleAwaitSingleFailure() = runBlocking<Unit> {
        Single.error<Int>(RuntimeException()).awaitSingle()
    }

    @Test
    fun testSingleAwaitSingleCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Single.fromEmitter<Int> { /* never emmit */ }
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .awaitSingle()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call()
            onUnsubscribe.call()
        }
    }

    @Test
    fun testSingleAwaitCompleteSuccess() = runBlocking<Unit> {
        Single.just(1).awaitComplete()
    }

    @Test(expected = RuntimeException::class)
    fun testSingleAwaitCompleteFailure() = runBlocking<Unit> {
        Single.error<Int>(RuntimeException()).awaitComplete()
    }

    @Test
    fun testSingleAwaitCompleteCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Single.fromEmitter<Int> { /* never emmit */ }
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .awaitComplete()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call()
            onUnsubscribe.call()
        }
    }

    @Test
    fun testRxSingleSuccess() = runBlocking<Unit> {
        assertEquals(1, rxSingle(coroutineContext) { 1 }.awaitSingle())
    }

    @Test(expected = RuntimeException::class)
    fun testRxSingleFailure() = runBlocking<Unit> {
        rxSingle<Int>(coroutineContext) { throw RuntimeException() }.awaitSingle()
    }

    @Test
    fun testRxSingleUnsubscribe() = runBlocking<Unit> {
        val suspendFunction = mockk<suspend () -> Unit>()
        coEvery { suspendFunction.invoke() } coAnswers { delay(Long.MAX_VALUE) }

        val subscription = rxSingle(coroutineContext) {
            suspendFunction()
            fail()
        }.subscribe()
        yield() // run coroutine
        subscription.unsubscribe()

        coVerify { suspendFunction.invoke() }
    }

}