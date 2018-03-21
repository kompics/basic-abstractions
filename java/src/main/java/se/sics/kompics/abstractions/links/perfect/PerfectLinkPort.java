package se.sics.kompics.abstractions.links.perfect;

import se.sics.kompics.PortType;

public class PerfectLinkPort extends PortType {
    {
        indication(Pp2pDeliver.class);
        request(Pp2pSend.class);
    }
}
