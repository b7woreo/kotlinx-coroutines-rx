package kotlinx.coroutines.rx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import rx.schedulers.Schedulers
import java.util.concurrent.Executor

class RxScheduleTest {

    @Test
    fun testScheduleAsCoroutineDispatcher() = runBlocking<Unit> {
        val executor = mockk<Executor>()
        every { executor.execute(any()) } answers { (it.invocation.args[0] as Runnable).run() }

        val scheduler = Schedulers.from(executor)
        val dispatcher = scheduler.asCoroutineDispatcher()

        launch(dispatcher) {  }.join()

        verify { executor.execute(any()) }
    }
}