package amstaff.core


import com.github.shyiko.skedule.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface Scheduler {
    fun schedule(scheduleSampling: ScheduledSampling)
    fun terminate()
}

class NaiveScheduler(threads: Int = 1) : Scheduler {

    private val executor: ScheduledThreadPoolExecutor =
        ScheduledThreadPoolExecutor(threads)

    init {
        executor.removeOnCancelPolicy = true
    }

    override fun schedule(scheduleSampling: ScheduledSampling) {
        executor.schedule(
            newRunnable(scheduleSampling),
            nextDelay(scheduleSampling.timing)!!,
            TimeUnit.SECONDS
        )
    }

    override fun terminate() {
        executor.shutdownNow()
    }

    private fun newRunnable(scheduleSampling: ScheduledSampling): Runnable {
        return dispatch {
            val result = scheduleSampling.sampler.probe()

            scheduleSampling.handlers
                .forEach { handler -> handler(SampleOk, result) }

            val reschedule = nextDelay(scheduleSampling.timing)
            if (reschedule != null && reschedule > 0) {
                executor.schedule(newRunnable(scheduleSampling), reschedule, TimeUnit.SECONDS)
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