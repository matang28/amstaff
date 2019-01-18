package amstaff.cluster.utils

import java.time.Duration

val Int.seconds: Duration
    get() = Duration.ofSeconds(this.toLong())