package kotlinx.coroutines.rx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import rx.Emitter
import rx.Observable
import rx.functions.Action0
import rx.functions.Action1

class RxObservableTest {

    @Test
    fun testObservableAwaitFirstSuccess() = runBlocking<Unit> {
        assertEquals(1, Observable.from(listOf(1, 2, 3)).awaitFirst())
    }

    @Test(expected = RuntimeException::class)
    fun testObservableAwaitFirstFailure() = runBlocking<Unit> {
        Observable.error<Int>(RuntimeException()).awaitFirst()
    }

    @Test
    fun testObservableAwaitFirstCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Observable.create<Int>({}, Emitter.BackpressureMode.NONE)
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .awaitFirst()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call()
            onUnsubscribe.call()
        }
    }

    @Test
    fun testObservableAwaitLastSuccess() = runBlocking<Unit> {
        assertEquals(3, Observable.from(listOf(1, 2, 3)).awaitLast())
    }

    @Test(expected = RuntimeException::class)
    fun testObservableAwaitLastFailure() = runBlocking<Unit> {
        Observable.error<Int>(RuntimeException()).awaitLast()
    }

    @Test
    fun testObservableAwaitLastCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Observable.create<Int>({}, Emitter.BackpressureMode.NONE)
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .awaitLast()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call()
            onUnsubscribe.call()
        }
    }

    @Test
    fun testObservableAwaitSingleSuccess() = runBlocking<Unit> {
        assertEquals(1, Observable.just(1).awaitSingle())
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun testObservableAwaitSingleMoreThenOneElement() = runBlocking<Unit> {
        Observable.from(listOf(1, 2, 3)).awaitSingle()
    }

    @Test(expected = RuntimeException::class)
    fun testObservableAwaitSingleFailure() = runBlocking<Unit> {
        Observable.error<Int>(RuntimeException()).awaitSingle()
    }

    @Test
    fun testObservableAwaitSingleCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Observable.create<Int>({}, Emitter.BackpressureMode.NONE)
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
    fun testObservableAwaitCompleteSuccess() = runBlocking<Unit> {
        Observable.empty<Int>().awaitComplete()
    }

    @Test(expected = RuntimeException::class)
    fun testObservableAwaitCompleteFailure() = runBlocking<Unit> {
        Observable.error<Int>(RuntimeException()).awaitSingle()
    }

    @Test
    fun testObservableAwaitCompleteCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Observable.create<Int>({}, Emitter.BackpressureMode.NONE)
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
    fun testObservableAsFlowSuccess() = runBlocking<Unit> {
        assertEquals(
            listOf(1, 2, 3),
            Observable.from(listOf(1, 2, 3))
                .asFlow()
                .toList()
        )
    }

    @Test(expected = RuntimeException::class)
    fun testObservableAsFlowFailure() = runBlocking<Unit> {
        Observable.error<Int>(RuntimeException())
            .asFlow()
            .collect()
    }

    @Test
    fun testObservableAsFlowCancel() = runBlocking<Unit> {
        val onSubscribe = mockk<Action0>()
        val onUnsubscribe = mockk<Action0>()

        every { onSubscribe.call() } returns Unit
        every { onUnsubscribe.call() } returns Unit

        val job = launch {
            Observable.create<Int>({}, Emitter.BackpressureMode.NONE)
                .doOnSubscribe(onSubscribe)
                .doOnUnsubscribe(onUnsubscribe)
                .asFlow()
                .collect()
        }
        yield() // run coroutine
        job.cancelAndJoin()

        verifySequence {
            onSubscribe.call()
            onUnsubscribe.call()
        }
    }

    @Test
    fun testFlowAsObservableSuccess() = runBlocking<Unit> {
        val onNext = mockk<Action1<Int>>()
        val onError = mockk<Action1<Throwable>>()
        val onComplete = mockk<Action0>()

        every { onNext.call(any()) } returns Unit
        every { onError.call(any()) } returns Unit
        every { onComplete.call() } returns Unit

        flowOf(1, 2, 3)
            .asObservable(coroutineContext, Emitter.BackpressureMode.NONE)
            .subscribe(onNext, onError, onComplete)
        yield() // run coroutine

        verifySequence {
            onNext.call(1)
            onNext.call(2)
            onNext.call(3)
            onComplete.call()
        }
    }

    @Test
    fun testFlowAsObservableFailure() = runBlocking<Unit> {
        val onNext = mockk<Action1<Int>>()
        val onError = mockk<Action1<Throwable>>()
        val onComplete = mockk<Action0>()

        every { onNext.call(any()) } returns Unit
        every { onError.call(any()) } returns Unit
        every { onComplete.call() } returns Unit

        flow<Int> {
            throw RuntimeException()
        }
            .asObservable(coroutineContext, Emitter.BackpressureMode.NONE)
            .subscribe(onNext, onError, onComplete)
        yield() // run coroutine

        verifySequence {
            onError.call(match { it is RuntimeException })
        }
    }

    @Test
    fun testFlowAsObservableUnsubscribe() = runBlocking<Unit> {
        val onClose = mockk<() -> Unit>()
        every { onClose() } returns Unit

        val subscription = callbackFlow<Int> {
            awaitClose(onClose)
        }
            .asObservable(coroutineContext, Emitter.BackpressureMode.NONE)
            .subscribe()
        yield() // run coroutine
        subscription.unsubscribe()
        yield() // run coroutine

        verify { onClose.invoke() }
    }

    @Test
    fun testRxObservableSuccess() = runBlocking<Unit> {
        val onNext = mockk<Action1<Int>>()
        val onError = mockk<Action1<Throwable>>()
        val onComplete = mockk<Action0>()

        every { onNext.call(any()) } returns Unit
        every { onError.call(any()) } returns Unit
        every { onComplete.call() } returns Unit

        rxObservable<Int>(coroutineContext, Emitter.BackpressureMode.NONE) {
            trySend(1)
            trySend(2)
            close()
        }.subscribe(onNext, onError, onComplete)
        yield() // run coroutine
        yield() // run coroutine
        yield() // run coroutine

        verifySequence {
            onNext.call(1)
            onNext.call(2)
            onComplete.call()
        }
    }

    @Test
    fun testRxObservableFailure() = runBlocking<Unit> {
        val onNext = mockk<Action1<Int>>()
        val onError = mockk<Action1<Throwable>>()
        val onComplete = mockk<Action0>()

        every { onNext.call(any()) } returns Unit
        every { onError.call(any()) } returns Unit
        every { onComplete.call() } returns Unit

        rxObservable<Int>(coroutineContext, Emitter.BackpressureMode.NONE) {
            throw RuntimeException()
        }.subscribe(onNext, onError, onComplete)
        yield() // run coroutine
        yield() // run coroutine
        yield() // run coroutine

        verifySequence { onError.call(match { it is RuntimeException }) }
    }

    @Test
    fun testRxObservableCancel() = runBlocking<Unit> {
        val onNext = mockk<Action1<Int>>()
        val onError = mockk<Action1<Throwable>>()
        val onComplete = mockk<Action0>()
        val onClose = mockk<() -> Unit>()

        every { onNext.call(any()) } returns Unit
        every { onError.call(any()) } returns Unit
        every { onComplete.call() } returns Unit
        every { onClose.invoke() } returns Unit

        val job = launch {
            rxObservable<Int>(coroutineContext, Emitter.BackpressureMode.NONE) {
                awaitClose(onClose)
            }.subscribe(onNext, onError, onComplete)
        }
        yield() // run coroutine
        yield() // run coroutine
        job.cancelAndJoin()

        verify { onClose.invoke() }
    }

}