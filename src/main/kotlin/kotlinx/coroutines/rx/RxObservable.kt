package kotlinx.coroutines.rx

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx.internal.RxCoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import rx.Emitter
import rx.Observable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Observable<T>.awaitFirst(): T = suspendCancellableCoroutine { cont ->
    val subscription = first().subscribe(
        { cont.resume(it) },
        { cont.resumeWithException(exception = it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

suspend fun <T> Observable<T>.awaitLast(): T = suspendCancellableCoroutine { cont ->
    val subscription = last().subscribe(
        { cont.resume(it) },
        { cont.resumeWithException(exception = it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

suspend fun <T> Observable<T>.awaitSingle(): T = suspendCancellableCoroutine { cont ->
    val subscription = single().subscribe(
        { cont.resume(it) },
        { cont.resumeWithException(exception = it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

suspend fun <T> Observable<T>.awaitComplete(): Unit = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        { /* ignore */ },
        { cont.resumeWithException(exception = it) },
        { cont.resume(Unit) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

fun <T> Observable<T>.asFlow(): Flow<T> = callbackFlow {
    val subscription = subscribe(
        { trySend(it) },
        { close(cause = it) },
        { close(cause = null) }
    )
    awaitClose { subscription.unsubscribe() }
}

fun <T> Flow<T>.asObservable(
    context: CoroutineContext,
    backpressureMode: Emitter.BackpressureMode
): Observable<T> {
    return Observable.create(
        { emitter ->
            val job = RxCoroutineScope.launch(context, CoroutineStart.LAZY) {
                runCatching { collect { emitter.onNext(it) } }
                    .onSuccess { emitter.onCompleted() }
                    .onFailure {
                        when (it) {
                            is CancellationException -> {} /* ignore */
                            else -> emitter.onError(it)
                        }
                    }
            }

            emitter.setCancellation { job.cancel() }
            job.start()
        },
        backpressureMode
    )
}

fun <T> rxObservable(
    context: CoroutineContext,
    backpressureMode: Emitter.BackpressureMode,
    block: suspend ProducerScope<T>.() -> Unit
): Observable<T> {
    return callbackFlow { block() }
        .asObservable(context, backpressureMode)
}