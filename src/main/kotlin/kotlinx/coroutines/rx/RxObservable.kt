package kotlinx.coroutines.rx

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.rx.internal.RxScope
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

fun <T> Observable<T>.asFlow(): Flow<T> = callbackFlow {
    val subscription = subscribe(
        { trySendBlocking(it) },
        { close(cause = it) },
        { close(cause = null) }
    )
    awaitClose { subscription.unsubscribe() }
}

fun <T> Flow<T>.asObservable(
    context: CoroutineContext,
    backpressureMode: Emitter.BackpressureMode
): Observable<T> {
    val job = Job(context[Job])
    return Observable.create<T>(
        { emitter ->
            RxScope.launch(context + job) {
                coroutineContext.job.invokeOnCompletion {
                    if (it == null) {
                        emitter.onCompleted()
                    } else {
                        emitter.onError(it)
                    }
                }
                collect { emitter.onNext(it) }
            }
        },
        backpressureMode
    ).doOnUnsubscribe { job.cancel() }
}

fun <T> rxObservable(
    context: CoroutineContext,
    backpressureMode: Emitter.BackpressureMode,
    block: ProducerScope<T>.() -> Unit
): Observable<T> {
    return callbackFlow { block() }
        .asObservable(context, backpressureMode)
}