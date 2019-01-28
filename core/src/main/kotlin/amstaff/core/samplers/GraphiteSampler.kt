package amstaff.core.samplers

import amstaff.core.SampleResult
import amstaff.core.Sampler
import amstaff.core.dsl.ScheduledSamplingDsl

data class GraphiteDataSource(
    val url: String,
    val username: String,
    val password: String
)

class GraphiteSampler(conf: GraphiteDataSource) : Sampler {

    var query: String = ""

    var warning: Int = -1

    var critical: Int = -1

    var range: String = "last 24h"

    override suspend fun probe(): SampleResult {
        TODO("not implemented")
    }
}

fun ScheduledSamplingDsl.graphite(conf: GraphiteDataSource, build: GraphiteSampler.() -> Unit): GraphiteSampler {
    val instance = GraphiteSampler(conf)
    instance.build()
    return instance
}