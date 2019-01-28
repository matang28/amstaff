package amstaff.core.handlers

import amstaff.core.SampleHandler
import amstaff.core.SampleResult
import amstaff.core.SampleStatus
import amstaff.core.Sampler
import amstaff.core.dsl.ScheduledSamplingDsl

class SlackHandler(apiKey: String) : SampleHandler {

    lateinit var message: String

    lateinit var channel: String

    override suspend fun invoke(sampler: Sampler, result: SampleResult): SampleStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun ScheduledSamplingDsl.slack(apiKey: String, build: SlackHandler.() -> Unit): SlackHandler {
    val instance = SlackHandler(apiKey)
    instance.build()
    return instance
}