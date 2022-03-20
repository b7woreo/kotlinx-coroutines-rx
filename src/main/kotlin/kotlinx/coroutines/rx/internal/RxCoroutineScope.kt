package kotlinx.coroutines.rx.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal object RxCoroutineScope: CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined
}