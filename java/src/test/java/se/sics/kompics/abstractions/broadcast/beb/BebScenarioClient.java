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
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * After waiting a delay of X time, the client sends a BebRequest to the defined nodes
 * Each client will have 1 sent msg and a total of N delivered (Where N equals total nodes)
 */
public class BebScenarioClient extends ComponentDefinition {
    /** Ports **/
    private final Positive<Timer> timer = requires(Timer.class);
    private final Positive<BestEffortBroadcastPort> beb = requires(BestEffortBroadcastPort.class);
    /** Fields **/
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private Set<NetAddress> nodes;
    private NetAddress self;
    private int sent = 0;
    private int delivered = 0;

    public BebScenarioClient(BSCInit init) {
        this.self = init.self;
        this.nodes = new HashSet<>();

       subscribe(startHandler, control);
       subscribe(bebDeliverHandler, beb);
       subscribe(timeoutHandler, timer);
   }

   /** Handlers **/

    private final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            for (int i = 1; i < TestUtils.BEB_NODES+1; i++) {
                try {
                    NetAddress addr = new NetAddress(InetAddress.getByName(TestUtils.NODE_ADDR_PREFIX + String.valueOf(i)), TestUtils.NODE_PORT);
                    nodes.add(addr);
                } catch (Exception e) {
                }
            }

            // Just to make sure that all components actually start before each BebNode
            // starts sending a request.
            setDelay(2000);
        }
    };

   private final Handler<BebDeliver> bebDeliverHandler = new Handler<BebDeliver>() {
       @Override
       public void handle(BebDeliver bebDeliver) {
           res.put(self.toString()+"delivered", ++delivered);
       }
   };


    private final Handler<Timeout> timeoutHandler = new Handler<Timeout>() {
        @Override
        public void handle(Timeout timeout) {
            trigger(new BebRequest(new BebEvent(), nodes, self), beb);
            res.put(self.toString()+"sent", ++sent);
        }
    };


    /** Others **/

   public static class BSCInit extends se.sics.kompics.Init<BebScenarioClient> {
        private final NetAddress self;

        public BSCInit(NetAddress self) {
            this.self = self;
        }
    }

    private class BebEvent implements KompicsEvent {
    }

    private class BebClientDelay extends se.sics.kompics.timer.Timeout {
        public BebClientDelay(ScheduleTimeout request) {
            super(request);
        }
    }


    private void setDelay(long delay) {
        ScheduleTimeout scheduledTimeout = new ScheduleTimeout(delay);
        scheduledTimeout.setTimeoutEvent(new BebClientDelay(scheduledTimeout));
        trigger(scheduledTimeout, timer);
    }
}
