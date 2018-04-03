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

package se.sics.kompics.abstractions.broadcast.erb;

import org.junit.Test;
import se.sics.kompics.abstractions.ScenarioCommon;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.run.LauncherComp;

import static junit.framework.Assert.assertEquals;

//TODO: Add tests with failures etc..
public class ErbTest {
    @Test
    public void normalScenarioTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario erbScenario = ScenarioCommon.normalBroadcast(TestUtils.ERB_NODES, ErbScenario.erbNodesOp);
        erbScenario.simulate(LauncherComp.class);
        SimulationResultMap res = SimulationResultSingleton.getInstance();
        int port = TestUtils.NODE_PORT;
        String nodePrefix = TestUtils.NODE_ADDR_PREFIX;

        for (int i = 1; i < TestUtils.ERB_NODES+1; i++) {
            int nodeSent = res.get("/" + nodePrefix + i + ":" + port +"sent", Integer.class);
            int nodeDelivered = res.get("/" + nodePrefix + i + ":" + port + "delivered", Integer.class);

            assertEquals(nodeSent, 1);
            assertEquals(nodeDelivered, TestUtils.ERB_NODES);
        }
    }
}
