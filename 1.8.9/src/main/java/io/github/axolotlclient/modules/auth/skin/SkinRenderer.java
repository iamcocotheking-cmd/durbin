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

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.model.entity.PlayerModel;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.Nullable;

public class SkinRenderer {
	private static PlayerModel classicModel, slimModel;
	private static final Minecraft minecraft = Minecraft.getInstance();

	private SkinRenderer() {
	}

	public static void render(boolean classicVariant,
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
			classicModel = new PlayerModel(0, false);
			classicModel.isBaby = false;
			classicModel.setVisible(true);
		}
		if (slimModel == null && !classicVariant) {
			slimModel = new PlayerModel(0, true);
			slimModel.isBaby = false;
			slimModel.setVisible(true);
		}

		int width = x1 - x0;
		int light = 15728880;
		GLX.multiTexCoord2f(GLX.GL_TEXTURE1, light % 65536, light / 65536f);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x0 + width / 2.0F, (float) (y1), 00.0F);
		GlStateManager.scalef(scale, scale, 1);
		GlStateManager.translatef(0.0F, -0.0625F, 0.0F);
		GlStateManager.translatef(0, pivotY, 0);
		GlStateManager.rotatef(rotationX, 1, 0, 0);
		GlStateManager.translatef(0, -pivotY, 0);
		GlStateManager.rotatef(rotationY, 0, 1, 0);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(1.0F, 1.0F, -1.0F);
		GlStateManager.translatef(0.0F, -1.5F, 0.0F);
		var model = classicVariant ? classicModel : slimModel;
		minecraft.getTextureManager().bind(skinTexture);
		GlStateManager.enableBlend();
		GlStateManager.enableDepthTest();
		GlStateManager.pushMatrix();
		float k = 0.0625F;
		model.head.render(k);
		model.body.render(k);
		model.rightLeg.render(k);
		model.leftLeg.render(k);
		model.hat.render(k);
		model.leftPants.render(k);
		model.rightPants.render(k);
		model.jacket.render(k);
		model.renderLeftArm();
		model.rightArm.render(0.0625F);
		GlStateManager.translatef(0, 0, -0.62F); // why?
		model.rightSleeve.render(0.0625F);
		GlStateManager.popMatrix();
		if (cape != null) {
			GlStateManager.pushMatrix();
			minecraft.getTextureManager().bind(cape);
			GlStateManager.translatef(0.0F, 0.0F, 0.125F);
			GlStateManager.rotatef(6.0F, 1, 0, 0);
			GlStateManager.rotatef(180.0F, 0, 1, 0);
			model.renderCape(0.0625F);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		GlStateManager.disableDepthTest();
	}
}
