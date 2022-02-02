package kotlinx.coroutines.rx

import kotlinx.coroutines.*
import kotlinx.coroutines.rx.internal.RxScope
import rx.Completable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Completable.await(): Unit = suspendCancellableCoroutine { cont ->
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
    val job = Job(context[Job])
    return Completable.create { subscriber ->
        RxScope.launch(context + job) {
            runCatching { block() }
                .onSuccess { subscriber.onCompleted() }
                .onFailure { subscriber.onError(it) }
        }
    }.doOnUnsubscribe { job.cancel() }
}