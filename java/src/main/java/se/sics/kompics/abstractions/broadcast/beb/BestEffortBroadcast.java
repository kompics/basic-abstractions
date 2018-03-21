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
