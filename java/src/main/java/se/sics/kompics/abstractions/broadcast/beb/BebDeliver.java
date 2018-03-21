package se.sics.kompics.abstractions.broadcast.beb;

import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;

public class BebDeliver implements PatternExtractor<Class, KompicsEvent>, Serializable {
    public final KompicsEvent payload;
    public final NetAddress src;

    public BebDeliver(KompicsEvent payload, NetAddress src) {
        this.payload = payload;
        this.src = src;
    }

    public Class extractPattern() {
        return payload.getClass();
    }

    public KompicsEvent extractValue() {
        return payload;
    }
}

