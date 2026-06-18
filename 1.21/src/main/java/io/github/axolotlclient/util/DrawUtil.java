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

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class DrawUtil {

	public static void fillRect(GuiGraphics graphics, Rectangle rectangle, Color color) {
		fillRect(graphics, rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + height, color);
	}

	public static void outlineRect(GuiGraphics graphics, Rectangle rectangle, Color color) {
		outlineRect(graphics, rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void outlineRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		fillRect(graphics, x, y, 1, height - 1, color);
		fillRect(graphics, x + width - 1, y + 1, 1, height - 1, color);
		fillRect(graphics, x + 1, y, width - 1, 1, color);
		fillRect(graphics, x, y + height - 1, width - 1, 1, color);
	}

	public static void drawCenteredString(GuiGraphics graphics, TextRenderer renderer, String text, int x, int y,
	                                      Color color, boolean shadow) {
		drawCenteredString(graphics, renderer, text, x, y, color.toInt(), shadow);
	}

	public static void drawCenteredString(GuiGraphics graphics, TextRenderer renderer, String text, int x, int y,
	                                      int color, boolean shadow) {
		drawString(graphics, text, (float) (x - renderer.getWidth(text) / 2), (float) y, color, shadow);
	}

	public static void drawString(GuiGraphics graphics, String text, float x, float y, int color, boolean shadow) {
		graphics.drawText(MinecraftClient.getInstance().textRenderer, text, (int) x, (int) y, color, shadow);
	}

	public static void drawString(GuiGraphics graphics, String text, float x, float y, Color color, boolean shadow) {
		drawString(graphics, text, x, y, color.toInt(), shadow);
	}

	public static void drawOutlines(VertexConsumerProvider.Immediate immediate, Entity camera, Vec3d cameraPos, MatrixStack stack, BlockPos pos, BlockState state, World world) {
		if (AxolotlClient.config().enableCustomOutlines.get() && AxolotlClient.config().outlineFill.get()) {
			var x = pos.getX() - cameraPos.x;
			var y = pos.getY() - cameraPos.y;
			var z = pos.getZ() - cameraPos.z;
			var color = AxolotlClient.config().outlineFillColor.get().toInt();
			var matrix = stack.peek();
			var shape = state.getOutlineShape(world, pos, ShapeContext.of(camera));
			var consumer = immediate.getBuffer(RenderLayer.getGui());
			shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
				var offset = 0.00005; // fix z-fighting
				x1 -= offset;
				x2 += offset;
				y1 -= offset;
				y2 += offset;
				z1 -= offset;
				z2 += offset;
				fillOutlineQuads(consumer, matrix, (float) (x1 + x), (float) (x2 + x), (float) (y1 + y), (float) (y2 + y), (float) (z1 + z), (float) (z2 + z), color);
			});
		}
	}

	private static void fillOutlineQuads(VertexConsumer consumer, MatrixStack.Entry matrix, float x1, float x2, float y1, float y2, float z1, float z2, int color) {
		consumer.xyz(matrix, x2, y1, z1).color(color);
		consumer.xyz(matrix, x1, y1, z1).color(color);
		consumer.xyz(matrix, x1, y2, z1).color(color);
		consumer.xyz(matrix, x2, y2, z1).color(color);

		consumer.xyz(matrix, x1, y1, z2).color(color);
		consumer.xyz(matrix, x2, y1, z2).color(color);
		consumer.xyz(matrix, x2, y2, z2).color(color);
		consumer.xyz(matrix, x1, y2, z2).color(color);

		consumer.xyz(matrix, x1, y2, z2).color(color);
		consumer.xyz(matrix, x1, y2, z1).color(color);
		consumer.xyz(matrix, x1, y1, z1).color(color);
		consumer.xyz(matrix, x1, y1, z2).color(color);

		consumer.xyz(matrix, x2, y2, z1).color(color);
		consumer.xyz(matrix, x2, y2, z2).color(color);
		consumer.xyz(matrix, x2, y1, z2).color(color);
		consumer.xyz(matrix, x2, y1, z1).color(color);

		consumer.xyz(matrix, x2, y1, z1).color(color);
		consumer.xyz(matrix, x2, y1, z2).color(color);
		consumer.xyz(matrix, x1, y1, z2).color(color);
		consumer.xyz(matrix, x1, y1, z1).color(color);

		consumer.xyz(matrix, x2, y2, z2).color(color);
		consumer.xyz(matrix, x2, y2, z1).color(color);
		consumer.xyz(matrix, x1, y2, z1).color(color);
		consumer.xyz(matrix, x1, y2, z2).color(color);
	}
}
