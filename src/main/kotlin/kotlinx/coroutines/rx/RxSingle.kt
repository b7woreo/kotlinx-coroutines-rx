package kotlinx.coroutines.rx

import kotlinx.coroutines.*
import kotlinx.coroutines.rx.internal.RxCoroutineScope
import rx.Single
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Single<T>.awaitSingle(): T = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        { cont.resume(it) },
        { cont.resumeWithException(it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

suspend fun <T> Single<T>.awaitComplete(): Unit = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        { cont.resume(Unit) },
        { cont.resumeWithException(it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

fun <T> rxSingle(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Single<T> {
    return Single.fromEmitter { emitter ->
        val job = RxCoroutineScope.launch(context, CoroutineStart.LAZY) {
            runCatching { block() }
                .onSuccess { emitter.onSuccess(it) }
                .onFailure {
                    when (it) {
                        is CancellationException -> {} /* ignore */
                        else -> emitter.onError(it)
                    }
                }
        }
        emitter.setCancellation { job.cancel() }
        job.start()
    }
}