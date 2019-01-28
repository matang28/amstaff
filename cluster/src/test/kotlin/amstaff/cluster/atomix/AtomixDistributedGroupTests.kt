package amstaff.cluster.atomix

import amstaff.cluster.MembershipEvent
import amstaff.cluster.MembershipEventType
import amstaff.cluster.join
import amstaff.cluster.testutils.TestCluster
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

class AtomixDistributedGroupTests {

    @Test
    fun `All joined members can be discovered using the members() method`() = runBlocking {

        val testCluster = TestCluster()

        Assert.assertEquals(testCluster.dg1.members().count(), 3)

        val names2 = testCluster.dg2.members().map { it.uniqueName }.toList()
        val names3 = testCluster.dg3.members().map { it.uniqueName }.toList()

        testCluster.dg1.members()
            .map { it.uniqueName }
            .forEach {
                Assert.assertTrue(testCluster.bootstrap.contains(it))
                Assert.assertTrue(names2.contains(it))
                Assert.assertTrue(names3.contains(it))
            }

        testCluster.dispose()
    }

    @Test
    fun `Members should notify each other when leaving or joining the cluster`() = runBlocking {

        val testCluster = TestCluster()

        val leaveLatch = CountDownLatch(1)
        val joinLatch = CountDownLatch(1)

        testCluster.dg1.onMembershipChanges { event ->
            clusterChangesHandler(event, testCluster, joinLatch, leaveLatch)
        }

        testCluster.dg2.disconnect()
        leaveLatch.await(10, TimeUnit.SECONDS)

        assertEquals(2, testCluster.dg1.members().count())
        assertEquals(2, testCluster.dg3.members().count())

        testCluster.dg2 = testCluster.member2 join testCluster.cluster
        joinLatch.await(10, TimeUnit.SECONDS)

        assertEquals(3, testCluster.dg1.members().count())
        assertEquals(3, testCluster.dg2.members().count())
        assertEquals(3, testCluster.dg3.members().count())

        Assert.assertEquals(leaveLatch.count, 0)
        Assert.assertEquals(joinLatch.count, 0)

        testCluster.dispose()
    }

    @Test
    fun `Members should be able to direct message with each other`() = runBlocking {

        val testCluster = TestCluster()

        val from = testCluster.member1
        val to = testCluster.member2

        val payload = "Hello"

        val messageLatch = CountDownLatch(1)
        testCluster.dg2.onMessageReceived { message ->
            Assert.assertEquals(message.origin.uniqueName, from.uniqueName)
            Assert.assertEquals(message.content, payload)
            messageLatch.countDown()
        }

        testCluster.dg3.onMessageReceived { message ->
            fail()
        }

        testCluster.dg1.sendMessage(from, to, payload)

        messageLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, messageLatch.count)

        testCluster.dispose()
    }

    @Test
    fun `Members should be able to broadcast messages to the whole cluster`() = runBlocking {

        val testCluster = TestCluster()

        val from = testCluster.member1
        val payload = "Hello"

        val messageLatch = CountDownLatch(2)
        testCluster.dg2.onMessageReceived { message ->
            Assert.assertEquals(message.origin.uniqueName, from.uniqueName)
            Assert.assertEquals(message.content, payload)
            messageLatch.countDown()
        }

        testCluster.dg3.onMessageReceived { message ->
            Assert.assertEquals(message.origin.uniqueName, from.uniqueName)
            Assert.assertEquals(message.content, payload)
            messageLatch.countDown()
        }

        testCluster.dg1.broadcastMessage(from, testCluster.cluster, payload)

        messageLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, messageLatch.count)

        testCluster.dispose()
    }

    private fun clusterChangesHandler(
        event: MembershipEvent,
        testCluster: TestCluster,
        joinLatch: CountDownLatch,
        leaveLatch: CountDownLatch
    ) {
        when (event.type) {
            MembershipEventType.MEMBER_ADDED -> {
                Assert.assertEquals(event.origin.uniqueName, testCluster.member2.uniqueName)
                joinLatch.countDown()
            }
            MembershipEventType.MEMBER_REMOVED -> {
                Assert.assertEquals(event.origin.uniqueName, testCluster.member2.uniqueName)
                leaveLatch.countDown()
            }
        }
    }

}