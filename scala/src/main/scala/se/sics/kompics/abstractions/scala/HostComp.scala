package se.sics.kompics.abstractions.scala

import java.net.InetAddress

import se.sics.kompics.Init
import se.sics.kompics.abstractions.network.NetAddress
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.sl.ComponentDefinition
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer

class HostComp extends ComponentDefinition {
  private val self = new NetAddress(InetAddress.getByName("localhost"), 5000)
  private val timer = create(classOf[JavaTimer], Init.NONE)
  private val net = create(classOf[NettyNetwork], new NettyInit(self))
  private val parent = create(classOf[ParentComp], Init.NONE)

  {
    connect[Timer](timer -> parent)
    connect[Network](net -> parent)
  }
}
