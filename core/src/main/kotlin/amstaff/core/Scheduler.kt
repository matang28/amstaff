package amstaff.core


import com.github.shyiko.skedule.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface Scheduler {
    fun schedule(scheduleSample: ScheduledSample)
    fun terminate()
}

class AmstaffScheduler(threads: Int = 1) : Scheduler {

    private val executor: ScheduledThreadPoolExecutor =
        ScheduledThreadPoolExecutor(threads)

    init {
        executor.removeOnCancelPolicy = true
    }

    override fun schedule(scheduleSample: ScheduledSample) {
        executor.schedule(
            newRunnable(scheduleSample),
            nextDelay(scheduleSample.timing)!!,
            TimeUnit.SECONDS
        )
    }

    override fun terminate() {
        executor.shutdownNow()
    }

    private fun newRunnable(scheduleSample: ScheduledSample): Runnable {
        return dispatch {
            val result = scheduleSample.sample.probe()

            scheduleSample.handlers
                .forEach { handler -> handler(SampleOk, result) }

            val reschedule = nextDelay(scheduleSample.timing)
            if (reschedule != null) {
                executor.schedule(newRunnable(scheduleSample), reschedule, TimeUnit.SECONDS)
            }
        }
    }

    private fun nextDelay(timing: Schedule): Long? {
        val now = ZonedDateTime.now()
        return (timing.next(now).toEpochSecond() - now.toEpochSecond())
    }

    private fun dispatch(work: suspend () -> Unit): Runnable {
        return Runnable {
            GlobalScope.launch {
                work()
            }
        }
    }
}