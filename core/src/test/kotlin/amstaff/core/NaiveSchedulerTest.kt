package amstaff.core

import amstaff.testutils.ConstDelaysTimingProvider
import amstaff.testutils.LatchSampler
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NaiveSchedulerTest {

    @Test
    fun `Given a finite scheduled sampling instance, the scheduler should probe the sampler a finite num of times`() {

        val ts = NaiveScheduler(
            1,
            ConstDelaysTimingProvider(listOf(5, 3))
        )

        val sampler = LatchSampler(2)

        val sample = simpleSampler(sampler)

        ts.schedule(sample)

        sampler.latch.await(20, TimeUnit.SECONDS)

        assertEquals(0, sampler.latch.count)
    }

    @Test
    fun `Given scheduled sampling instance, all of the handlers should receive the sampling result`() {
        val ts = NaiveScheduler(
            1,
            ConstDelaysTimingProvider(listOf(1))
        )

        val samplerName = "latch"
        val samplerTags = setOf("prod", "my-team")
        val sampler = LatchSampler(1, 0, samplerName, samplerTags)

        val handlersLatch = CountDownLatch(2)
        val handler1 = createHandler(sampler, handlersLatch)
        val handler2 = createHandler(sampler, handlersLatch)

        val sample = ScheduledSampling(
            sampler,
            at(LocalTime.now()).everyDay(),
            sequenceOf(handler1, handler2)
        )

        ts.schedule(sample)

        sampler.latch.await(20, TimeUnit.SECONDS)
        handlersLatch.await(20, TimeUnit.SECONDS)

        assertEquals(0, sampler.latch.count)
        assertEquals(0, handlersLatch.count)
    }

    @Test
    fun `Scheduler should not block when running samplers or handlers, so it should trigger parallel samplings`() {
        val ts = NaiveScheduler(
            1,
            ConstDelaysTimingProvider(
                listOf(
                    1, 1, 1,
                    1, 1, 1,
                    1, 1, 1,
                    1, 1, 1
                )
            )
        )

        val sampler1 = LatchSampler(3, sleepTime = 4)
        val sampler2 = LatchSampler(3, sleepTime = 3)
        val sampler3 = LatchSampler(3, sleepTime = 2)
        val sampler4 = LatchSampler(3, sleepTime = 1)

        val samplers = listOf(
            simpleSampler(sampler1),
            simpleSampler(sampler2),
            simpleSampler(sampler3),
            simpleSampler(sampler4)
        )

        samplers.forEach { ts.schedule(it) }

        sampler1.latch.await(20, TimeUnit.SECONDS)
        sampler2.latch.await(20, TimeUnit.SECONDS)
        sampler3.latch.await(20, TimeUnit.SECONDS)
        sampler4.latch.await(20, TimeUnit.SECONDS)

        assertEquals(0, sampler1.latch.count)
        assertEquals(0, sampler2.latch.count)
        assertEquals(0, sampler3.latch.count)
        assertEquals(0, sampler4.latch.count)
    }

    private fun simpleSampler(sampler1: LatchSampler): ScheduledSampling {
        return ScheduledSampling(
            sampler1,
            at(LocalTime.now()).everyDay(),
            sequenceOf()
        )
    }

    private fun createHandler(sampler: LatchSampler, handlersLatch: CountDownLatch): SampleHandler {
        return object : SampleHandler {
            override suspend fun invoke(s: Sampler, result: SampleResult): SampleStatus {
                assertEquals(sampler, s)
                assertEquals(result.first, "")
                assertEquals(result.second, SampleOk)
                handlersLatch.countDown()
                return SampleOk
            }
        }
    }
}