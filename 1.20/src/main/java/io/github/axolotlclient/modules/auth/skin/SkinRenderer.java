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

package io.github.axolotlclient.modules.auth.skin;

import com.mojang.blaze3d.lighting.DiffuseLighting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import org.jetbrains.annotations.Nullable;

public class SkinRenderer {
	private static PlayerEntityModel<?> classicModel, slimModel;
	private static final MinecraftClient minecraft = MinecraftClient.getInstance();

	private SkinRenderer() {
	}

	public static void render(GuiGraphics graphics, boolean classicVariant,
							  Identifier skinTexture,
							  @Nullable Identifier cape,
							  float rotationX,
							  float rotationY,
							  float pivotY,
							  int x0,
							  int y0,
							  int x1,
							  int y1,
							  float scale) {
		if (classicModel == null && classicVariant) {
			classicModel = new PlayerEntityModel<>(minecraft.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER), false);
			classicModel.child = false;
		}
		if (slimModel == null && !classicVariant) {
			slimModel = new PlayerEntityModel<>(minecraft.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_SLIM), true);
			slimModel.child = false;
		}

		int width = x1 - x0;
		DiffuseLighting.setupInventoryEntityLighting();
		graphics.getMatrices().push();
		graphics.getMatrices().translate(x0 + width / 2.0F, (float) (y1), 100.0F);
		graphics.getMatrices().scale(scale, scale, scale);
		graphics.getMatrices().translate(0.0F, -0.0625F, 0.0F);
		graphics.getMatrices().rotateAround(Axis.X_POSITIVE.rotationDegrees(rotationX), 0.0F, pivotY, 0.0F);
		graphics.getMatrices().multiply(Axis.Y_POSITIVE.rotationDegrees(rotationY));
		graphics.draw();
		graphics.getMatrices().push();
		graphics.getMatrices().scale(1.0F, 1.0F, -1.0F);
		graphics.getMatrices().translate(0.0F, -1.5F, 0.0F);
		var model = classicVariant ? classicModel : slimModel;
		RenderLayer renderLayer = RenderLayer.getEntityAlpha(skinTexture);
		model.render(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(renderLayer), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		if (cape != null) {
			graphics.getMatrices().translate(0.0F, 0.0F, 0.125F);
			graphics.getMatrices().multiply(Axis.X_POSITIVE.rotationDegrees(6.0F));
			graphics.getMatrices().multiply(Axis.Y_POSITIVE.rotationDegrees(180.0F));
			model.renderCape(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(RenderLayer.getEntityAlpha(cape)), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
		}
		graphics.getMatrices().pop();
		graphics.draw();
		graphics.getMatrices().pop();
		DiffuseLighting.setup3DGuiLighting();
	}
}
