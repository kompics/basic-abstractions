package se.sics.kompics.abstractions.scala.broadcast.beb

import java.net.InetAddress

import se.sics.kompics.KompicsEvent
import se.sics.kompics.abstractions.network.NetAddress
import se.sics.kompics.sl.{ComponentDefinition, PositivePort, handle}
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}

case class TestTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)


class Test extends ComponentDefinition {
  private val beb: PositivePort[BestEffortBroadcast] = requires[BestEffortBroadcast]
  private val timer = requires[Timer]

  case class TestEvent(id: Int) extends KompicsEvent

  ctrl uponEvent {
    case _ => handle {
      startTimer(200)
      val s1 = new NetAddress(InetAddress.getByName("localhost"), 5000)
      val request: BebRequest = BebRequest(TestEvent(1), Set(s1), s1)
      trigger(request -> beb)
    }
  }

  beb uponEvent {
    case BebDeliver(t: TestEvent, src) => handle {
      println(t)
    }
  }

  private def startTimer(delay: Long): Unit = {
    val timeout = new ScheduleTimeout(delay)
    timeout.setTimeoutEvent(TestTimeout(timeout))
    trigger(timeout -> timer)
  }
}
