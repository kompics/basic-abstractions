package se.sics.kompics.abstractions.links.perfect;

import se.sics.kompics.abstractions.network.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Pp2pSend implements KompicsEvent, Serializable{
    private static final long serialVersionUID = -4181045153332189199L;

    public final NetAddress dest;
    public final KompicsEvent payload;

    public Pp2pSend(NetAddress dest, KompicsEvent payload){
        this.dest = dest;
        this.payload = payload;
    }
}
