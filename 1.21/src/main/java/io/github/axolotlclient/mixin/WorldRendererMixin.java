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

import java.util.OptionalDouble;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * <p>License: MIT</p>
 **/

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Shadow
	private @Nullable ClientWorld world;

	@ModifyArgs(method = "drawBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawShapeOutline(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/util/shape/VoxelShape;DDDFFFF)V"))
	private void axolotlclient$customOutlineColor(Args args) {
		if (AxolotlClient.config().enableCustomOutlines.get()) {
			int color = AxolotlClient.config().outlineColor.get().toInt();
			float a = (float) (color >> 24 & 0xFF) / 255.0F;
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			args.set(6, r);
			args.set(7, g);
			args.set(8, b);
			args.set(9, a);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
	private void renderBlockOutline(WorldRenderer instance, MatrixStack matrices, VertexConsumer consumer, Entity entity, double offsetX, double offsetY, double offsetZ, BlockPos blockPos, BlockState blockState, Operation<Void> original, @Local VertexConsumerProvider.Immediate provider) {
		if (AxolotlClient.config().enableCustomOutlines.get()) {
			provider.draw(RenderLayer.LINES);
		}
		original.call(instance, matrices, consumer, entity, offsetX, offsetY, offsetZ, blockPos, blockState);
		if (AxolotlClient.config().enableCustomOutlines.get()) {
			Util.lineWidthModifier = OptionalDouble.of(AxolotlClient.config().outlineWidth.get());
			provider.draw(RenderLayer.LINES);
			Util.lineWidthModifier = OptionalDouble.empty();
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getLines()Lnet/minecraft/client/render/RenderLayer;", ordinal = 0))
	private void renderOutlineFill(DeltaTracker tracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local VertexConsumerProvider.Immediate immediate, @Local MatrixStack stack, @Local BlockPos pos, @Local BlockState state) {
		DrawUtil.drawOutlines(immediate, camera.getFocusedEntity(), camera.getPos(), stack, pos, state, world);
	}

	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$changeWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (AxolotlClient.config().noRain.get()) {
			ci.cancel();
		}
	}
}
