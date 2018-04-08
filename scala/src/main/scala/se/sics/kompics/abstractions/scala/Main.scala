package se.sics.kompics.abstractions.scala

import se.sics.kompics.Kompics

object Main extends App {
  Kompics.createAndStart(classOf[HostComp])
  Kompics.waitForTermination()
}
