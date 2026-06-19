/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.durbin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

public final class DurbinPortalBDServer {
	public static final String NAME = "PortalBD";
	public static final String IP = "play.portalbd.fun";

	private DurbinPortalBDServer() {
	}

	public static void ensure(Minecraft minecraft) {
		if (minecraft == null) {
			return;
		}
		try {
			ServerList servers = new ServerList(minecraft);
			servers.load();
			ensure(servers);
			servers.save();
		} catch (Exception ignored) {
		}
	}

	public static void ensure(ServerList servers) {
		if (servers == null) {
			return;
		}
		try {
			ServerData existing = servers.get(IP);
			if (existing == null) {
				servers.add(new ServerData(NAME, IP, ServerData.Type.OTHER), true);
			} else {
				existing.name = NAME;
				existing.ip = IP;
			}
		} catch (Exception ignored) {
		}
	}
}
