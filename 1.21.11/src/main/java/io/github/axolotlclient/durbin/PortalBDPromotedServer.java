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

public final class PortalBDPromotedServer {
	public static final String NAME = "PortalBD";
	public static final String IP = "play.portalbd.fun";

	private PortalBDPromotedServer() {
	}

	public static boolean isPortalBD(ServerData serverData) {
		if (serverData == null) {
			return false;
		}
		return cleanAddress(serverData.ip).equalsIgnoreCase(IP) || NAME.equalsIgnoreCase(serverData.name);
	}

	public static void ensure(Minecraft minecraft) {
		if (minecraft == null) {
			return;
		}
		try {
			ServerList servers = new ServerList(minecraft);
			servers.load();

			ServerData visible = findVisiblePortalBD(servers);
			if (visible == null) {
				servers.add(new ServerData(NAME, IP, ServerData.Type.OTHER), false);
			} else {
				visible.name = NAME;
				visible.ip = IP;
			}

			servers.save();
		} catch (Exception ignored) {
		}
	}

	private static ServerData findVisiblePortalBD(ServerList servers) {
		for (int i = 0; i < servers.size(); i++) {
			ServerData serverData = servers.get(i);
			if (isPortalBD(serverData)) {
				return serverData;
			}
		}
		return null;
	}

	private static String cleanAddress(String address) {
		if (address == null) {
			return "";
		}
		String cleaned = address.trim();
		if (cleaned.endsWith(":25565")) {
			cleaned = cleaned.substring(0, cleaned.length() - 6);
		}
		return cleaned;
	}
}
