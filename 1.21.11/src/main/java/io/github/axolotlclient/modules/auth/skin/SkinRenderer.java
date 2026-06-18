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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;

public class SkinRenderer extends PictureInPictureRenderer<SkinRenderState> {
	private static final Map<String, SkinRenderer> renderers = new ConcurrentHashMap<>();

	public static void closeRenderers() {
		renderers.values().forEach(PictureInPictureRenderer::close);
		renderers.clear();
	}

	public static SkinRenderer getOrCreate(MultiBufferSource.BufferSource bufferSource, Minecraft minecraft, String id) {
		return renderers.computeIfAbsent(id, _id -> new SkinRenderer(bufferSource, minecraft, id));
	}

	private PlayerModel classicModel, slimModel;
	private PlayerCapeModel capeModel;
	private final Minecraft minecraft;
	private final String id;

	private SkinRenderer(MultiBufferSource.BufferSource bufferSource, Minecraft minecraft, String id) {
		super(bufferSource);
		this.minecraft = minecraft;
		this.id = id;
	}

	@Override
	public @NotNull Class<SkinRenderState> getRenderStateClass() {
		return SkinRenderState.class;
	}

	@Override
	protected void renderToTexture(SkinRenderState renderState, PoseStack poseStack) {
		if (classicModel == null && renderState.classicVariant()) {
			classicModel = new PlayerModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
		}
		if (slimModel == null && !renderState.classicVariant()) {
			slimModel = new PlayerModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
		}
		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.PLAYER_SKIN);
		int i = Minecraft.getInstance().getWindow().getGuiScale();
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		float f = renderState.scale() * i;
		matrix4fStack.rotateAround(Axis.XP.rotationDegrees(renderState.rotationX()), 0.0F, f * -renderState.pivotY(), 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-renderState.rotationY()));
		poseStack.translate(0.0F, -1.6010001F, 0.0F);
		var model = renderState.classicVariant() ? classicModel : slimModel;
		RenderType renderType = model.renderType(renderState.skinTexture());
		model.renderToBuffer(poseStack, this.bufferSource.getBuffer(renderType), 15728880, OverlayTexture.NO_OVERLAY);
		if (renderState.cape() != null) {
			if (capeModel == null) {
				capeModel = new PlayerCapeModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_CAPE));
			}
			var type = capeModel.renderType(renderState.cape());
			poseStack.mulPose(Axis.XP.rotationDegrees(6.0F));
			capeModel.renderToBuffer(poseStack, bufferSource.getBuffer(type), 15728880, OverlayTexture.NO_OVERLAY);
		}
		this.bufferSource.endBatch();
		matrix4fStack.popMatrix();
	}

	@Override
	protected @NotNull String getTextureLabel() {
		return "axolotlclient/skin_render/" + id;
	}
}
