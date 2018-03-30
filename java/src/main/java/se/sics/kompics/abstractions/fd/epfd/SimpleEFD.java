/*
 * This file is part of the CaracalDB distributed storage system.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or
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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.HashSet;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author Lars Kroll <lkroll@sics.se>
 */
public class SimpleEFD extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEFD.class);
    // PORTS
    private Negative<EventualFailureDetector> fd = provides(EventualFailureDetector.class);
    private Positive<Network> net = requires(Network.class);
    private Positive<Timer> timer = requires(Timer.class);
    // STATE
    private long period;
    private NetAddress self;
    private HashSet<NetAddress> liveSet = new HashSet<NetAddress>();
    private LinkedListMultimap<NetAddress, SubscribeNodeStatus> subscriptions
            = LinkedListMultimap.create();
    private HashSet<NetAddress> activeSet = new HashSet<NetAddress>();
    private HashSet<NetAddress> lastActiveSet = new HashSet<NetAddress>();
    private UUID timeoutId = null;
    private HashSet<NetAddress> broadcastSet = new HashSet<NetAddress>();

    static {
    }

    public SimpleEFD(Init init) {
        this.period = init.timeout / 2l;
        this.self = init.self;

        // Subscriptions
        subscribe(startHandler, control);
        subscribe(stopHandler, control);
        subscribe(heartbeatHandler, net);
        subscribe(subHandler, fd);
        subscribe(unsubHandler, fd);
        subscribe(timeoutHandler, timer);
    }
    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
            EFDTimeout toe = new EFDTimeout(spt);
            spt.setTimeoutEvent(toe);
            timeoutId = toe.getTimeoutId();
            trigger(spt, timer);
            broadcast();
        }
    };
    Handler<Stop> stopHandler = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            liveSet.clear();
            subscriptions.clear();
            activeSet.clear();
            lastActiveSet.clear();
            broadcastSet.clear();
            if (timeoutId != null) {
                trigger(new CancelPeriodicTimeout(timeoutId), timer);
                timeoutId = null;
            }
            LOG.debug("{}: Stopping", self);
        }
    };
    Handler<Heartbeat> heartbeatHandler = new Handler<Heartbeat>() {

        @Override
        public void handle(Heartbeat event) {
            if (broadcastSet.add(event.getSrc())) {
                trigger(new Heartbeat(self, event.getSrc()), net); // reply immediately
            }
            activeSet.add(event.getSrc());
        }
    };
    Handler<SubscribeNodeStatus> subHandler = new Handler<SubscribeNodeStatus>() {

        @Override
        public void handle(SubscribeNodeStatus event) {
            subscriptions.put(event.node, event);
            broadcastSet.add(event.node);
            liveSet.add(event.node);
            // In case there is a timeout event directly after the subscription
            lastActiveSet.add(event.node);
            trigger(new Heartbeat(self, event.node), net);
        }
    };
    Handler<UnsubscribeNodeStatus> unsubHandler = new Handler<UnsubscribeNodeStatus>() {

        @Override
        public void handle(UnsubscribeNodeStatus event) {
            SubscribeNodeStatus r = null;
            for (SubscribeNodeStatus req : subscriptions.get(event.node)) {
                if (req.requestId.equals(event.requestId)) {
                    r = req;
                    break;
                }
            }
            if (r == null) {
                return;
            }
            subscriptions.remove(r.node, r);
//            if (subscriptions.containsKey(r.node)) {
//                return;
//            }
        }

    };
    Handler<EFDTimeout> timeoutHandler = new Handler<EFDTimeout>() {

        @Override
        public void handle(EFDTimeout event) {
            SetView<NetAddress> allActive = Sets.union(activeSet, lastActiveSet);
            SetView<NetAddress> subbedLive = Sets.intersection(liveSet, subscriptions.keySet());
            SetView<NetAddress> subbedActive = Sets.intersection(allActive, subscriptions.keySet());
            SetView<NetAddress> failed = Sets.difference(subbedLive, subbedActive);
            SetView<NetAddress> restored = Sets.difference(subbedActive, subbedLive);
            for (NetAddress adr : failed) {
                for (SubscribeNodeStatus req : subscriptions.get(adr)) {
                    trigger(new Suspect(req, adr), fd);
                }
            }
            //LOG.debug("{}: Failed: {}", self, failed);
            for (NetAddress adr : restored) {
                for (SubscribeNodeStatus req : subscriptions.get(adr)) {
                    trigger(new Restore(req, adr), fd);
                }
            }
            //LOG.debug("{}: Restored: {}", self, restored);
            lastActiveSet = activeSet;
            activeSet = new HashSet<NetAddress>();
            liveSet.clear();
            liveSet.addAll(subbedActive);
            broadcast();
        }
    };

    private void broadcast() {
        for (NetAddress adr : broadcastSet) {
            trigger(new Heartbeat(self, adr), net);
        }
    }


    public static class Init extends se.sics.kompics.Init<SimpleEFD> {

        public final long timeout;
        public final NetAddress self;

        public Init(long timeout, NetAddress self) {
            this.timeout = timeout;
            this.self = self;
        }
    }

    public static class EFDTimeout extends Timeout {

        public EFDTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
}


