package se.sics.kompics.abstractions.links.perfect;


import org.junit.Test;
import se.sics.kompics.Component;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.abstractions.network.Message;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import java.net.InetAddress;

import static org.junit.Assert.assertTrue;

public class PerfectLinkTest {

    //TODO: Improve..
    @Test
    public void Pp2pSendTest() {
        NetAddress n1 = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
        NetAddress n2 = new NetAddress(InetAddress.getLoopbackAddress(), 12347);

        try {
            TestContext<PerfectLink> tc = TestContext.newInstance(PerfectLink.class, new PerfectLink.Pp2pInit(n1));
            Component comp = tc.getComponentUnderTest();
            /*
            Negative<Network> net = comp.getNegative(Network.class);
            Positive<PerfectLinkPort> pLink = comp.getPositive(PerfectLinkPort.class);

            TestEvent event = new TestEvent();
            Pp2pSend pp2pSend = new Pp2pSend(n2, event);

            tc.body()
                    .trigger(pp2pSend, pLink)
                    // Check that the message that is heading "out" has correct src and dst
                    .expect(Message.class, (Message m) -> m.header.src.equals(n1) && m.header.dst.equals(n2),
                            net, Direction.OUT);

            assertTrue(tc.check());
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TestEvent implements KompicsEvent {
    }
}
