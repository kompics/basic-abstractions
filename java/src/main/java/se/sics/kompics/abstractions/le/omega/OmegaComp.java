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

package se.sics.kompics.abstractions.le.omega;

import com.google.common.collect.Sets;
import se.sics.kompics.*;
import se.sics.kompics.abstractions.fd.epfd.EpfdComp;
import se.sics.kompics.abstractions.fd.epfd.EventuallyPerfectFailureDetector;
import se.sics.kompics.abstractions.fd.epfd.Restore;
import se.sics.kompics.abstractions.fd.epfd.Suspect;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Based on the Monarchial EventualLeaderDetection specification, but also code from here
 * https://github.com/Limmen/Distributed-KV-store/blob/master/server/src/main/java/se/kth/id2203/omega/Omega.java
 *
 * TODO: add reconfiguration for omega/epfd and fix component setup
 */
public class OmegaComp extends ComponentDefinition {
    /** Ports **/
    private final Positive<Timer> timer = requires(Timer.class);
    private final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    private final Negative<EventualLeaderDetector> omega = provides(EventualLeaderDetector.class);

    /** Fields **/
    private NetAddress self;
    private NetAddress leader;
    private Set<NetAddress> suspected;
    private Set<NetAddress> members;
    private UUID timeoutId;
    private Component epfdComp;

    public OmegaComp(Init init) {
        this.self = init.self;
        this.suspected = new HashSet<>();
        this.leader = null;
        this.members = init.members;
        this.epfdComp = create(EpfdComp.class, new EpfdComp.Init(init.members, self, init.timeout));

        // Subscriptions
        subscribe(restoreHandler, epfd);
        subscribe(suspectHandler, epfd);
        subscribe(omegaTimeoutHandler, timer);

        // Enable timer
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(init.timeout, init.timeout);
        spt.setTimeoutEvent(new OmegaTimeout(spt));
        trigger(spt, timer);
        timeoutId = spt.getTimeoutEvent().getTimeoutId();
    }

    /** Handlers **/

    private final Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect s) {
            suspected.add(s.suspect);
        }
    };

    private final Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore r) {
            suspected.remove(r.restored);
        }
    };

    private final Handler<OmegaTimeout> omegaTimeoutHandler = new Handler<OmegaTimeout>() {
        @Override
        public void handle(OmegaTimeout omegaTimeout) {
            NetAddress max = maxRank(Sets.difference(members, suspected));
            if (max != null && (leader == null || !leader.equals(max))) {
                leader = max;
                trigger(new Trust(leader), omega);
            }
        }
    };


    public static class Init extends se.sics.kompics.Init<OmegaComp> {
        private final NetAddress self;
        private final Set<NetAddress> members;
        private final long timeout;

        public Init(Set<NetAddress> members, NetAddress self, long timeout) {
            this.self = self;
            this.timeout = timeout;
            this.members = members;
        }
    }

    private class OmegaTimeout extends se.sics.kompics.timer.Timeout {
        public OmegaTimeout(SchedulePeriodicTimeout timeout) {
            super(timeout);
        }
    }


    private NetAddress maxRank(Set<NetAddress> nodes) {
        return nodes.size() > 0 ? Collections.min(nodes) : null;
    }

}
