package amstaff.core

import com.github.shyiko.skedule.Schedule
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

val Int.seconds: Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.SECONDS)

val Int.minutes: Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.MINUTES)

val Int.hours: Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.HOURS)

val Int.days: Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.DAYS)

fun every(timeElapsed: Pair<Long, ChronoUnit>): Schedule {
    return Schedule.every(timeElapsed.first, timeElapsed.second)!!
}

fun at(localTime: LocalTime): Schedule.AtScheduleBuilder {
    return Schedule.at(localTime)!!
}

interface TimingProvider {
    fun nextDelay(schedule: Schedule): Optional<Long>
}

internal open class BestNextTimingProvider : TimingProvider {

    override fun nextDelay(schedule: Schedule): Optional<Long> {
        val now = ZonedDateTime.now()

        val iterator = schedule.iterate(now)

        while (iterator.hasNext()) {
            val next = iterator.next().toEpochSecond()

            val nextDelay = next - now.toEpochSecond()

            if (nextDelay > 0) return Optional.ofNullable(nextDelay)
        }

        return Optional.empty()
    }

}