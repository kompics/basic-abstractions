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

package se.sics.kompics.abstractions.fd.epfd;

import org.junit.Test;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.run.LauncherComp;

import static junit.framework.Assert.assertEquals;

public class EpfdTest {

    @Test
    public void oneFailureSimulationTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario epfdScenario = EpfdScenario.oneFailure(TestUtils.EPFD_NODES);
        epfdScenario.simulate(LauncherComp.class);
        SimulationResultMap res = SimulationResultSingleton.getInstance();
        int port = TestUtils.NODE_PORT;
        String nodePrefix = TestUtils.NODE_ADDR_PREFIX;

        // As the assertions assumptions down below are "static"
        assert(TestUtils.EPFD_NODES == 3);

        int nodeOneRestored = res.get("/" + nodePrefix + 1 + ":" + port +"restore", Integer.class);
        int nodeOneSuspected = res.get("/" + nodePrefix + 1 + ":" + port + "suspect", Integer.class);

        int nodeTwoRestored = res.get("/" + nodePrefix + 2 + ":" + port +"restore", Integer.class);
        int nodeTwoSuspected = res.get("/" + nodePrefix + 2 + ":" + port + "suspect", Integer.class);

        int nodeThreeRestored = res.get("/" + nodePrefix + 3 + ":" + port +"restore", Integer.class);
        int nodeThreeSuspected = res.get("/" + nodePrefix + 3 + ":" + port + "suspect", Integer.class);

        /*
         * Node1 starts first and directly suspects Node2 and Node3.
         * Node2 starts up -> Node1 restores Node2 and Node2 Suspects Node3.
         * Node3 starts up -> Node1 and Node2 Restores Node3.
         * EpfdScenario.oneFailure sends of a killEvent to Node1,
         * Node2 and Node3 suspects Node1.
         */

        assertEquals(nodeOneRestored, 2);
        assertEquals(nodeOneSuspected, 2);

        assertEquals(nodeTwoSuspected, 2);
        assertEquals(nodeTwoRestored, 1);

        assertEquals(nodeThreeSuspected, 1);
        assertEquals(nodeThreeRestored, 0);
    }
}
