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

import com.google.common.collect.Sets;
import se.sics.kompics.*;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkPort;
import se.sics.kompics.abstractions.links.perfect.Pp2pDeliver;
import se.sics.kompics.abstractions.links.perfect.Pp2pSend;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

public class Epfd extends ComponentDefinition {
    /** Ports **/
    private final Positive<Timer> timer = requires(Timer.class);
    private final Positive<PerfectLinkPort> pLink = requires(PerfectLinkPort.class);
    private final Negative<EpfdPort> epfd = provides(EpfdPort.class);

    /** Fields **/
    private Set<NetAddress> alive;
    private Set<NetAddress> suspected;
    private int seq = 0;
    private NetAddress self;
    private Set<NetAddress> members;
    private long delta;
    private long delay;

    public Epfd(Init init) {
        this.self = init.self;
        this.delta = init.timeout;
        this.delay = this.delta;
        this.members = init.members;
        this.alive = new HashSet<>(members);
        this.suspected = new HashSet<>();

        // Subscriptions
        subscribe(timeoutHandler, timer);
        subscribe(hbReplyHandler, pLink);
        subscribe(hbReqHandler, pLink);
        subscribe(startHandler, control);
    }

    /** Handlers **/

    private final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            startTimer(delay);
        }
    };

    private final Handler<Timeout> timeoutHandler = new Handler<Timeout>() {
        @Override
        public void handle(Timeout timeout) {
            if(!(Sets.intersection(alive, suspected).isEmpty())) {
                delay += delta;
            }

            seq += 1;

            for (NetAddress member : members) {
                if(!alive.contains(member) && !suspected.contains(member)) {
                    suspected.add(member);
                    trigger(new Suspect(member), epfd);
                }
                else if (alive.contains(member) && suspected.contains(member)) {
                    suspected.remove(member);
                    trigger(new Restore(member), epfd);
                }
                trigger(new Pp2pSend(member, new HeartbeatRequest(seq)), pLink);
            }


            alive.clear();
            startTimer(delay);
        }
    };

    private final ClassMatchedHandler<HeartbeatRequest, Pp2pDeliver> hbReqHandler = new ClassMatchedHandler<HeartbeatRequest, Pp2pDeliver>() {
        @Override
        public void handle(HeartbeatRequest heartbeatRequest, Pp2pDeliver pp2pDeliver) {
            trigger(new Pp2pSend(pp2pDeliver.src, new HeartbeatReply(heartbeatRequest.seq)), pLink);
        }
    };

    private final ClassMatchedHandler<HeartbeatReply, Pp2pDeliver> hbReplyHandler = new ClassMatchedHandler<HeartbeatReply, Pp2pDeliver>() {
        @Override
        public void handle(HeartbeatReply heartbeatReply, Pp2pDeliver pp2pDeliver) {
            if(heartbeatReply.seq == seq|| suspected.contains(pp2pDeliver.src)) {
                alive.add(pp2pDeliver.src);
            }
        }
    };

    private void startTimer(long delay) {
        ScheduleTimeout scheduledTimeout = new ScheduleTimeout(delay);
        scheduledTimeout.setTimeoutEvent(new EpfdTimeout(scheduledTimeout));
        trigger(scheduledTimeout, timer);
    }

    private class EpfdTimeout extends se.sics.kompics.timer.Timeout {
        public EpfdTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class Init extends se.sics.kompics.Init<Epfd> {
        private final NetAddress self;
        private final Set<NetAddress> members;
        private final long timeout;

        public Init(Set<NetAddress> members, NetAddress self, long timeout) {
            this.self = self;
            this.timeout = timeout;
            this.members = members;
        }
    }

}
