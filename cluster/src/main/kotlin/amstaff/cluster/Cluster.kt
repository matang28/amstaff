package amstaff.cluster

import io.atomix.cluster.Node
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider
import io.atomix.cluster.discovery.MulticastDiscoveryProvider
import io.atomix.core.Atomix
import io.atomix.core.AtomixBuilder
import io.atomix.primitive.partition.MemberGroupStrategy
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup
import amstaff.cluster.atomix.AtomixDistributedGroup

data class Cluster(
    val name: String,
    val discoveryMethod: MembershipDiscovery = MulticastDiscovery()
)

suspend infix fun Member.join(cluster: Cluster): AtomixDistributedGroup {

    val member = this

    val atomixCluster = Atomix.builder().apply {

        withClusterId(cluster.name)

        withMemberId(member.uniqueName)

        withAddress(member.address)

        withManagementGroup(
            PrimaryBackupPartitionGroup.builder("${cluster.name}-mgt")
                .withMemberGroupStrategy(MemberGroupStrategy.NODE_AWARE)
                .withNumPartitions(1)
                .build()
        )

        withPartitionGroups(
            PrimaryBackupPartitionGroup.builder("${cluster.name}-prt")
                .withMemberGroupStrategy(MemberGroupStrategy.NODE_AWARE)
                .withNumPartitions(1)
                .build()
        )

        when (cluster.discoveryMethod) {
            is BootstrapDiscovery -> bootstrapDiscovery(cluster.discoveryMethod)
            is MulticastDiscovery -> multicastDiscovery(cluster.discoveryMethod)
        }
    }.build()

    val group = AtomixDistributedGroup(atomixCluster!!)

    group.connect()

    return group
}

private fun AtomixBuilder.bootstrapDiscovery(discoveryMethod: BootstrapDiscovery) {
    val seedMembers = discoveryMethod.seedMembers.map {
        Node.builder().withAddress(it.address).build()
    }

    withMembershipProvider(
        BootstrapDiscoveryProvider.builder()
            .withNodes(*seedMembers.toTypedArray())
            .build()
    )
}

private fun AtomixBuilder.multicastDiscovery(discoveryMethod: MulticastDiscovery) {
    withMulticastEnabled()
    withMembershipProvider(
        MulticastDiscoveryProvider.builder()
            .withBroadcastInterval(discoveryMethod.broadcastInterval)
            .withFailureThreshold(discoveryMethod.failureThreshold)
            .withFailureTimeout(discoveryMethod.failureTimeout)
            .build()
    )
}