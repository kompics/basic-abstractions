package se.sics.kompics.abstractions.links.perfect;

import se.sics.kompics.*;
import se.sics.kompics.abstractions.network.Message;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;

public class PerfectLink extends ComponentDefinition {
    /** Ports **/
    private final Negative<PerfectLinkPort> pLink = provides(PerfectLinkPort.class);
    private final Positive<Network> net = requires(Network.class);

    /** Fields **/
    private NetAddress self;

    public PerfectLink(Pp2pInit init) {
        this.self = init.self;
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


    public static class Pp2pInit extends se.sics.kompics.Init<PerfectLink> {
        private final NetAddress self;

        public Pp2pInit(NetAddress self) {
            this.self = self;
        }
    }


    {
        subscribe(plSendHandler, pLink);
        subscribe(plDeliverHandler, net);
    }
}
