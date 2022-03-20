package kotlinx.coroutines.rx

import io.mockk.*
import junit.framework.TestCase.fail
import kotlinx.coroutines.*
import org.junit.Test
import rx.Completable
import rx.Subscription
import rx.functions.Action0
import rx.functions.Action1

class RxCompletableTest {

    @Test
    fun testCompletableAwaitCompleteSuccess() = runBlocking<Unit> {
        Completable.complete().awaitComplete()
    }

    @Test(expected = RuntimeException::class)
    fun testCompletableAwaitCompleteFailure() = runBlocking<Unit> {
        Completable.error(RuntimeException()).awaitComplete()
    }

    @Test
    fun testCompletableAwaitCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action1<Subscription>>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call(any()) } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Completable.fromEmitter { /* never emmit */ }
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .awaitComplete()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call(any())
            onUnsubscribe.call()
        }
    }

    @Test
    fun testRxCompletableSuccess() = runBlocking<Unit> {
        rxCompletable(coroutineContext) {}.awaitComplete()
    }

    @Test(expected = RuntimeException::class)
    fun testRxCompletableFailure() = runBlocking<Unit> {
        rxCompletable(coroutineContext) { throw RuntimeException() }.awaitComplete()
    }

    @Test
    fun testRxCompletableUnsubscribe() = runBlocking<Unit> {
        val suspendFunction = mockk<suspend () -> Unit>()
        coEvery { suspendFunction.invoke() } coAnswers { delay(Long.MAX_VALUE) }

        val subscription = rxCompletable(coroutineContext) {
            suspendFunction()
            fail()
        }.subscribe()
        yield() // run coroutine
        subscription.unsubscribe()

        coVerify { suspendFunction.invoke() }
    }
}