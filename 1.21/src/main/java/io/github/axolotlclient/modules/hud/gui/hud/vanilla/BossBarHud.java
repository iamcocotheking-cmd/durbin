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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.mixin.BossBarHudAccessor;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.boss_bar.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class BossBarHud extends TextHudEntry {

	public static final Identifier ID = Identifier.of("kronhud", "bossbarhud");
	private static final Identifier[] BAR_BACKGROUND_SPRITES =
		new Identifier[]{Identifier.ofDefault("boss_bar/pink_background"),
			Identifier.ofDefault("boss_bar/blue_background"),
			Identifier.ofDefault("boss_bar/red_background"),
			Identifier.ofDefault("boss_bar/green_background"),
			Identifier.ofDefault("boss_bar/yellow_background"),
			Identifier.ofDefault("boss_bar/purple_background"),
			Identifier.ofDefault("boss_bar/white_background")};
	private static final Identifier[] BAR_PROGRESS_SPRITES =
		new Identifier[]{Identifier.ofDefault("boss_bar/pink_progress"),
			Identifier.ofDefault("boss_bar/blue_progress"),
			Identifier.ofDefault("boss_bar/red_progress"),
			Identifier.ofDefault("boss_bar/green_progress"),
			Identifier.ofDefault("boss_bar/yellow_progress"),
			Identifier.ofDefault("boss_bar/purple_progress"),
			Identifier.ofDefault("boss_bar/white_progress")};
	private static final Identifier[] OVERLAY_BACKGROUND_SPRITES =
		new Identifier[]{Identifier.ofDefault("boss_bar/notched_6_background"),
			Identifier.ofDefault("boss_bar/notched_10_background"),
			Identifier.ofDefault("boss_bar/notched_12_background"),
			Identifier.ofDefault("boss_bar/notched_20_background")};
	private static final Identifier[] OVERLAY_PROGRESS_SPRITES =
		new Identifier[]{Identifier.ofDefault("boss_bar/notched_6_progress"),
			Identifier.ofDefault("boss_bar/notched_10_progress"),
			Identifier.ofDefault("boss_bar/notched_12_progress"),
			Identifier.ofDefault("boss_bar/notched_20_progress")};
	private final BossBar placeholder = new CustomBossBar(Text.literal("Boss bar"), BossBar.Color.WHITE,
		BossBar.Style.PROGRESS);
	private final BossBar placeholder2 = Util.make(() -> {
		BossBar boss = new CustomBossBar(Text.literal("More boss bars..."), BossBar.Color.PURPLE,
			BossBar.Style.PROGRESS);
		boss.setPercent(0.45F);
		return boss;
	});
	private final BooleanOption text = new BooleanOption("text", true);
	private final BooleanOption bar = new BooleanOption("bar", true);
	// TODO custom color
	private Map<UUID, ClientBossBar> bossBars = new HashMap<>();
	private final MinecraftClient client = (MinecraftClient) super.client;

	public BossBarHud() {
		super(184, 80, false);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		setBossBars();
		if (bossBars == null || this.bossBars.isEmpty()) {
			return;
		}
		DrawPosition scaledPos = getContentPos();
		int by = 12;
		for (ClientBossBar bossBar : bossBars.values()) {
			renderBossBar((GuiGraphics) graphics, scaledPos.x() + 1, by + scaledPos.y(), bossBar);
			by = by + 19;
			if (by > getContentHeight()) {
				break;
			}
		}
	}

	public void setBossBars() {
		bossBars = ((BossBarHudAccessor) client.inGameHud.getBossBarHud()).axolotlclient$getBossBars();
		if (bossBars != null) {
			if (bossBars.isEmpty()) {
				// Just leave it alone, it's not rendering anyway
				return;
			}
			var h = Math.min(bossBars.size() * 19, (int) (AxoWindow.getWindow().br$getScaledHeight() / 3d));
			if (h != getContentHeight()) {
				// Update height
				setContentHeight(Math.min(bossBars.size() * 19, (int) (AxoWindow.getWindow().br$getScaledHeight() / 3d)));
				onBoundsUpdate();
			}
		}
	}

	private void renderBossBar(GuiGraphics graphics, int x, int y, BossBar bossBar) {
		if (bar.get()) {
			this.draw(graphics, x, y, bossBar, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
			int i = MathHelper.method_53063(bossBar.getPercent(), 0, 182);
			if (i > 0) {
				this.draw(graphics, x, y, bossBar, i, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
			}
		}
		if (text.get()) {
			Text text = bossBar.getName();
			float textX = x + ((float) getContentWidth() / 2) - ((float) client.textRenderer.getWidth(text) / 2);
			float textY = y - 9;
			graphics.drawText(client.textRenderer, text, (int) textX, (int) textY, textColor.get().toInt(), shadow.get());
		}
	}

	private void draw(GuiGraphics graphics, int x, int y, BossBar bar, int width, Identifier[] textures, Identifier[] alternativeTextures) {
		RenderSystem.enableBlend();
		graphics.drawGuiTexture(textures[bar.getColor().ordinal()], 182, 5, 0, 0, x, y, width, 5);
		if (bar.getStyle() != BossBar.Style.PROGRESS) {
			graphics.drawGuiTexture(alternativeTextures[bar.getStyle().ordinal() - 1], 182, 5, 0, 0, x, y, width, 5);
		}

		RenderSystem.disableBlend();
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		var height = 2 * 19;
		if (height != getContentHeight()) {
			setContentHeight(height);
			onBoundsUpdate();
		}
		DrawPosition pos = getContentPos();
		renderBossBar((GuiGraphics) graphics, pos.x() + 1, pos.y() + 12, placeholder);
		renderBossBar((GuiGraphics) graphics, pos.x() + 1, pos.y() + 31, placeholder2);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(hide);
		options.add(text);
		options.add(bar);
		return options;
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_MIDDLE;
	}

	public static class CustomBossBar extends BossBar {

		public CustomBossBar(Text name, Color color, Style style) {
			super(MathHelper.randomUuid(), name, color, style);
		}
	}
}
