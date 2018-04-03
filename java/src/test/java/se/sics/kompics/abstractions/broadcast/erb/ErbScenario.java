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

import se.sics.kompics.Init;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ErbScenario {

    public static final Operation1 erbNodesOp = new Operation1<StartNodeEvent, Integer>() {
        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                { try {
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
                    return ErbNode.class;
                }

                @Override
                public String toString() {
                    return "StartNode<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return new ErbNode.Init(selfAdr);
                }
            };
        }
    };
}
