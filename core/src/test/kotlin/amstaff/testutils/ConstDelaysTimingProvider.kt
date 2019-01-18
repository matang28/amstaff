package amstaff.testutils

import amstaff.core.TimingProvider
import com.github.shyiko.skedule.Schedule
import java.util.*

class ConstDelaysTimingProvider(private val delays: List<Long>) : TimingProvider {
    private var index: Int = 0

    override fun nextDelay(schedule: Schedule): Optional<Long> {
        return try {
            Optional.ofNullable(delays[index++])
        } catch (ex: Exception) {
            Optional.empty()
        }
    }
}