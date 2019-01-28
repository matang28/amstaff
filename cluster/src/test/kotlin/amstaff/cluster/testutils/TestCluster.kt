package amstaff.cluster.testutils

import amstaff.cluster.Cluster
import amstaff.cluster.DistributedGroup
import amstaff.cluster.Member
import amstaff.cluster.join
import io.atomix.utils.net.Address
import kotlinx.coroutines.runBlocking

class TestCluster {

    val member1 = Member("member-1", Address.from(9080))
    val member2 = Member("member-2", Address.from(9081))
    val member3 = Member("member-3", Address.from(9082))

    val cluster = Cluster("cluster")
    val bootstrap = listOf(member1.uniqueName, member2.uniqueName, member3.uniqueName)

    lateinit var dg1: DistributedGroup
    lateinit var dg2: DistributedGroup
    lateinit var dg3: DistributedGroup

    init {
        runBlocking {
            dg1 = member1 join cluster
            dg2 = member2 join cluster
            dg3 = member3 join cluster
        }
    }

    fun dispose() = runBlocking {
        dg1.disconnect()
        dg2.disconnect()
        dg3.disconnect()
    }
}