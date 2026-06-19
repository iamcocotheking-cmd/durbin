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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(JoinMultiplayerScreen.class)
public abstract class PortalBDMultiplayerScreenMixin extends Screen {

	protected PortalBDMultiplayerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("HEAD"))
	private void durbin$ensurePortalBDBeforeLoad(CallbackInfo ci) {
		PortalBDPromotedServer.ensure(Minecraft.getInstance());
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void durbin$lockPortalBDButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		ServerData selected = durbin$getSelectedServerData();
		if (!PortalBDPromotedServer.isPortalBD(selected)) {
			return;
		}

		durbin$setButtonActive("editButton", false);
		durbin$setButtonActive("deleteButton", false);
	}

	@Unique
	private ServerData durbin$getSelectedServerData() {
		Object list = durbin$getFieldValue("serverSelectionList");
		if (list == null) {
			list = durbin$findFieldValueBySimpleName("ServerSelectionList");
		}
		if (list == null) {
			return null;
		}

		Object selected = durbin$invokeNoArg(list, "getSelected");
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
	private void durbin$setButtonActive(String fieldName, boolean active) {
		Object value = durbin$getFieldValue(fieldName);
		if (value instanceof Button button) {
			button.active = active;
		}
	}

	@Unique
	private Object durbin$getFieldValue(String fieldName) {
		Class<?> type = this.getClass();
		while (type != null && type != Object.class) {
			try {
				Field field = type.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(this);
			} catch (Exception ignored) {
			}
			type = type.getSuperclass();
		}
		return null;
	}

	@Unique
	private Object durbin$findFieldValueBySimpleName(String simpleName) {
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
