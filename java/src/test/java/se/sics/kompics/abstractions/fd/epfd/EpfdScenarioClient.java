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

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class EpfdScenarioClient extends ComponentDefinition {
    /** Ports **/
    private final Positive<EpfdPort> epfd = requires(EpfdPort.class);

    /** Fields **/
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private final NetAddress self;
    private int suspectCount = 0;
    private int restoreCount = 0;

    public EpfdScenarioClient(Init init) {
        this.self = init.self;

        subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
        subscribe(startHandler, control);
    }

    /** Handlers **/

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            // Create an initial input, to avoid nullptr exceptions after killing nodes
            res.put(self.toString()+"suspect", 0);
            res.put(self.toString()+"restore", 0);
        }
    };

    private Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect event) {
            logger.info(self.toString() + " SUSPECTED " + event.suspect);
            res.put(self.toString()+"suspect", ++suspectCount);
        }
    };

    private Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore restore) {
            logger.info(self.toString()+" RESTORED " + restore.restored);
            res.put(self.toString()+"restore", ++restoreCount);
        }
    };


    public static class Init extends se.sics.kompics.Init<EpfdScenarioClient> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }

}
