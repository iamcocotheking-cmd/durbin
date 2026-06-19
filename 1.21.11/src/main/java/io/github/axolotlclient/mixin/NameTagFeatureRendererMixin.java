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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.modules.hypixel.LevelHead;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.duck.NameTagSubmitExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NameTagFeatureRenderer.class)
public abstract class NameTagFeatureRendererMixin {
	@Unique
	private static final Identifier DURBIN_NAMETAG_BADGE = Identifier.fromNamespaceAndPath(AxolotlClientCommon.MODID, "textures/durbin/ui/nametag_badge.png");

	@Unique
	private static final RenderType TEXTURED_TYPE = RenderType.create("axolotlclient_textured_quads", RenderSetup.builder(
			RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath(AxolotlClientCommon.MODID, "pipeline/badge"))
				.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).build())
		.bufferSize(1536)
		.withTexture("Sampler0", DURBIN_NAMETAG_BADGE)
		.setTextureTransform(TextureTransform.DEFAULT_TEXTURING)
		.createRenderSetup());

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V", ordinal = 1, shift = At.Shift.AFTER))
	private void renderBadges(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, Font font, CallbackInfo ci, @Local SubmitNodeStorage.NameTagSubmit submit) {
		if (((NameTagSubmitExtension) (Object) submit).axolotlclient$hasBadge()) {
			var nameStartX = submit.x();
			if (AxolotlClient.config().customBadge.get()) {

				Component badgeText = Util.formatFromCodes(AxolotlClient.config().badgeText.get());
				var x = nameStartX - (font.width(badgeText) + 4);
				Minecraft.getInstance().font.drawInBatch(badgeText, x, 0, -1, AxolotlClient.config().useShadows.get(), submit.pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
			} else {
				var x = nameStartX - 12;
				var builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(TEXTURED_TYPE);
				Matrix4f matrix4f = submit.pose();
				// Durbin badge beside the normal player nametag.
				builder.addVertex(matrix4f, x, -1, 0).setUv(0, 0).setColor(-1);
				builder.addVertex(matrix4f, x, 9, 0).setUv(0, 1).setColor(-1);
				builder.addVertex(matrix4f, x + 10, 9, 0).setUv(1, 1).setColor(-1);
				builder.addVertex(matrix4f, x + 10, -1, 0).setUv(1, 0).setColor(-1);
				Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
			}
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V", ordinal = 1), index = 4)
	public boolean axolotlclient$enableShadows(boolean shadow) {
		return AxolotlClient.config().useShadows.get();
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V"), index = 8)
	public int axolotlclient$bgColor(int color) {
		if (AxolotlClient.config().nametagBackground.get()) {
			return color;
		} else {
			return 0;
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V"))
	private void applyLevelHeadOptions(Font instance, Component text, float x, float y, int color, boolean drawShadow, Matrix4f pose, MultiBufferSource bufferSource, Font.DisplayMode mode, int backgroundColor, int packedLightCoords, Operation<Void> original, @Local SubmitNodeStorage.NameTagSubmit submit) {
		if (((NameTagSubmitExtension) (Object) submit).axolotlclient$isForLevelHead()) {
			color = ARGB.color(ARGB.alpha(color), LevelHead.getInstance().textColor.get().toInt());
			if (backgroundColor != 0 && !LevelHead.getInstance().background.get()) {
				backgroundColor = 0;
			}
		}
		original.call(instance, text, x, y, color, drawShadow, pose, bufferSource, mode, backgroundColor, packedLightCoords);
	}
}
