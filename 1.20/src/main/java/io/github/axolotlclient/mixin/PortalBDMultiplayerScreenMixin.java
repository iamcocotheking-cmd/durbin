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

import io.github.axolotlclient.durbin.DurbinPortalBDServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@Mixin(MultiplayerScreen.class)
public abstract class PortalBDMultiplayerScreenMixin extends Screen {
	protected PortalBDMultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("HEAD"), require = 0)
	private void durbin$ensurePortalBDBeforeLoad(CallbackInfo ci) {
		DurbinPortalBDServer.ensure(MinecraftClient.getInstance());
	}

	@Inject(method = "init", at = @At("TAIL"), require = 0)
	private void durbin$lockPortalBDAfterInit(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	@Inject(method = "updateButtonActivationStates", at = @At("TAIL"), require = 0)
	private void durbin$lockPortalBDAfterButtonStatus(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	@Inject(method = "tick", at = @At("TAIL"), require = 0)
	private void durbin$lockPortalBDOnTick(CallbackInfo ci) {
		durbin$lockPortalBDButtonsIfSelected();
	}

	@Unique
	private void durbin$lockPortalBDButtonsIfSelected() {
		ServerInfo selected = durbin$getSelectedServerInfo();
		if (DurbinPortalBDServer.isPortalBD(selected)) {
			durbin$disableProtectedButtons();
		}
	}

	@Unique
	private ServerInfo durbin$getSelectedServerInfo() {
		Object list = durbin$getFieldValueByType(MultiplayerServerListWidget.class);
		Object selected = durbin$invokeNoArg(list, "getSelected");
		return durbin$getServerInfoFromEntry(selected);
	}

	@Unique
	private ServerInfo durbin$getServerInfoFromEntry(Object selected) {
		if (selected == null) {
			return null;
		}

		Object fromMethod = durbin$invokeNoArg(selected, "getServer");
		if (fromMethod instanceof ServerInfo serverInfo) {
			return serverInfo;
		}
		fromMethod = durbin$invokeNoArg(selected, "getServerData");
		if (fromMethod instanceof ServerInfo serverInfo) {
			return serverInfo;
		}

		Class<?> type = selected.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (ServerInfo.class.isAssignableFrom(field.getType())) {
					try {
						field.setAccessible(true);
						Object value = field.get(selected);
						if (value instanceof ServerInfo serverInfo) {
							return serverInfo;
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
	private void durbin$disableProtectedButtons() {
		Class<?> type = this.getClass();
		while (type != null && type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (!ButtonWidget.class.isAssignableFrom(field.getType())) {
					continue;
				}
				try {
					field.setAccessible(true);
					Object value = field.get(this);
					if (value instanceof ButtonWidget button && durbin$isProtectedButton(button)) {
						button.active = false;
					}
				} catch (Exception ignored) {
				}
			}
			type = type.getSuperclass();
		}
	}

	@Unique
	private boolean durbin$isProtectedButton(ButtonWidget button) {
		String visible = button.getMessage().getString().toLowerCase(Locale.ROOT);
		String raw = button.getMessage().toString().toLowerCase(Locale.ROOT);
		return visible.contains("edit")
			|| visible.contains("delete")
			|| visible.contains("remove")
			|| visible.contains("up")
			|| visible.contains("down")
			|| raw.contains("selectserver.edit")
			|| raw.contains("selectserver.delete")
			|| raw.contains("selectserver.remove")
			|| raw.contains("selectserver.move")
			|| raw.contains("moveup")
			|| raw.contains("movedown");
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
