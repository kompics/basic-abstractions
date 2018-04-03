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

import se.sics.kompics.Init;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EpfdScenario {

    private static final Operation1 startServerOp = new Operation1<StartNodeEvent, Integer>() {
        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName(TestUtils.NODE_ADDR_PREFIX + self), TestUtils.NODE_PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public NetAddress getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return EpfdNode.class;
                }

                @Override
                public String toString() {
                    return "StartNode<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return new EpfdNode.Init(selfAdr);
                }
            };
        }
    };


    private static Operation1 killOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer self) {
            return new KillNodeEvent() {
                NetAddress addr;
                {
                    try {
                        addr = new NetAddress(InetAddress.getByName(TestUtils.NODE_ADDR_PREFIX  + self), TestUtils.NODE_PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public NetAddress getNodeAddress() {
                    return addr;
                }

                @Override
                public String toString() {
                    return "Kill<" + addr.toString() + ">";
                }
            };
        }
    };


    public static SimulationScenario oneFailure(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess nodes = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess kill = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killOp, new BasicIntSequentialDistribution((1)));
                    }
                };

                nodes.start();
                kill.startAfterTerminationOf(10000, nodes);
                terminateAfterTerminationOf(50000, nodes);
            }
        };
    }

}
