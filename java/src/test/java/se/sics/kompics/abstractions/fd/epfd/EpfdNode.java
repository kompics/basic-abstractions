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

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.abstractions.links.perfect.PerfectLink;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkComp;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class EpfdNode extends ComponentDefinition {
    /** Ports **/
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);

    /** Components **/
    private Component epfd;
    private Component epfdClient;
    private Component pLink;


    /** Fields **/
    private NetAddress self;
    private long timeout = 200;


    public EpfdNode(Init init) {
        this.self = init.self;
        Set<NetAddress> members = new HashSet<>();
        for (int i = 1; i < TestUtils.EPFD_NODES+1; i++) {
            try {
                NetAddress addr= new NetAddress(InetAddress.getByName(TestUtils.NODE_ADDR_PREFIX + String.valueOf(i)), TestUtils.NODE_PORT);
                members.add(addr);
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }

        members.remove(self);

        this.epfd = create(EpfdComp.class, new EpfdComp.Init(members, self, timeout));
        this.pLink = create(PerfectLinkComp.class, new PerfectLinkComp.Init(self));
        this.epfdClient = create(EpfdScenarioClient.class, new EpfdScenarioClient.Init(self));

        // Connections
        connect(epfd.getPositive(EventuallyPerfectFailureDetector.class),
                epfdClient.getNegative(EventuallyPerfectFailureDetector.class), Channel.TWO_WAY);
        connect(timer, epfd.getNegative(Timer.class), Channel.TWO_WAY);
        connect(pLink.getPositive(PerfectLink.class), epfd.getNegative(PerfectLink.class), Channel.TWO_WAY);
        connect(net, pLink.getNegative(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<EpfdNode> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
