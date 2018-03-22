/*
 * This file is part of the Kompics Basic Abstractions library
 *
 * Copyright (C) 2018 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2018 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TestEvent implements KompicsEvent {
    }
}
