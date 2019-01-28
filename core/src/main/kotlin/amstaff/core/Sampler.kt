package amstaff.core

import com.github.shyiko.skedule.Schedule

sealed class SampleStatus
object SampleOk : SampleStatus()
object SampleWarn : SampleStatus()
object SampleCritical : SampleStatus()
object SampleNoData : SampleStatus()

typealias SampleResult = Pair<String, SampleStatus>

interface Sampler {
    suspend fun probe(): SampleResult
}

@FunctionalInterface
interface SampleHandler {
    suspend operator fun invoke(sampler: Sampler, result: SampleResult): SampleStatus
}

data class ScheduledSampling(
    val name: String,
    val tags: Set<String>,
    val sampler: Sampler,
    val timing: Schedule,
    val handlers: Sequence<SampleHandler>
)
