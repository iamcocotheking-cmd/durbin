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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hypixel.LevelHead;
import io.github.axolotlclient.modules.hypixel.NickHider;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.duck.SubmitNodeCollectorExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class EntityRendererMixin {

	@WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
	private void axolotlclient$modifiyName(SubmitNodeCollector instance, PoseStack poseStack, Vec3 vec3, int a, Component component, boolean b, int i, double v, CameraRenderState cameraRenderState, Operation<Void> original, @Local(argsOnly = true) AvatarRenderState state) {
		if (AxolotlClient.config() != null) {
			var mc = Minecraft.getInstance();
			if (mc.player != null) {
				Level level = Minecraft.getInstance().level;
				if (level == null) return;
				Entity player = level.getEntity(state.id);
				if (player == null)
					return; // some mods seem to create players without adding them to a level for gui rendering. why?
				boolean self = player.getUUID() == mc.player.getUUID();
				if (self && NickHider.getInstance().hideOwnName.get()) {
					component = (Component) NickHider.getInstance().editComponent(component, player.getName().getString(), NickHider.getInstance().hiddenNameSelf.get());
				} else if (!self && NickHider.getInstance().hideOtherNames.get()) {
					component = (Component) NickHider.getInstance().editComponent(component, player.getName().getString(), NickHider.getInstance().hiddenNameOthers.get());
				}
			}
		}
		original.call(instance, poseStack, vec3, a, component, b, i, v, cameraRenderState);
	}

	@Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 1, shift = At.Shift.AFTER))
	public void axolotlclient$addBadges(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (state.isDiscrete || !AxolotlClient.config().showBadges.get() || Minecraft.getInstance().level == null) {
			return;
		}

		Entity entity = Minecraft.getInstance().level.getEntity(state.id);
		if (entity instanceof Player) {
			// Durbin: show the local badge image beside normal player nametags.
			((SubmitNodeCollectorExtension) submitNodeCollector).axolotlclient$lastNameTagSubmitHasBadge();
		}
	}

	@Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void addLevel(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (Minecraft.getInstance().level == null) {
			return;
		}
		var entity = Minecraft.getInstance().level.getEntity(state.id);
		if (!(entity instanceof Player player)) {
			return;
		}

		if (Minecraft.getInstance().getCurrentServer() != null && Minecraft.getInstance().getCurrentServer().ip.endsWith("hypixel.net")) {
			if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().bedwarsLevelHead.get()) {
				String text = BedwarsMod.getInstance().getGame().get().getLevelHead(player);
				if (text != null) {
					var y = state.showExtraEars ? -20 : -10;

					if (LevelHead.getInstance().background.get()) {
						y -= 2;
					}

					submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, y, Component.literal(text).withStyle(s -> s.withColor(LevelHead.getInstance().textColor.get().toInt())), !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, cameraRenderState);
					((SubmitNodeCollectorExtension) submitNodeCollector).axolotlclient$lastNameTagSubmitIsLevelHead();
				}
			} else if (LevelHead.getInstance().enabled.get()) {
				String text = LevelHead.getInstance().getDisplayString(player.getStringUUID());

				var y = state.showExtraEars ? -20 : -10;

				if (LevelHead.getInstance().background.get()) {
					y -= 2;
				}

				submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, y, Component.literal(text).withStyle(s -> s.withColor(LevelHead.getInstance().textColor.get().toInt())), !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, cameraRenderState);
				((SubmitNodeCollectorExtension) submitNodeCollector).axolotlclient$lastNameTagSubmitIsLevelHead();
			}
		}

	}
}
