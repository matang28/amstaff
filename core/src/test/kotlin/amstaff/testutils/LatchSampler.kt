package amstaff.testutils

import amstaff.core.SampleOk
import amstaff.core.SampleResult
import amstaff.core.Sampler
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch

class LatchSampler(latchCount: Int,
                   val sleepTime: Int = 0,
                   override val name: String = "latch",
                   override val tags: Set<String> = setOf()
) : Sampler {

    val latch = CountDownLatch(latchCount)

    override suspend fun probe(): SampleResult {
        sleep(sleepTime.toLong())
        latch.countDown()
        return SampleResult("", SampleOk)
    }
}