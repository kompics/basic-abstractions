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

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.abstractions.broadcast.beb.BestEffortBroadcast;
import se.sics.kompics.abstractions.broadcast.beb.BestEffortBroadcastPort;
import se.sics.kompics.abstractions.links.perfect.PerfectLink;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkPort;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ErbNode extends ComponentDefinition {
    /** Ports **/
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);

    /** Components **/
    private Component erbClient;
    private Component erb;
    private Component beb;
    private Component pLink;

    /** Fields **/

    private NetAddress self;

    public ErbNode(Init init) {
        this.self = init.self;
        this.erb = create(EagerReliableBroadcast.class, new EagerReliableBroadcast.Init(self));
        this.erbClient = create(ErbScenarioClient.class, new ErbScenarioClient.Init(self));
        this.pLink = create(PerfectLink.class, new PerfectLink.Pp2pInit(self));
        this.beb = create(BestEffortBroadcast.class, new BestEffortBroadcast.BebInit(self));

        // Connections
        connect(erb.getPositive(EagerReliableBroadcastPort.class), erbClient.getNegative(EagerReliableBroadcastPort.class), Channel.TWO_WAY);
        connect(pLink.getPositive(PerfectLinkPort.class), beb.getNegative(PerfectLinkPort.class), Channel.TWO_WAY);
        connect(net, pLink.getNegative(Network.class), Channel.TWO_WAY);
        connect(timer, erbClient.getNegative(Timer.class), Channel.TWO_WAY);
        connect(erb.getNegative(BestEffortBroadcastPort.class), beb.getPositive(BestEffortBroadcastPort.class), Channel.TWO_WAY);
    }


    public static class Init extends se.sics.kompics.Init<ErbNode> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
