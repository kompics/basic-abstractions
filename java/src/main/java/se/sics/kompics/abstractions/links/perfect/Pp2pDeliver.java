package se.sics.kompics.abstractions.links.perfect;

import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;

public class Pp2pDeliver implements KompicsEvent, Serializable, PatternExtractor<Class, KompicsEvent> {
    private static final long serialVersionUID = -3181045153332189199L;

    public final NetAddress src;
    public final KompicsEvent payload;

    public Pp2pDeliver(NetAddress src, KompicsEvent payload){
        this.src = src;
        this.payload = payload;
    }

    @Override
    public Class extractPattern() {
        return payload.getClass();
    }

    @Override
    public KompicsEvent extractValue() {
        return payload;
    }
}
