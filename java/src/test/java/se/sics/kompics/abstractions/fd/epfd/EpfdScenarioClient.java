package se.sics.kompics.abstractions.fd.epfd;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class EpfdScenarioClient extends ComponentDefinition {
    /** Ports **/
    private final Positive<EventualFailureDetector> epfd = requires(EventualFailureDetector.class);
    private final Positive<Timer> timer = requires(Timer.class);
    private final Positive<Network> net = requires(Network.class);

    /** Fields **/


    public EpfdScenarioClient(Init init) {
        //subscribe(nodesHandler, net);
        //subscribe(responseHandler, epfd);
        subscribe(startHandler, control);
    }

    /** Handlers **/

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            setDelay(3000);
        }
    };

    private Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect event) {
            System.out.println("WE SUSPECTED");
        }
    };

    private Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore restore) {
            System.out.println("WE RESTORED");
        }
    };


    public static class Init extends se.sics.kompics.Init<EpfdScenarioClient> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }

    private class EpfdClientDelay extends se.sics.kompics.timer.Timeout {
        public EpfdClientDelay(ScheduleTimeout request) {
            super(request);
        }
    }


    private void setDelay(long delay) {
        ScheduleTimeout scheduledTimeout = new ScheduleTimeout(delay);
        scheduledTimeout.setTimeoutEvent(new EpfdClientDelay(scheduledTimeout));
        trigger(scheduledTimeout, timer);
    }

}
