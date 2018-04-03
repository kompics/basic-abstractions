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

import se.sics.kompics.*;
import se.sics.kompics.abstractions.broadcast.beb.BebDeliver;
import se.sics.kompics.abstractions.broadcast.beb.BebRequest;
import se.sics.kompics.abstractions.broadcast.beb.BestEffortBroadcastPort;
import se.sics.kompics.abstractions.network.NetAddress;

import java.util.HashSet;
import java.util.Set;

public class EagerReliableBroadcast extends ComponentDefinition {
    /** Ports **/
    private final Positive<BestEffortBroadcastPort> beb = requires(BestEffortBroadcastPort.class);
    private final Negative<EagerReliableBroadcastPort> erb = provides(EagerReliableBroadcastPort.class);

    /** Fields **/
    private Set<KompicsEvent> delivered;
    private NetAddress self;

    public EagerReliableBroadcast(Init init) {
        this.self = init.self;
        this.delivered = new HashSet<>();

        subscribe(erbRequestHandler, erb);
        subscribe(bebDeliverHandler, beb);
    }

    /** Handlers **/

    private Handler<ErbRequest> erbRequestHandler = new Handler<ErbRequest>() {
        @Override
        public void handle(ErbRequest erbRequest) {
            ErbMessage msg = new ErbMessage(erbRequest.payload, erbRequest.nodes);
            trigger(new BebRequest(msg, erbRequest.nodes, self), beb);
        }
    };

    private ClassMatchedHandler<ErbMessage, BebDeliver> bebDeliverHandler= new ClassMatchedHandler<ErbMessage, BebDeliver>() {
        @Override
        public void handle(ErbMessage erbMessage, BebDeliver bebDeliver) {
            KompicsEvent payload = bebDeliver.payload;
            if (!delivered.contains(payload)) {
                delivered.add(payload);
                trigger(new ErbDeliver(payload, bebDeliver.src), erb);
                trigger(new BebRequest(payload, erbMessage.nodes, self), beb);
            }
        }
    };


    public static class Init extends se.sics.kompics.Init<EagerReliableBroadcast> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }

}
