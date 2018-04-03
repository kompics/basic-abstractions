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

    @Test
    public void Pp2pSendTest() {
        NetAddress n1 = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
        NetAddress n2 = new NetAddress(InetAddress.getLoopbackAddress(), 12347);

        TestContext<PerfectLinkComp> tc = TestContext.newInstance(PerfectLinkComp.class, new PerfectLinkComp.Init(n1));
        Component comp = tc.getComponentUnderTest();
        Negative<Network> net = comp.getNegative(Network.class);
        Positive<PerfectLink> pLink = comp.getPositive(PerfectLink.class);

        TestEvent event = new TestEvent();
        Pp2pSend pp2pSend = new Pp2pSend(n2, event);

        tc.body()
                .trigger(pp2pSend, pLink)
                .expect(Message.class, (Message m) -> isValid(m, n1, n2), net, Direction.OUT);

        assertTrue(tc.check());
    }

    // Check that the message that is heading "out" has correct src, dst
    // and that it contains a Pp2pDeliver payload
    private boolean isValid(Message m, NetAddress src, NetAddress dst) {
        boolean isDeliver = m.payload instanceof Pp2pDeliver;
        return (isDeliver && m.header.src.equals(src) && m.header.dst.equals(dst));
    }

    private class TestEvent implements KompicsEvent {
    }
}
