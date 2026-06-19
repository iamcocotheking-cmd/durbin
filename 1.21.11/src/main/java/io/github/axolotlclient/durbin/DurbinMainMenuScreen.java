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

package io.github.axolotlclient.durbin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.net.URI;

public class DurbinMainMenuScreen extends Screen {
	private static final Identifier LOGO = id("logo_main.png");
	private static final Identifier SINGLE = id("button_singleplayer.png");
	private static final Identifier MULTI = id("button_multiplayer.png");
	private static final Identifier SETTINGS = id("icon_settings.png");
	private static final Identifier EXIT = id("icon_exit.png");
	private static final Identifier DISCORD = id("icon_discord.png");
	private static final Identifier LANGUAGE = id("icon_language.png");
	private static final Identifier CLOSE = id("icon_close.png");
	private static final Identifier ACCOUNT = id("icon_account.png");
	private static final Identifier BACKGROUND = id("background_main.png");

	private int singleX, singleY, singleW, singleH;
	private int multiX, multiY, multiW, multiH;
	private int settingsX, exitX, discordX, langX, iconY, iconSize;
	private int closeX, closeY, closeSize;
	private int accountX, accountY, accountSize;
	private final long openedAt = System.currentTimeMillis();

	public DurbinMainMenuScreen() {
		super(Component.literal("Durbin Client Main Menu"));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("axolotlclient", "textures/durbin/mainmenu/" + path);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private float openProgress() {
		return smooth(Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 650.0F));
	}

