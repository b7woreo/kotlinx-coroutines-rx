package kotlinx.coroutines.rx

import kotlinx.coroutines.*
import kotlinx.coroutines.rx.internal.RxCoroutineScope
import rx.Completable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Completable.awaitComplete(): Unit = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        { cont.resume(Unit) },
        { cont.resumeWithException(it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

fun rxCompletable(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Completable {
    return Completable.fromEmitter { emitter ->
        val job = RxCoroutineScope.launch(context, CoroutineStart.LAZY) {
            runCatching { block() }
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
    }
}