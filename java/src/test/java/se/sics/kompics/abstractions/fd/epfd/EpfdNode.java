package se.sics.kompics.abstractions.fd.epfd;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.abstractions.links.perfect.PerfectLink;
import se.sics.kompics.abstractions.links.perfect.PerfectLinkPort;
import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class EpfdNode extends ComponentDefinition {
    /** Ports **/
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);

    /** Components **/
    private Component epfd;
    private Component epfdClient;
    private Component pLink;


    /** Fields **/
    private NetAddress self;
    private long timeout = 2000;


    public EpfdNode(Init init) {
        this.self = init.self;
        this.epfd = create(SimpleEFD.class, new SimpleEFD.Init(timeout, self));
        this.pLink = create(PerfectLink.class, new PerfectLink.Pp2pInit(self));
        this.epfdClient = create(EpfdScenarioClient.class, new EpfdScenarioClient.Init(self));

        // Connections
        connect(epfd.getPositive(EventualFailureDetector.class), epfdClient.getNegative(EventualFailureDetector.class), Channel.TWO_WAY);
        connect(timer, epfdClient.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, epfdClient.getNegative(Network.class), Channel.TWO_WAY);
        connect(pLink.getPositive(PerfectLinkPort.class), epfd.getNegative(PerfectLinkPort.class), Channel.TWO_WAY);
        connect(net, pLink.getNegative(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<EpfdNode> {
        private final NetAddress self;

        public Init(NetAddress self) {
            this.self = self;
        }
    }
}
