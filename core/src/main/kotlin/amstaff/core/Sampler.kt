package amstaff.core

import com.github.shyiko.skedule.Schedule

sealed class SampleStatus
object SampleOk : SampleStatus()
object SampleWarn : SampleStatus()
object SampleCritical : SampleStatus()

typealias SampleResult = Pair<String, SampleStatus>

interface Sampler {
    val name: String
    val tags: Set<String>
    suspend fun probe(): SampleResult
}

interface OnSampleChanged {
    suspend operator fun invoke(from: SampleStatus, to: SampleResult): SampleStatus
}

data class ScheduledSampling(val sampler: Sampler,
                             val timing: Schedule,
                             val handlers: Sequence<OnSampleChanged>)