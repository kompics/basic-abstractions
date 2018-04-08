package se.sics.kompics.abstractions.scala

import java.net.InetAddress

import se.sics.kompics.Init
import se.sics.kompics.network.Network
import se.sics.kompics.sl.{ComponentDefinition, PositivePort}
import se.sics.kompics.abstractions.network.NetAddress
import se.sics.kompics.abstractions.scala.broadcast.beb.{BebComp, BestEffortBroadcast, Test}
import se.sics.kompics.timer.Timer

class ParentComp extends ComponentDefinition {
  /** Ports **/
  val net: PositivePort[Network] = requires[Network]
  val timer = requires[Timer]

  /** Fields **/
  val self= new NetAddress(InetAddress.getByName("localhost"), 5000)

  /** Children **/
  val test = create(classOf[Test], Init.NONE)
  val beb = create(classOf[BebComp], BebComp.BebInit(self))

  {
    connect[Timer](timer -> test)
    connect[Network](net -> beb)
    connect[BestEffortBroadcast](beb -> test)
  }
}
