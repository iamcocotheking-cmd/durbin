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

import java.lang.reflect.Field;
import java.util.List;

public final class DurbinPortalBDServer {
	public static final String NAME = "PortalBD";
	public static final String IP = "play.portalbd.fun";

	private DurbinPortalBDServer() {
	}

	public static boolean isPortalBD(ServerData serverData) {
		if (serverData == null) {
			return false;
		}
		return NAME.equalsIgnoreCase(serverData.name) || cleanAddress(serverData.ip).equalsIgnoreCase(IP);
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
			List<ServerData> visible = findVisibleServerList(servers);
			if (visible != null) {
				removePortalBDFromOtherLists(servers, visible);

				ServerData promoted = null;
				for (int i = visible.size() - 1; i >= 0; i--) {
					ServerData serverData = visible.get(i);
					if (isPortalBD(serverData)) {
						if (promoted == null) {
							promoted = serverData;
						}
						visible.remove(i);
					}
				}

				if (promoted == null) {
					promoted = new ServerData(NAME, IP, ServerData.Type.OTHER);
				}
				promoted.name = NAME;
				promoted.ip = IP;
				visible.add(0, promoted);
				return;
			}
		} catch (Exception ignored) {
		}

		try {
			ServerData existing = null;
			for (int i = 0; i < servers.size(); i++) {
				ServerData serverData = servers.get(i);
				if (isPortalBD(serverData)) {
					existing = serverData;
					break;
				}
			}

			if (existing == null) {
				servers.add(new ServerData(NAME, IP, ServerData.Type.OTHER), false);
			} else {
				existing.name = NAME;
				existing.ip = IP;
			}
		} catch (Exception ignored) {
		}
	}

	@SuppressWarnings("unchecked")
	private static List<ServerData> findVisibleServerList(ServerList servers) {
		Class<?> type = servers.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (!List.class.isAssignableFrom(field.getType())) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(servers);
					if (value instanceof List<?> list && looksLikeVisibleList(servers, list)) {
						return (List<ServerData>) list;
					}
				} catch (Exception ignored) {
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

	private static boolean looksLikeVisibleList(ServerList servers, List<?> list) {
		try {
			if (list.size() != servers.size()) {
				return false;
			}
			for (int i = 0; i < servers.size(); i++) {
				if (list.get(i) != servers.get(i)) {
					return false;
				}
			}
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static void removePortalBDFromOtherLists(ServerList servers, List<ServerData> visible) {
		Class<?> type = servers.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (!List.class.isAssignableFrom(field.getType())) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(servers);
					if (value instanceof List<?> list && list != visible) {
						((List<ServerData>) list).removeIf(DurbinPortalBDServer::isPortalBD);
					}
				} catch (Exception ignored) {
				}
			}
			type = type.getSuperclass();
		}
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
