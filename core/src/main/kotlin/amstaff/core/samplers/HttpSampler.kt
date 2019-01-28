package amstaff.core.samplers

import amstaff.core.SampleResult
import amstaff.core.Sampler
import amstaff.core.dsl.ScheduledSamplingDsl

class HttpSampler : Sampler {

    var url: String = ""

    var okStatus: Set<Int> = setOf()

    override suspend fun probe(): SampleResult {
        TODO("not implemented")
    }
}

fun ScheduledSamplingDsl.httpGet(build: HttpSampler.() -> Unit): HttpSampler {
    val instance = HttpSampler()
    instance.build()
    return instance
}