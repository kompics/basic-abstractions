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
