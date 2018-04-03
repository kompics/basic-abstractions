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

package se.sics.kompics.abstractions.links.perfect;

import se.sics.kompics.*;
import se.sics.kompics.abstractions.network.Message;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;

public class PerfectLinkComp extends ComponentDefinition {
    /** Ports **/
    private final Negative<PerfectLink> pLink = provides(PerfectLink.class);
    private final Positive<Network> net = requires(Network.class);

    /** Fields **/
    private NetAddress self;

    public PerfectLinkComp(Init init) {
        this.self = init.self;
        subscribe(plSendHandler, pLink);
        subscribe(plDeliverHandler, net);
    }

    /** Handlers **/
    private final Handler<Pp2pSend> plSendHandler = new Handler<Pp2pSend>() {
        @Override
        public void handle(Pp2pSend s) {
            trigger(new Message(self, s.dest, new Pp2pDeliver(self, s.payload)), net);
        }
    };


    private final ClassMatchedHandler<Pp2pDeliver, Message> plDeliverHandler = new ClassMatchedHandler<Pp2pDeliver, Message>() {
        @Override
        public void handle(Pp2pDeliver content, Message m) {
            trigger(content, pLink);
        }
    };


    public static class Init extends se.sics.kompics.Init<PerfectLinkComp> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }

}
