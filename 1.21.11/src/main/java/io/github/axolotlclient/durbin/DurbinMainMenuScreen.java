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
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.net.URI;

public class DurbinMainMenuScreen extends Screen {
	private static final Identifier BG = id("panorama_0.png");
	private static final Identifier LOGO = id("logo_main.png");
	private static final Identifier SINGLE = id("button_singleplayer.png");
	private static final Identifier MULTI = id("button_multiplayer.png");
	private static final Identifier SETTINGS = id("icon_settings.png");
	private static final Identifier EXIT = id("icon_exit.png");
	private static final Identifier DISCORD = id("icon_discord.png");
	private static final Identifier LANGUAGE = id("icon_language.png");
	private static final Identifier CLOSE = id("icon_close.png");
	private static final Identifier ACCOUNT = id("icon_account.png");
	private static final Identifier PROMO = id("promo_card.png");

	private int singleX, singleY, singleW, singleH;
	private int multiX, multiY, multiW, multiH;
	private int settingsX, exitX, discordX, langX, iconY, iconSize;
	private int closeX, closeY, closeSize;
	private int accountX, accountY, accountSize;
	private int promoX, promoY, promoW, promoH;

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

	@Override
	public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float delta) {
		super.render(g, mouseX, mouseY, delta);
		layout();

		drawCover(g, BG, 0, 0, this.width, this.height, 1024, 1024);
		g.fill(0, 0, this.width, this.height, argb(108, 0, 0, 0));
		g.fill(0, 0, this.width, 55, argb(35, 0, 0, 0));
		g.fill(0, this.height - 70, this.width, this.height, argb(65, 0, 0, 0));

		int logoW = Math.max(110, Math.min(160, this.width / 5));
		int logoH = logoW * 236 / 376;
		int logoX = (this.width - logoW) / 2;
		int logoY = Math.max(22, this.height / 2 - 130);
		g.blit(RenderPipelines.GUI_TEXTURED, LOGO, logoX, logoY, 0, 0, logoW, logoH, 376, 236);

		drawImageButton(g, SINGLE, singleX, singleY, singleW, singleH, 784, 64, inside(mouseX, mouseY, singleX, singleY, singleW, singleH));
		drawImageButton(g, MULTI, multiX, multiY, multiW, multiH, 784, 64, inside(mouseX, mouseY, multiX, multiY, multiW, multiH));

		drawIconButton(g, SETTINGS, settingsX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, EXIT, exitX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, DISCORD, discordX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, LANGUAGE, langX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, ACCOUNT, accountX, accountY, accountSize, mouseX, mouseY);
		drawIconButton(g, CLOSE, closeX, closeY, closeSize, mouseX, mouseY);

		g.blit(RenderPipelines.GUI_TEXTURED, PROMO, promoX, promoY, 0, 0, promoW, promoH, 382, 178);

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

		this.iconSize = Math.max(22, Math.min(32, this.width / 32));
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

		this.promoW = Math.max(112, Math.min(182, this.width / 5));
		this.promoH = promoW * 178 / 382;
		this.promoX = this.width - promoW - 28;
		this.promoY = this.height - promoH - 46;
	}

	private void drawCover(GuiGraphics g, Identifier texture, int x, int y, int w, int h, int tw, int th) {
		float targetRatio = (float) w / (float) h;
		float sourceRatio = (float) tw / (float) th;
		int drawW;
		int drawH;
		if (sourceRatio > targetRatio) {
			drawH = h;
			drawW = Math.round(h * sourceRatio);
		} else {
			drawW = w;
			drawH = Math.round(w / sourceRatio);
		}
		int dx = x + (w - drawW) / 2;
		int dy = y + (h - drawH) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, texture, dx, dy, 0, 0, drawW, drawH, tw, th);
	}

	private void drawImageButton(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int tw, int th, boolean hover) {
		g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, w, h, tw, th);
		if (hover) {
			g.fill(x, y, x + w, y + h, argb(26, 255, 255, 255));
		}
	}

	private void drawIconButton(GuiGraphics g, Identifier tex, int x, int y, int size, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, size, size);
		g.fill(x, y, x + size, y + size, hover ? argb(120, 255, 255, 255) : argb(70, 255, 255, 255));
		int pad = Math.max(4, size / 5);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, x + pad, y + pad, 0, 0, size - pad * 2, size - pad * 2, 64, 64);
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
			minecraft.setScreen(new SelectWorldScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, multiX, multiY, multiW, multiH)) {
			minecraft.setScreen(new JoinMultiplayerScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, settingsX, iconY, iconSize, iconSize)) {
			minecraft.setScreen(new OptionsScreen(this, minecraft.options));
			return true;
		}
		if (inside(mouseX, mouseY, exitX, iconY, iconSize, iconSize) || inside(mouseX, mouseY, closeX, closeY, closeSize, closeSize)) {
			minecraft.stop();
			return true;
		}
		if (inside(mouseX, mouseY, discordX, iconY, iconSize, iconSize)) {
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, langX, iconY, iconSize, iconSize)) {
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

	private void openUrl(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(URI.create(url));
			}
		} catch (Exception ignored) {
		}
	}
}
