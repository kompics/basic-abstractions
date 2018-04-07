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

import se.sics.kompics.*;
import se.sics.kompics.abstractions.links.perfect.PerfectLink;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkComp;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class BebNode extends ComponentDefinition {
    /** Ports **/
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);

    /** Components **/
    private Component bebClient;
    private Component beb;
    private Component pLink;

    /** Fields **/
    private NetAddress self;

    public BebNode(Init init) {
        this.self = init.self;
        this.beb = create(BebComp.class, se.sics.kompics.Init.NONE);
        this.bebClient = create(BebScenarioClient.class, new BebScenarioClient.BSCInit(self));
        this.pLink = create(PerfectLinkComp.class, new PerfectLinkComp.Init(self));

        // Connections
        connect(beb.getPositive(BestEffortBroadcast.class), bebClient.getNegative(BestEffortBroadcast.class), Channel.TWO_WAY);
        connect(pLink.getPositive(PerfectLink.class), beb.getNegative(PerfectLink.class), Channel.TWO_WAY);
        connect(net, pLink.getNegative(Network.class), Channel.TWO_WAY);
        connect(timer, bebClient.getNegative(Timer.class), Channel.TWO_WAY);
    }


    public static class Init extends se.sics.kompics.Init<BebNode> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
