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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;

import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Graphics;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class Util {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static OptionalDouble lineWidthModifier = OptionalDouble.empty();
	/**
	 * Gets the amount of ticks in between start and end, on a 24000 tick system.
	 *
	 * @param start The start of the time you wish to measure
	 * @param end   The end of the time you wish to measure
	 * @return The amount of ticks in between start and end
	 */
	public static int getTicksBetween(int start, int end) {
		if (end < start)
			end += 24000;
		return end - start;
	}

	public static Text formatFromCodes(String formattedString) {
		MutableText text = Text.empty();
		String[] arr = formattedString.split("§");

		List<Formatting> modifiers = new ArrayList<>();
		for (int i = 0, length = arr.length; i < length; i++) {
			String s = arr[i];
			if (s.isEmpty()) {
				continue;
			} else if (i == 0) {
				text.append(s);
				continue;
			}
			Formatting formatting = Formatting.byCode(s.charAt(0));
			if (formatting != null && formatting.isModifier()) {
				modifiers.add(formatting);
			}
			MutableText part = Text.literal(formatting != null ? s.substring(1) : s);
			if (formatting != null) {
				part.formatted(formatting);

				if (!modifiers.isEmpty()) {
					modifiers.forEach(part::formatted);
					if (formatting.equals(Formatting.RESET)) {
						modifiers.clear();
					}
				}
			}
			text.append(part);
		}
		return text;
	}

	public static void sendChatMessage(String msg) {
		msg = ChatUtil.cutString(StringUtils.normalizeSpace(msg.trim()));
		assert MinecraftClient.getInstance().player != null;
		if (msg.startsWith("/")) {
			MinecraftClient.getInstance().player.networkHandler.sendCommand(msg.substring(1));
		} else {
			MinecraftClient.getInstance().player.networkHandler.sendChatMessage(msg);
		}
	}

	public static void addMessageToChatHud(Text msg) {
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(msg);
	}

	public static Identifier getTexture(GraphicsOption option) {
		return getTexture(option.get(), option.getName());
	}

	public static Identifier getTexture(Graphics graphics, String name) {
		Identifier id = new Identifier(AxolotlClientCommon.MODID, "graphics_" + name.toLowerCase(Locale.ROOT));
		try {
			NativeImageBackedTexture texture;
			var previous = MinecraftClient.getInstance().getTextureManager().getOrDefault(id, null);
			if (previous == null || (previous instanceof NativeImageBackedTexture tex && (tex.getImage() == null || tex.getImage().getHeight() != graphics.getHeight() || tex.getImage().getWidth() != graphics.getWidth()))) {
				texture = new NativeImageBackedTexture(NativeImage.read(graphics.getPixelData()));
				MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
			} else {
				texture = (NativeImageBackedTexture) previous;
				for (int x = 0; x < graphics.getWidth(); x++) {
					for (int y = 0; y < graphics.getHeight(); y++) {
						texture.getImage().setPixelColor(x, y, graphics.getPixelColor(x, y));
					}
				}
			}

			texture.upload();
		} catch (IOException e) {
			AxolotlClientCommon.getInstance().getLogger().error("Failed to bind texture for " + name + ": ", e);
		}
		return id;
	}
}
