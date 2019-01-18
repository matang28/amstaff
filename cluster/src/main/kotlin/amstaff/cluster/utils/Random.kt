package amstaff.cluster.utils

import java.security.SecureRandom

object Random {
    private val randomSeed = SecureRandom("qweoiasdkjqweoiasjdlkmniqwjleknasmddasd".toByteArray())

    fun randomPort(): Int {
        return 5000 + randomSeed.nextInt(4000)
    }
}