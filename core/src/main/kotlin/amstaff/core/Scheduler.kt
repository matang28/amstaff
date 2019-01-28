package amstaff.core


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface Scheduler {
    fun schedule(scheduleSampling: ScheduledSampling)
    fun terminate()
}

open class NaiveScheduler(
    threads: Int = 1,
    private val timingProvider: TimingProvider
) : Scheduler {

    private val executor: ScheduledThreadPoolExecutor =
        ScheduledThreadPoolExecutor(threads)

    init {
        executor.removeOnCancelPolicy = true
    }

    override fun schedule(scheduleSampling: ScheduledSampling) {
        executor.schedule(
            newRunnable(scheduleSampling),
            timingProvider.nextDelay(scheduleSampling.timing).get(),
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
                .forEach { handler -> handler(scheduleSampling.sampler, result) }

            timingProvider.nextDelay(scheduleSampling.timing)
                .ifPresent { delay ->
                    executor.schedule(
                        newRunnable(scheduleSampling),
                        delay,
                        TimeUnit.SECONDS
                    )
                }
        }
    }

    private fun dispatch(work: suspend () -> Unit): Runnable {
        return Runnable {
            GlobalScope.launch {
                work()
            }
        }
    }
}