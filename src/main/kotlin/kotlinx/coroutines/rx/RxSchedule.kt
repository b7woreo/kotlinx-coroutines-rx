package kotlinx.coroutines.rx

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import rx.Scheduler
import kotlin.coroutines.CoroutineContext

fun Scheduler.asCoroutineDispatcher(): CoroutineDispatcher {
    return object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            createWorker().schedule { block.run() }
        }
    }
}