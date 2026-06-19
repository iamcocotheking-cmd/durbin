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

package io.github.axolotlclient.mixin;

import io.github.axolotlclient.durbin.PortalBDPromotedServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@Mixin(JoinMultiplayerScreen.class)
public abstract class PortalBDMultiplayerScreenMixin extends Screen {

	protected PortalBDMultiplayerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("HEAD"))
	private void durbin$ensurePortalBDBeforeLoad(CallbackInfo ci) {
		PortalBDPromotedServer.ensure(Minecraft.getInstance());
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void durbin$lockPortalBDAfterInit(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	/*
	 * Minecraft 1.21.11's JoinMultiplayerScreen does not always own a render(...) method.
	 * Injecting into render caused a startup crash on some builds. updateButtonStatus is the
	 * safer place because vanilla calls it when the selected server changes.
	 */
	@Inject(method = "updateButtonStatus", at = @At("TAIL"), require = 0)
	private void durbin$lockPortalBDAfterButtonStatus(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	/*
	 * Extra safety for builds where the selection update method name changes.
	 * If tick is not present on the target class, require = 0 prevents a crash.
	 */
	@Inject(method = "tick", at = @At("TAIL"), require = 0)
	private void durbin$lockPortalBDOnTick(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	@Unique
	private void durbin$lockPortalBDButtonsIfSelected() {
		ServerData selected = durbin$getSelectedServerData();
		if (!PortalBDPromotedServer.isPortalBD(selected)) {
			return;
		}

		durbin$disableEditDeleteButtons();
	}

	@Unique
	private ServerData durbin$getSelectedServerData() {
		Object list = durbin$getFieldValueByType(ServerSelectionList.class);
		if (list instanceof AbstractSelectionList<?> selectionList) {
			Object selected = selectionList.getSelected();
			ServerData serverData = durbin$getServerDataFromEntry(selected);
			if (serverData != null) {
				return serverData;
			}
		}

		Object reflectedList = durbin$getFieldValueByAssignableSimpleName("ServerSelectionList");
		Object selected = durbin$invokeNoArg(reflectedList, "getSelected");
		return durbin$getServerDataFromEntry(selected);
	}

	@Unique
	private ServerData durbin$getServerDataFromEntry(Object selected) {
		if (selected == null) {
			return null;
		}

		Object fromMethod = durbin$invokeNoArg(selected, "getServerData");
		if (fromMethod instanceof ServerData serverData) {
			return serverData;
		}

		Class<?> type = selected.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (ServerData.class.isAssignableFrom(field.getType())) {
					try {
						field.setAccessible(true);
						Object value = field.get(selected);
						if (value instanceof ServerData serverData) {
							return serverData;
						}
					} catch (Exception ignored) {
					}
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

	@Unique
	private void durbin$disableEditDeleteButtons() {
		Class<?> type = this.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (!Button.class.isAssignableFrom(field.getType())) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(this);
					if (value instanceof Button button && durbin$isEditOrDeleteButton(button)) {
						button.active = false;
					}
				} catch (Exception ignored) {
				}
			}
			type = type.getSuperclass();
		}
	}

	@Unique
	private boolean durbin$isEditOrDeleteButton(Button button) {
		String visible = button.getMessage().getString().toLowerCase(Locale.ROOT);
		String raw = button.getMessage().toString().toLowerCase(Locale.ROOT);
		return visible.contains("edit")
			|| visible.contains("delete")
			|| visible.contains("remove")
			|| raw.contains("selectserver.edit")
			|| raw.contains("selectserver.delete")
			|| raw.contains("selectserver.remove");
	}

	@Unique
	private Object durbin$getFieldValueByType(Class<?> wantedType) {
		Class<?> type = this.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (wantedType.isAssignableFrom(field.getType())) {
					try {
						field.setAccessible(true);
						return field.get(this);
					} catch (Exception ignored) {
					}
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

	@Unique
	private Object durbin$getFieldValueByAssignableSimpleName(String simpleName) {
		Class<?> type = this.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (field.getType().getSimpleName().equals(simpleName)) {
					try {
						field.setAccessible(true);
						return field.get(this);
					} catch (Exception ignored) {
					}
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

	@Unique
	private Object durbin$invokeNoArg(Object target, String methodName) {
		if (target == null) {
			return null;
		}

		Class<?> type = target.getClass();
		while (type != null && type != Object.class) {
			try {
				Method method = type.getDeclaredMethod(methodName);
				method.setAccessible(true);
				return method.invoke(target);
			} catch (Exception ignored) {
			}
			type = type.getSuperclass();
		}
		return null;
	}
}
