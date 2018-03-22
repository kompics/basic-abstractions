package se.sics.kompics.abstractions.broadcast.beb;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import se.sics.kompics.*;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkPort;
import se.sics.kompics.abstractions.links.perfect.Pp2pSend;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import java.net.InetAddress;

import static org.junit.Assert.assertTrue;

public class BestEffortBroadcastTest {

    //TODO: Improve with simulation instead?
    @Test
    public void broadcastTest() {
        NetAddress n1 = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
        NetAddress n2 = new NetAddress(InetAddress.getLoopbackAddress(), 12347);
        NetAddress n3 = new NetAddress(InetAddress.getLoopbackAddress(), 12348);
        ImmutableSet<NetAddress> nodes = ImmutableSet.of(n1, n2, n3);

        TestContext<BestEffortBroadcast> tc = TestContext.newInstance(BestEffortBroadcast.class, new BestEffortBroadcast.BebInit(n1));
        Component comp = tc.getComponentUnderTest();
        Negative<PerfectLinkPort> pLink = comp.getNegative(PerfectLinkPort.class);
        Positive<BestEffortBroadcastPort> beb = comp.getPositive(BestEffortBroadcastPort.class);

        Ping pong = new Ping();
        BebRequest req = new BebRequest(pong, nodes, n1);

        tc.body()
                .trigger(req, beb)
                .expect(Pp2pSend.class, (Pp2pSend send) -> send.dest.sameHostAs(n1), pLink, Direction.OUT)
                .expect(Pp2pSend.class, (Pp2pSend send) -> send.dest.sameHostAs(n2), pLink, Direction.OUT)
                .expect(Pp2pSend.class, (Pp2pSend send) -> send.dest.sameHostAs(n3), pLink, Direction.OUT);

        assertTrue(tc.check());
    }


    private class Ping implements KompicsEvent {

    }
}
