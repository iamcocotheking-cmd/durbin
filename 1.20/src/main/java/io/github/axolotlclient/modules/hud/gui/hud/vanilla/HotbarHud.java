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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class HotbarHud extends TextHudEntry {

	public static final Identifier ID = new Identifier(AxolotlClientCommon.MODID, "hotbarhud");
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
	public static final Identifier ICONS_TEXTURE = new Identifier("textures/gui/icons.png");

	private final MinecraftClient client = (MinecraftClient) super.client;

	public HotbarHud() {
		super(182, 22, false);
		supportsScaling = false;
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		graphics.br$pushMatrix();
		PlayerEntity playerEntity = MinecraftClient.getInstance().cameraEntity instanceof PlayerEntity
			? (PlayerEntity) MinecraftClient.getInstance().cameraEntity
			: null;
		if (playerEntity != null) {
			ItemStack itemStack = playerEntity.getOffHandStack();
			Arm arm = playerEntity.getMainArm().getOpposite();
			DrawPosition pos = getPos();
			int i = pos.x() + getWidth() / 2;
			graphics.br$pushMatrix();
			((GuiGraphics) graphics).getMatrices().translate(0.0F, 0.0F, -90.0F);
			((GuiGraphics) graphics).drawTexture(WIDGETS_TEXTURE, i - 91, pos.y(), 0, 0, 182, 22);
			((GuiGraphics) graphics).drawTexture(WIDGETS_TEXTURE, i - 91 - 1 + playerEntity.getInventory().selectedSlot * 20, pos.y() - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (arm == Arm.LEFT) {
					((GuiGraphics) graphics).drawTexture(WIDGETS_TEXTURE, i - 91 - 29, pos.y() - 1, 24, 22, 29, 24);
				} else {
					((GuiGraphics) graphics).drawTexture(WIDGETS_TEXTURE, i + 91, pos.y() - 1, 53, 22, 29, 24);
				}
			}

			graphics.br$popMatrix();
			int l = 1;

			for (int m = 0; m < 9; ++m) {
				int n = i - 90 + m * 20 + 2;
				int o = pos.y() + 6 - 3;
				this.renderHotbarItem(graphics, n, o, delta, playerEntity, playerEntity.getInventory().main.get(m), l++);
			}

			if (!itemStack.isEmpty()) {
				int m = pos.y() + 6 - 3;
				if (arm == Arm.LEFT) {
					this.renderHotbarItem(graphics, i - 91 - 26, m, delta, playerEntity, itemStack, l);
				} else {
					this.renderHotbarItem(graphics, i + 91 + 10, m, delta, playerEntity, itemStack, l);
				}
			}

			RenderSystem.enableBlend();
			if (this.client.options.getAttackIndicator().get() == AttackIndicator.HOTBAR) {
				assert this.client.player != null;
				float f = this.client.player.getAttackCooldownProgress(0.0F);
				if (f < 1.0F) {
					int n = pos.y() + 2;
					int o = i + 91 + 6;
					if (arm == Arm.RIGHT) {
						o = i - 91 - 22;
					}

					int p = (int) (f * 19.0F);
					((GuiGraphics) graphics).drawTexture(ICONS_TEXTURE, o, n, 0, 94, 18, 18);
					((GuiGraphics) graphics).drawTexture(ICONS_TEXTURE, o, n + 18 - p, 18, 112 - p, 18, p);
				}
			}

			RenderSystem.disableBlend();
		}
		graphics.br$popMatrix();
	}

	private void renderHotbarItem(AxoRenderContext graphics, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) {
		if (!stack.isEmpty()) {
			float f = (float) stack.getCooldown() - tickDelta;
			if (f > 0.0F) {
				float g = 1.0F + f / 5.0F;
				graphics.br$pushMatrix();
				graphics.br$translateMatrix((float) (x + 8), (float) (y + 12));
				graphics.br$scaleMatrix(1.0F / g, (g + 1.0F) / 2.0F);
				graphics.br$translateMatrix((float) (-(x + 8)), (float) (-(y + 12)));
			}

			((GuiGraphics) graphics).drawItem(player, stack, x, y, seed);
			if (f > 0.0F) {
				graphics.br$popMatrix();
			}

			((GuiGraphics) graphics).drawItemInSlot(this.client.textRenderer, stack, x, y);
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		DrawPosition pos = getPos();

		DrawUtil.drawCenteredString((GuiGraphics) graphics, MinecraftClient.getInstance().textRenderer, getName(), pos.x() + width / 2,
			pos.y() + height / 2 - 4, -1, true);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public boolean overridesF3() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> list = new ArrayList<>();
		list.add(enabled);
		list.add(hide);
		list.add(anchor);
		return list;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.BOTTOM_MIDDLE;
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.96;
	}
}
