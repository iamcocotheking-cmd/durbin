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

package io.github.axolotlclient.modules.hud.gui.hud;

import com.mojang.blaze3d.lighting.DiffuseLighting;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.events.types.PlayerDirectionChangeEvent;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class PlayerHud extends PlayerHudCommon {
	@Getter
	private static boolean currentlyRendering;

	public PlayerHud() {
		super();
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void onPlayerDirectionChange(PlayerDirectionChangeEvent event) {
		yawOffset += (event.yaw() - event.prevYaw()) / 2;
	}

	@Override
	public void tick() {
		super.tick();
		var client = MinecraftClient.getInstance();
		if (client.player != null && client.player.isInSwimmingPose()) {
			float rawPitch = client.player.isTouchingWater() ? -90.0F - client.player.getPitch() : -90.0F;
			float pitch = MathHelper.lerp(client.player.getLeaningPitch(1), 0.0F, rawPitch);
			float height = client.player.getHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height);
			yOffset = Math.abs(offset) + 35;
		} else if (client.player != null && client.player.isFallFlying()) {
			// Elytra!

			float j = (float) client.player.getRoll() + 1;
			float k = MathHelper.clamp(j * j / 100.0F, 0.0F, 1.0F);

			float pitch = k * (-90.0F - client.player.getPitch()) + 90;
			float height = client.player.getHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height) * 50;
			yOffset = 35 - offset;
			if (pitch < 0) {
				yOffset -= (float) (((1 / (1 + Math.exp(-pitch / 4))) - .5) * 20);
			}
		} else {
			yOffset *= .8f;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderPlayer(AxoRenderContext ctx, boolean placeholder, double x, double y, float delta) {
		var client = MinecraftClient.getInstance();
		var graphics = (GuiGraphics) ctx;
		if (client.player == null) {
			return;
		}

		float lerpY = (lastYOffset + ((yOffset - lastYOffset) * delta));

		Quaternionf quaternion = Axis.Z_POSITIVE.rotationDegrees(180.0F);

		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.headYaw;
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}
		Quaternionf quaternionf2 = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), deltaYaw - 180 + rotation.get().floatValue());
		quaternion.mul(quaternionf2);

		// Save these to set them back later
		float pastYaw = client.player.getYaw();
		float pastPrevYaw = client.player.prevYaw;
		currentlyRendering = true;
		float x1 = ((float) (x / getScale() + getContentWidth() / 2f));
		float y1 = ((float) (y / getScale() + getContentHeight() * 0.925f - lerpY));
		Vector3f offset = new Vector3f();
		var pose = graphics.getMatrices();
		pose.push();
		pose.translate(x1, y1, 50.0);
		pose.scale((float) 40, (float) 40, -(float) 40);
		pose.translate(offset.x, offset.y, offset.z);
		pose.rotate(quaternion);
		DiffuseLighting.setupInventoryShaderLighting();
		EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		if (quaternionf2 != null) {
			entityRenderDispatcher.setRotation(quaternionf2.conjugate(new Quaternionf()).rotateY((float) Math.PI));
		}

		entityRenderDispatcher.setRenderShadows(false);
		RenderSystem.runAsFancy(
			() -> entityRenderDispatcher.render(client.player, 0.0, 0.0, 0.0, 0.0F, delta, pose, graphics.getVertexConsumers(), 15728880)
		);
		graphics.draw();
		entityRenderDispatcher.setRenderShadows(true);
		pose.pop();
		DiffuseLighting.setup3DGuiLighting();
		currentlyRendering = false;

		client.player.setYaw(pastYaw);
		client.player.prevYaw = pastPrevYaw;
	}

	@Override
	protected boolean isPerformingAction() {
		// inspired by tr7zw's mod
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		//noinspection DataFlowIssue
		return player.isSneaking() || player.isSprinting() || player.isFallFlying() || player.getAbilities().flying
			|| player.isSubmergedInWater() || player.isInSwimmingPose() || player.hasVehicle()
			|| player.isUsingItem() || player.handSwinging || player.hurtTime > 0 || player.isOnFire();
	}
}
