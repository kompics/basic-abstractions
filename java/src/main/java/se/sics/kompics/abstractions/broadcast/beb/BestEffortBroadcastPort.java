package se.sics.kompics.abstractions.broadcast.beb;


import se.sics.kompics.PortType;

public class BestEffortBroadcastPort extends PortType {
    {
        request(BebRequest.class);
        indication(BebDeliver.class);
    }
}
