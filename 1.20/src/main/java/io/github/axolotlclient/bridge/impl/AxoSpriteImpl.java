/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.bridge.impl;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.bridge.render.AxoSprite;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public interface AxoSpriteImpl extends AxoSprite {
	void draw(MinecraftClient client, GuiGraphics stack, int sX, int sY, int sW, int sH, int color);

	record Simple(Identifier id, int x, int y, int width, int height) implements AxoSpriteImpl {
		@Override
		public void draw(MinecraftClient client, GuiGraphics stack, int sX, int sY, int sW, int sH, int color) {
			stack.setShaderColor(ClientColors.ARGB.redFloat(color), ClientColors.ARGB.greenFloat(color), ClientColors.ARGB.blueFloat(color), ClientColors.ARGB.alphaFloat(color));
			stack.drawTexture(id, sX, sY, x, y, sW, sH, width, height);
			stack.setShaderColor(1, 1, 1, 1);
		}
	}

	record Vanilla(Sprite sprite) implements AxoSpriteImpl {
		@Override
		public void draw(MinecraftClient client, GuiGraphics stack, int sX, int sY, int sW, int sH, int color) {
			stack.setShaderColor(ClientColors.ARGB.redFloat(color), ClientColors.ARGB.greenFloat(color), ClientColors.ARGB.blueFloat(color), ClientColors.ARGB.alphaFloat(color));
			stack.drawSprite(sX, sY, 0, sW, sH, sprite);
			stack.setShaderColor(1, 1, 1, 1);
		}
	}

	record Config(GraphicsOption option) implements AxoSpriteImpl {
		@Override
		public void draw(MinecraftClient client, GuiGraphics stack, int sX, int sY, int sW, int sH, int color) {
			stack.setShaderColor(ClientColors.ARGB.redFloat(color), ClientColors.ARGB.greenFloat(color), ClientColors.ARGB.blueFloat(color), ClientColors.ARGB.alphaFloat(color));
			stack.drawTexture(Util.getTexture(option), sX, sY, 0, 0, sW, sH, option.get().getWidth(), option.get().getHeight());
			stack.setShaderColor(1, 1, 1, 1);
		}
	}
}
