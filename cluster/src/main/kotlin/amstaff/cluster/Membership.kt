package amstaff.cluster

import io.atomix.utils.net.Address
import amstaff.cluster.utils.Random
import amstaff.cluster.utils.seconds
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.time.Duration

open class Member(
    open val uniqueName: String,
    open val address: Address = Address.from(Random.randomPort())
) {
    fun asJson(): JSONObject {
        val json = JSONObject()

        json["uniqueName"] = uniqueName
        json["address"] = "${address.host()}:${address.port()}"

        return json
    }

    companion object {
        fun fromJson(string: String): Member {
            val json = JSONParser().parse(string) as JSONObject
            return Member(json["uniqueName"] as String, Address.from(json["address"] as String))
        }
    }
}

data class JoinedMember(
    override val uniqueName: String,
    override val address: Address,
    val isReachable: Boolean,
    val isActive: Boolean
) : Member(uniqueName, address)

sealed class MembershipDiscovery

class BootstrapDiscovery(vararg val seedMembers: Member) : MembershipDiscovery()

class MulticastDiscovery(val broadcastInterval: Duration = 1.seconds,
                         val failureThreshold: Int = 10,
                         val failureTimeout: Duration = 10.seconds
) : MembershipDiscovery()