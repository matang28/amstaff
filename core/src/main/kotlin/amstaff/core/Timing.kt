package amstaff.core

import com.github.shyiko.skedule.Schedule
import java.time.LocalTime
import java.time.temporal.ChronoUnit

val Int.seconds : Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.SECONDS)

val Int.minutes : Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.MINUTES)

val Int.hours : Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.HOURS)

val Int.days : Pair<Long, ChronoUnit>
    get() = Pair(this.toLong(), ChronoUnit.DAYS)

fun every(timeElapsed: Pair<Long, ChronoUnit>): Schedule {
    return Schedule.every(timeElapsed.first, timeElapsed.second)!!
}

fun at(localTime: LocalTime): Schedule.AtScheduleBuilder {
    return Schedule.at(localTime)!!
}