	private float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	@Override
	public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float delta) {
		super.render(g, mouseX, mouseY, delta);
		layout();

		float anim = openProgress();
		int introRise = (int) ((1.0F - anim) * 22.0F);

		// Use the provided background image directly on the main menu in full quality.
		drawCustomBackground(g);

		int logoW = Math.max(110, Math.min(160, this.width / 5));
		int logoH = logoW * 236 / 376;
		int logoX = (this.width - logoW) / 2;
		int logoY = Math.max(22, this.height / 2 - 130);
		drawScaledTexture(g, LOGO, logoX, logoY, logoW, logoH, 376, 236);

		drawImageButton(g, SINGLE, singleX, singleY + introRise, singleW, singleH, 784, 64, inside(mouseX, mouseY, singleX, singleY, singleW, singleH));
		drawImageButton(g, MULTI, multiX, multiY + introRise, multiW, multiH, 784, 64, inside(mouseX, mouseY, multiX, multiY, multiW, multiH));

		int iconSlide = (int) ((1.0F - anim) * 16.0F);
		drawIconButton(g, SETTINGS, settingsX, iconY + iconSlide, iconSize, mouseX, mouseY);
		drawIconButton(g, EXIT, exitX, iconY + iconSlide, iconSize, mouseX, mouseY);
		drawIconButton(g, DISCORD, discordX, iconY + iconSlide, iconSize, mouseX, mouseY);
		drawIconButton(g, LANGUAGE, langX, iconY + iconSlide, iconSize, mouseX, mouseY);
		drawIconButton(g, ACCOUNT, accountX, accountY, accountSize, mouseX, mouseY);
		drawIconButton(g, CLOSE, closeX, closeY, closeSize, mouseX, mouseY);

		drawTiny(g, "PortalBD: play.portalbd.fun", (this.width - 92) / 2, multiY + multiH + 13 + introRise, 0x99FFFFFF);

		if (anim < 1.0F) {
			g.fill(0, 0, this.width, this.height, argb((int) ((1.0F - anim) * 120.0F), 0, 0, 0));
		}

		drawTiny(g, "Durbin Client 1.21.11", 6, this.height - 10, 0x66FFFFFF);
		drawTiny(g, "Copyright COSA", this.width - 62, this.height - 10, 0x66FFFFFF);
	}

	private void layout() {
		this.singleW = Math.max(170, Math.min(245, this.width / 3));
		this.singleH = Math.max(14, singleW * 64 / 784);
		this.multiW = singleW;
		this.multiH = singleH;
		this.singleX = (this.width - singleW) / 2;
		this.singleY = this.height / 2 - 7;
		this.multiX = singleX;
		this.multiY = singleY + singleH + 8;

		this.iconSize = Math.max(18, Math.min(24, this.width / 44));
		int gap = Math.max(8, iconSize / 3);
		int total = iconSize * 4 + gap * 3;
		this.iconY = this.height - Math.max(44, this.height / 12);
		int startX = (this.width - total) / 2;
		this.settingsX = startX;
		this.exitX = startX + iconSize + gap;
		this.discordX = startX + (iconSize + gap) * 2;
		this.langX = startX + (iconSize + gap) * 3;

		this.closeSize = Math.max(18, Math.min(28, this.width / 45));
		this.closeX = this.width - closeSize - 12;
		this.closeY = 12;
		this.accountSize = closeSize;
		this.accountX = 12;
		this.accountY = 12;
	}

	private void drawCustomBackground(GuiGraphics g) {
		int texW = 1920;
		int texH = 1080;
		float scale = Math.max((float) this.width / (float) texW, (float) this.height / (float) texH);
		int drawW = Math.round(texW * scale);
		int drawH = Math.round(texH * scale);
		int x = (this.width - drawW) / 2;
		int y = (this.height - drawH) / 2;
		drawScaledTexture(g, BACKGROUND, x, y, drawW, drawH, texW, texH);

		// Small dark overlay keeps Durbin buttons readable while preserving the uploaded background image.
		g.fill(0, 0, this.width, this.height, argb(72, 0, 0, 0));
		g.fill(0, 0, this.width, 58, argb(42, 0, 0, 0));
		g.fill(0, this.height - 76, this.width, this.height, argb(42, 0, 0, 0));
	}

	private void drawImageButton(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int tw, int th, boolean hover) {
		int grow = hover ? 3 : 0;
		drawScaledTexture(g, tex, x - grow, y - grow / 2, w + grow * 2, h + grow, tw, th);
		if (hover) {
			g.fill(x - grow, y - grow / 2, x + w + grow, y + h + grow / 2, argb(30, 255, 255, 255));
		}
	}

	private void drawIconButton(GuiGraphics g, Identifier tex, int x, int y, int size, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, size, size);
		int drawSize = hover ? size + 2 : size;
		int drawX = hover ? x - 1 : x;
		int drawY = hover ? y - 1 : y;
		drawScaledTexture(g, tex, drawX, drawY, drawSize, drawSize, 64, 64);
	}

	private void drawScaledTexture(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int tw, int th) {
		float sx = (float) w / (float) tw;
		float sy = (float) h / (float) th;
		g.pose().pushMatrix();
		g.pose().scale(sx, sy);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, Math.round(x / sx), Math.round(y / sy), 0, 0, tw, th, tw, th);
		g.pose().popMatrix();
	}

	private void drawTiny(GuiGraphics g, String text, int x, int y, int color) {
		g.pose().pushMatrix();
		g.pose().scale(0.5F, 0.5F);
		g.drawString(font, text, Math.round(x * 2F), Math.round(y * 2F), color, false);
		g.pose().popMatrix();
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (inside(mouseX, mouseY, singleX, singleY, singleW, singleH)) {
			playClick();
			minecraft.setScreen(new SelectWorldScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, multiX, multiY, multiW, multiH)) {
			playClick();
			DurbinPortalBDServer.ensure(minecraft);
			minecraft.setScreen(new JoinMultiplayerScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, settingsX, iconY, iconSize, iconSize)) {
			playClick();
			minecraft.setScreen(new OptionsScreen(this, minecraft.options));
			return true;
		}
		if (inside(mouseX, mouseY, exitX, iconY, iconSize, iconSize) || inside(mouseX, mouseY, closeX, closeY, closeSize, closeSize)) {
			playClick();
			minecraft.stop();
			return true;
		}
		if (inside(mouseX, mouseY, discordX, iconY, iconSize, iconSize)) {
			playClick();
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, langX, iconY, iconSize, iconSize)) {
			playClick();
			minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager()));
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}

	private int argb(int a, int r, int g, int b) {
		return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

	private void playClick() {
		try {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		} catch (Exception ignored) {
		}
	}

	private void openUrl(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(URI.create(url));
			}
		} catch (Exception ignored) {
		}
	}
}
