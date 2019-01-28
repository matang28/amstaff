package amstaff.core

object DI {

    internal object TimingProviderImpl :
        BestNextTimingProvider(), TimingProvider

    object SchedulerImpl :
        NaiveScheduler(1, TimingProviderImpl), Scheduler
}