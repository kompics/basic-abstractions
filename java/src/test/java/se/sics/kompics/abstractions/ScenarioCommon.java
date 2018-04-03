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

package se.sics.kompics.abstractions;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

public class ScenarioCommon {

    public static SimulationScenario normalBroadcast(final int servers, Operation1<StartNodeEvent, Integer> op) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess nodes = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, op, new BasicIntSequentialDistribution(1));
                    }
                };

                nodes.start();
                terminateAfterTerminationOf(100000, nodes);
            }
        };
    }
}
