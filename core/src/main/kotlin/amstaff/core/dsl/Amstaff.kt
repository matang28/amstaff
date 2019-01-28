package amstaff.core.dsl

import amstaff.core.SampleHandler
import amstaff.core.Sampler
import amstaff.core.ScheduledSampling
import com.github.shyiko.skedule.Schedule

fun amstaff(module: String = "base", dsl: AmstaffDsl.() -> Unit): List<ScheduledSampling> {
    val instance = AmstaffDsl()
    instance.dsl()
    return instance.samplings
}

class AmstaffDsl {

    val samplings: List<ScheduledSampling> = mutableListOf()

    fun monitor(name: String, dsl: ScheduledSamplingDsl.() -> Unit): Unit {
        val instance = ScheduledSamplingDsl()
        instance.dsl()

        val scheduledSampling = ScheduledSampling(
            name,
            instance.tags,
            instance.sampler,
            instance.timing,
            instance.handlers
        )

        this.samplings.plus(scheduledSampling)
    }
}

class ScheduledSamplingDsl {

    var tags: Set<String> = setOf()

    lateinit var sampler: Sampler

    lateinit var timing: Schedule

    var handlers: Sequence<SampleHandler> = sequenceOf()

}