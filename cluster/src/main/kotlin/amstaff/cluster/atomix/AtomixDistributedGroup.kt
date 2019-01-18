package amstaff.cluster.atomix

import io.atomix.cluster.ClusterMembershipEvent
import io.atomix.core.Atomix
import io.atomix.utils.net.Address
import amstaff.cluster.*
import amstaff.cluster.utils.EventHandler
import java.util.concurrent.Executors
import java.util.function.BiConsumer
import java.util.stream.Stream

class AtomixDistributedGroup(private val atomix: Atomix) : DistributedGroup {

    private val clusterMessage = "cluster_message"
    private val directMessage = "direct_message"

    override suspend fun sendMessage(from: Member, to: Member, content: String) {
        atomix.messagingService.sendAsync(
            to.address,
            directMessage,
            Message(from, content).asBytes()
        ).join()
    }

    override suspend fun broadcastMessage(from: Member, to: Cluster, content: String) {
        atomix.broadcastService.broadcast(
            clusterMessage,
            Message(from, content).asBytes()
        )
    }

    override fun onMessageReceived(handler: EventHandler<Message>) {
        atomix.broadcastService.addListener(clusterMessage) { message ->
            handler(Message.fromBytes(message))
        }

        atomix.messagingService.registerHandler(
            directMessage,
            BiConsumer { _, message ->
                handler(Message.fromBytes(message))
            },
            Executors.newSingleThreadExecutor()
        )
    }

    override fun onMembershipChanges(handler: EventHandler<MembershipEvent>) {
        atomix.membershipService.addListener { event ->

            val type = when (event?.type()) {
                ClusterMembershipEvent.Type.MEMBER_ADDED -> MembershipEventType.MEMBER_ADDED
                ClusterMembershipEvent.Type.METADATA_CHANGED -> MembershipEventType.MEMBER_UPDATED
                ClusterMembershipEvent.Type.REACHABILITY_CHANGED -> MembershipEventType.MEMBER_UPDATED
                ClusterMembershipEvent.Type.MEMBER_REMOVED -> MembershipEventType.MEMBER_REMOVED
                else -> return@addListener
            }

            val member = event.subject().asJoinedMember()

            handler(MembershipEvent(member, type))
        }
    }

    override fun members(): Stream<JoinedMember> {
        return atomix.membershipService.members
            .stream()
            .map { it.asJoinedMember() }
    }

    override suspend fun connect() {
        atomix.start().join()
    }

    override suspend fun disconnect() {
        atomix.stop().join()
    }
}

fun io.atomix.cluster.Member.asJoinedMember(): JoinedMember {
    return JoinedMember(this.id().id(), this.address(), this.isReachable, this.isActive)
}