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

import se.sics.kompics.abstractions.links.perfect.PerfectLinkPort;
import se.sics.kompics.abstractions.links.perfect.Pp2pDeliver;
import se.sics.kompics.abstractions.links.perfect.Pp2pSend;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.*;

public class BestEffortBroadcast extends ComponentDefinition {
    /** Ports **/
    private final Positive<PerfectLinkPort> pLink = requires(PerfectLinkPort.class);
    private final Negative<BestEffortBroadcastPort> beb = provides(BestEffortBroadcastPort.class);

    /** Fields **/
    private NetAddress self;

    public BestEffortBroadcast(BebInit init) {
        this.self = init.self;
    }

    /** Handlers **/
    private final Handler<BebRequest> requestHandler = new Handler<BebRequest>() {
        @Override
        public void handle(BebRequest event) {
            for (NetAddress addr: event.nodes) {
                trigger(new Pp2pSend(addr, event.payload), pLink);
            }
        }
    };

    private final Handler<Pp2pDeliver> deliverHandler = new Handler<Pp2pDeliver>() {
        @Override
        public void handle(Pp2pDeliver d) {
            trigger(new BebDeliver(d.payload, d.src), beb);
        }
    };

    public static class BebInit extends se.sics.kompics.Init<BestEffortBroadcast> {
        private final NetAddress self;

        public BebInit(NetAddress self) {
            this.self = self;
        }
    }

    {
        subscribe(requestHandler, beb);
        subscribe(deliverHandler, pLink);
    }
}
