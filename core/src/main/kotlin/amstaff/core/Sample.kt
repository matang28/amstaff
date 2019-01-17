package amstaff.core

import com.github.shyiko.skedule.Schedule

sealed class SampleStatus
object SampleOk : SampleStatus()
object SampleWarn : SampleStatus()
object SampleCritical : SampleStatus()

typealias SampleResult = Pair<String, SampleStatus>

interface Sample {
    val name: String
    val tags: Set<String>
    suspend fun probe(): SampleResult
}

interface OnSampleChanged {
    suspend operator fun invoke(from: SampleStatus, to: SampleResult): SampleStatus
}

data class ScheduledSample(val sample: Sample,
                           val timing: Schedule,
                           val handlers: Sequence<OnSampleChanged>)
