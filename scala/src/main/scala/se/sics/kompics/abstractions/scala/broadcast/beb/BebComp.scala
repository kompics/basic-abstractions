/*
 * This file is part of the Kompics Basic Abstractions library
 *
 * Copyright (C) 2018 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2018 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package se.sics.kompics.abstractions.scala.broadcast.beb

import se.sics.kompics.{Channel, Init}
import se.sics.kompics.sl.{ComponentDefinition, NegativePort, PositivePort, handle}
import se.sics.kompics.abstractions.broadcast.beb.
{
  BebComp => BebCompJ,
  BebDeliver => BebDeliverJ,
  BestEffortBroadcast => BestEffortBroadcastJ
}
import se.sics.kompics.abstractions.links.perfect.{PerfectLink, PerfectLinkComp}
import se.sics.kompics.abstractions.network.NetAddress
import se.sics.kompics.abstractions.scala.broadcast.beb.BebComp.BebInit
import se.sics.kompics.network.Network

object BebComp {
  case class BebInit(self: NetAddress) extends Init[BebComp]
}

class BebComp(init: BebInit) extends ComponentDefinition {
  /** Ports **/
  private val bebJava: PositivePort[BestEffortBroadcastJ] = requires[BestEffortBroadcastJ]
  private val beb: NegativePort[BestEffortBroadcast] = provides[BestEffortBroadcast]
  private val net: PositivePort[Network] = requires[Network]

  /** Fields **/
  private val self = init.self

  /** Components **/
  private val bebJavaComp = create(classOf[BebCompJ], Init.NONE)
  private val pLink = create(classOf[PerfectLinkComp], new PerfectLinkComp.Init(self))

  /** Handlers **/
  bebJava uponEvent {
    case deliver: BebDeliverJ => handle {
      trigger(BebDeliver(deliver.payload, deliver.src) -> beb)
    }
  }

  beb uponEvent {
    case req@BebRequest(_, _, _) => handle {
      trigger(req.asJava -> bebJava)
    }
  }

  {
    connect[BestEffortBroadcastJ](bebJavaComp -> this.getComponentCore)
    connect[PerfectLink](pLink -> bebJavaComp)
    connect[Network](net -> pLink)
  }

}
