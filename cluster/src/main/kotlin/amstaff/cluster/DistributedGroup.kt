package amstaff.cluster

import amstaff.cluster.utils.EventHandler
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.stream.Stream

enum class MembershipEventType {
    MEMBER_ADDED,
    MEMBER_REMOVED,
    MEMBER_UPDATED
}

data class MembershipEvent(
    val origin: Member,
    val type: MembershipEventType
)

data class Message(
    val origin: Member,
    val content: String
) {
    fun asBytes(): ByteArray {
        val json = JSONObject()
        json.put("origin", origin.asJson())
        json.put("content", content)

        return json.toJSONString().toByteArray()
    }

    companion object {
        fun fromBytes(bytesArray: ByteArray): Message {

            val json = JSONParser().parse(String(bytesArray)) as JSONObject

            return Message(Member.fromJson(json["origin"].toString()), json["content"] as String)
        }
    }
}

interface DistributedGroup {

    suspend fun sendMessage(from: Member, to: Member, content: String)

    suspend fun broadcastMessage(from: Member, to: Cluster, content: String)

    fun onMessageReceived(handler: EventHandler<Message>)

    fun onMembershipChanges(handler: EventHandler<MembershipEvent>)

    fun members(): Stream<JoinedMember>

    suspend fun connect(): Unit

    suspend fun disconnect(): Unit

}