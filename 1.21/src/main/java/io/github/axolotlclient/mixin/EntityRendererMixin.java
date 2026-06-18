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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.LevelHead;
import io.github.axolotlclient.modules.hypixel.NickHider;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
		ordinal = 0))
	public void axolotlclient$addBadges(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity && text.equals(entity.getDisplayName())) {
			if (!entity.isSneaky()) {
				if (AxolotlClient.config().showBadges.get() && UserRequest.getOnline(entity.getUuid().toString())) {
					RenderSystem.enableDepthTest();

					assert MinecraftClient.getInstance().player != null;
					int x = -(MinecraftClient.getInstance().textRenderer.getWidth(
						entity.getUuid() == MinecraftClient.getInstance().player.getUuid() ? (
							NickHider.getInstance().hideOwnName.get() ? NickHider.getInstance().hiddenNameSelf.get()
								: Team.decorateName(entity.getScoreboardTeam(),
								entity.getName()
							).getString()) : (
							NickHider.getInstance().hideOtherNames.get()
								? NickHider.getInstance().hiddenNameOthers.get()
								: Team.decorateName(entity.getScoreboardTeam(), entity.getName()).getString())) / 2 +
						(AxolotlClient.config().customBadge.get()
							? MinecraftClient.getInstance().textRenderer.getWidth(
							" " + Formatting.strip(AxolotlClient.config().badgeText.get())) : 10));

					RenderSystem.setShaderColor(1, 1, 1, 1);

					if (AxolotlClient.config().customBadge.get()) {
						Text badgeText = Util.formatFromCodes(AxolotlClient.config().badgeText.get());
						MinecraftClient.getInstance().textRenderer.draw(badgeText, x + 6, 0, -1,
							AxolotlClient.config().useShadows.get(),
							matrices.peek().getModel(), vertexConsumers,
							TextRenderer.TextLayerType.NORMAL, 0, 15728880
						);
					} else {
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderTexture(0, (Identifier) AxolotlClientCommon.BADGE_PATH);
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

						matrices.push();
						Matrix4f matrix4f = matrices.peek().getModel();
						builder.xyz(matrix4f, x, 0, 0).uv0(0, 0);
						builder.xyz(matrix4f, x, 8, 0).uv0(0, 1);
						builder.xyz(matrix4f, x + 8, 8, 0).uv0(1, 1);
						builder.xyz(matrix4f, x + 8, 0, 0).uv0(1, 0);
						BufferRenderer.drawWithShader(builder.endOrThrow());
						matrices.pop();
					}
					RenderSystem.disableDepthTest();
				}
			}
		}
	}

	@ModifyArg(method = "renderLabelIfPresent", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 0),
		index = 8)
	public int axolotlclient$bgColor(int color) {
		if (AxolotlClient.config().nametagBackground.get()) {
			return color;
		} else {
			return 0;
		}
	}

	@ModifyArg(method = "renderLabelIfPresent", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 1),
		index = 4)
	public boolean axolotlclient$enableShadows(boolean shadow) {
		return AxolotlClient.config().useShadows.get();
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
		ordinal = 1))
	public void axolotlclient$addLevel(Entity entity, Text string, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci, @Local(ordinal = 2) int bgColor) {
		if (entity instanceof AbstractClientPlayerEntity && string.equals(entity.getDisplayName())) {
			if (MinecraftClient.getInstance().getCurrentServerEntry() != null &&
				MinecraftClient.getInstance().getCurrentServerEntry().address.contains("hypixel.net")) {
				TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
				if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() &&
					BedwarsMod.getInstance().bedwarsLevelHead.get()) {
					String text =
						BedwarsMod.getInstance().getGame().get().getLevelHead((AbstractClientPlayerEntity) entity);
					if (text != null) {
						float x = -textRenderer.getWidth(text) / 2F;
						float y = string.getString().contains("deadmau5") ? -20 : -10;

						if (LevelHead.getInstance().background.get()) {
							y -= 2;
						}

						Matrix4f matrix4f = matrices.peek().getModel();
						MinecraftClient.getInstance().textRenderer.draw(text, x, y,
							ClientColors.ARGB.color(0x20, LevelHead.getInstance().textColor.get().toInt()),
							false, matrix4f,
							vertexConsumers,
							TextRenderer.TextLayerType.SEE_THROUGH,
							LevelHead.getInstance().background.get() ? bgColor
								: 0,
							light
						);
						MinecraftClient.getInstance().textRenderer.draw(text, x, y,
							LevelHead.getInstance().textColor.get().toInt(),
							AxolotlClient.config().useShadows.get(), matrix4f,
							vertexConsumers,
							TextRenderer.TextLayerType.NORMAL,
							0,
							light
						);
					}
				} else if (LevelHead.getInstance().enabled.get()) {
					String text = LevelHead.getInstance().getDisplayString(entity.getUuid().toString());

					float x = -textRenderer.getWidth(text) / 2F;
					float y = string.getString().contains("deadmau5") ? -20 : -10;

					if (LevelHead.getInstance().background.get()) {
						y -= 2;
					}

					Matrix4f matrix4f = matrices.peek().getModel();
					MinecraftClient.getInstance().textRenderer.draw(text, x, y,
						ClientColors.ARGB.color(0x20, LevelHead.getInstance().textColor.get().toInt()),
						false, matrix4f,
						vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH,
						LevelHead.getInstance().background.get() ? bgColor : 0,
						light
					);
					MinecraftClient.getInstance().textRenderer.draw(text, x, y,
						LevelHead.getInstance().textColor.get().toInt(),
						AxolotlClient.config().useShadows.get(), matrix4f,
						vertexConsumers, TextRenderer.TextLayerType.NORMAL,
						0,
						light
					);
				}
			}
		}
	}
}
