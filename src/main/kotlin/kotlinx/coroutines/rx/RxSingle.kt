package kotlinx.coroutines.rx

import kotlinx.coroutines.*
import kotlinx.coroutines.rx.internal.RxScope
import rx.Single
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Single<T>.await(): T = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        { cont.resume(it) },
        { cont.resumeWithException(it) }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

fun <T> rxSingle(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Single<T> {
    val job = Job(context[Job])
    return Single.create<T> { subscriber ->
        RxScope.launch(context + job) {
            runCatching { block() }
                .onSuccess { subscriber.onSuccess(it) }
                .onFailure { subscriber.onError(it) }
        }
    }.doOnUnsubscribe { job.cancel() }
}