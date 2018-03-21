package se.sics.kompics.abstractions.broadcast.beb;

import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.Set;

public class BebRequest implements KompicsEvent {
    public final KompicsEvent payload;
    public final Set<NetAddress> nodes;
    public final NetAddress src;

    public BebRequest(KompicsEvent payload, Set<NetAddress> nodes, NetAddress src) {
        this.payload = payload;
        this.nodes = nodes;
        this.src = src;
    }
}
