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

package se.sics.kompics.abstractions.broadcast.beb;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import se.sics.kompics.*;
import se.sics.kompics.abstractions.ScenarioCommon;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.abstractions.links.perfect.PerfectLink;
import se.sics.kompics.abstractions.links.perfect.Pp2pSend;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.run.LauncherComp;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BebTest {

    @Test
    public void scenarioTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario bebScenario = ScenarioCommon.normalBroadcast(TestUtils.BEB_NODES, BebScenario.bebNodesOp);
        bebScenario.simulate(LauncherComp.class);
        SimulationResultMap res = SimulationResultSingleton.getInstance();
        int port = TestUtils.NODE_PORT;
        String nodePrefix = TestUtils.NODE_ADDR_PREFIX;

        for (int i = 1; i < TestUtils.BEB_NODES+1; i++) {
            int nodeSent = res.get("/" + nodePrefix + i + ":" + port +"sent", Integer.class);
            int nodeDelivered = res.get("/" + nodePrefix + i + ":" + port + "delivered", Integer.class);

            assertEquals(nodeSent, 1);
            assertEquals(nodeDelivered, TestUtils.BEB_NODES);
        }
    }

    @Test
    public void componentTest() {
        NetAddress n1 = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
        NetAddress n2 = new NetAddress(InetAddress.getLoopbackAddress(), 12347);
        NetAddress n3 = new NetAddress(InetAddress.getLoopbackAddress(), 12348);
        ImmutableSet<NetAddress> nodes = ImmutableSet.of(n1, n2, n3);

        TestContext<BebComp> tc = TestContext.newInstance(BebComp.class, Init.NONE);
        Component comp = tc.getComponentUnderTest();
        Negative<PerfectLink> pLink = comp.getNegative(PerfectLink.class);
        Positive<BestEffortBroadcast> beb = comp.getPositive(BestEffortBroadcast.class);

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
