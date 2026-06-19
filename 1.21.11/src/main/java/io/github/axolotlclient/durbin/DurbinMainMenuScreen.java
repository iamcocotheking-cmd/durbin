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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DurbinMainMenuScreen extends Screen {
	private static final Identifier[] PANORAMA = {
		id("panorama_0.png"), id("panorama_1.png"), id("panorama_2.png"),
		id("panorama_3.png"), id("panorama_4.png"), id("panorama_5.png")
	};
	private static final Identifier LOGO = id("logo_main.png");
	private static final Identifier SETTINGS = id("icon_settings.png");
	private static final Identifier EXIT = id("icon_exit.png");
	private static final Identifier DISCORD = id("icon_discord.png");
	private static final Identifier YOUTUBE = id("icon_youtube.png");
	private static final Identifier LANGUAGE = id("icon_language.png");
	private static final Identifier CLOSE = id("icon_close.png");
	private static final Identifier ACCOUNT = id("icon_account.png");
	private static final String BUNDLED_PACK_PATH = "/assets/axolotlclient/resourcepacks/Durbin_Panorama_Pack.zip";
	private static final String PACK_FILE_NAME = "Durbin_Panorama_Pack.zip";

	private int panelX, panelY, panelW, panelH;
	private int singleX, singleY, multiY, optionsY, quitY, buttonW, buttonH;
	private int discordX, youtubeX, langX, iconY, iconSize;
	private int closeX, closeY, closeSize;
	private int accountX, accountY, accountSize;
	private final long openedAt = System.currentTimeMillis();
	private boolean packCopied;

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
		float raw = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 360.0F);
		return raw * raw * (3.0F - 2.0F * raw);
	}

	@Override
	public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float delta) {
		DurbinPanoramaPackInstaller.install();
		ensureDurbinPanoramaPackInstalled();
		layout();

		float anim = openProgress();
		int introRise = (int) ((1.0F - anim) * 12.0F);

		drawSmoothPanorama(g);
		g.fill(0, 0, this.width, this.height, argb(125, 0, 0, 0));
		g.fill(0, 0, this.width, 42, argb(40, 0, 0, 0));
		g.fill(0, this.height - 50, this.width, this.height, argb(55, 0, 0, 0));

		int drawPanelY = panelY + introRise;
		rect(g, panelX, drawPanelY, panelW, panelH, argb(110, 115, 128, 150), argb(175, 5, 8, 12));
		g.fill(panelX + 1, drawPanelY + 1, panelX + panelW - 1, drawPanelY + 34, argb(70, 255, 255, 255));

		int logoW = Math.max(126, Math.min(170, panelW - 54));
		int logoH = logoW * 236 / 376;
		int logoX = panelX + (panelW - logoW) / 2;
		int logoY = drawPanelY + 14;
		drawScaledTexture(g, LOGO, logoX, logoY, logoW, logoH, 376, 236);

		drawMenuButton(g, singleX, singleY + introRise, buttonW, buttonH, "Singleplayer", 0xFF80E070, mouseX, mouseY);
		drawMenuButton(g, singleX, multiY + introRise, buttonW, buttonH, "Multiplayer", 0xFF57C7FF, mouseX, mouseY);
		drawMenuButton(g, singleX, optionsY + introRise, buttonW, buttonH, "Options", 0xFFE4E8F0, mouseX, mouseY);
		drawMenuButton(g, singleX, quitY + introRise, buttonW, buttonH, "Quit Game", 0xFFFFB5B5, mouseX, mouseY);

		int portalY = drawPanelY + panelH - 35;
		g.fill(panelX + 18, portalY - 6, panelX + panelW - 18, portalY - 5, argb(55, 255, 255, 255));
		drawCentered(g, "PortalBD Quick Connect", panelX, portalY, panelW, 0xFFE9EEF6);
		drawCentered(g, "play.portalbd.fun", panelX, portalY + 12, panelW, 0xFF57C7FF);

		drawIconButton(g, DISCORD, discordX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, YOUTUBE, youtubeX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, LANGUAGE, langX, iconY, iconSize, mouseX, mouseY);
		drawIconButton(g, ACCOUNT, accountX, accountY, accountSize, mouseX, mouseY);
		drawIconButton(g, CLOSE, closeX, closeY, closeSize, mouseX, mouseY);

		if (anim < 1.0F) {
			g.fill(0, 0, this.width, this.height, argb((int) ((1.0F - anim) * 100.0F), 0, 0, 0));
		}

		drawTiny(g, "Durbin Client 1.21.11", 6, this.height - 10, 0x77FFFFFF);
		drawTiny(g, "COSA", this.width - 22, this.height - 10, 0x77FFFFFF);
	}

	private void layout() {
		this.panelW = Math.max(190, Math.min(260, this.width / 3));
		this.panelH = Math.max(210, Math.min(248, this.height - 46));
		this.panelX = (this.width - panelW) / 2;
		this.panelY = Math.max(12, (this.height - panelH) / 2 - 4);

		this.buttonW = panelW - 38;
		this.buttonH = 22;
		this.singleX = panelX + 19;
		this.singleY = panelY + 84;
		this.multiY = singleY + buttonH + 7;
		this.optionsY = multiY + buttonH + 7;
		this.quitY = optionsY + buttonH + 7;

		this.iconSize = Math.max(18, Math.min(24, this.width / 44));
		int gap = Math.max(8, iconSize / 2);
		int total = iconSize * 3 + gap * 2;
		this.iconY = this.height - Math.max(38, this.height / 13);
		int startX = (this.width - total) / 2;
		this.discordX = startX;
		this.youtubeX = startX + iconSize + gap;
		this.langX = startX + (iconSize + gap) * 2;

		this.closeSize = Math.max(18, Math.min(28, this.width / 45));
		this.closeX = this.width - closeSize - 12;
		this.closeY = 12;
		this.accountSize = closeSize;
		this.accountX = 12;
		this.accountY = 12;
	}

	private void drawSmoothPanorama(GuiGraphics g) {
		// Static high-resolution panorama face from the bundled Durbin panorama resource pack.
		// No movement here: this removes the lag/jitter while keeping the custom menu clean.
		drawCover(g, PANORAMA[0], 0, 0, this.width, this.height, 1024, 1024);
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
		drawScaledTexture(g, texture, dx, dy, drawW, drawH, tw, th);
	}

	private void drawMenuButton(GuiGraphics g, int x, int y, int w, int h, String text, int accent, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, hover ? argb(145, 100, 205, 255) : argb(95, 100, 115, 135), hover ? argb(115, 32, 43, 58) : argb(72, 12, 16, 23));
		g.fill(x + 1, y + 1, x + 3, y + h - 1, accent);
		drawCentered(g, text, x, y + (h - 8) / 2, w, 0xFFFFFFFF);
	}

	private void drawIconButton(GuiGraphics g, Identifier tex, int x, int y, int size, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, size, size);
		int drawSize = hover ? size + 2 : size;
		int drawX = hover ? x - 1 : x;
		int drawY = hover ? y - 1 : y;
		rect(g, x - 2, y - 2, size + 4, size + 4, argb(80, 100, 115, 135), hover ? argb(95, 32, 43, 58) : argb(50, 12, 16, 23));
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

	private void rect(GuiGraphics g, int x, int y, int w, int h, int border, int fill) {
		g.fill(x, y, x + w, y + h, border);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
	}

	private void drawTiny(GuiGraphics g, String text, int x, int y, int color) {
		g.pose().pushMatrix();
		g.pose().scale(0.5F, 0.5F);
		g.drawString(font, text, Math.round(x * 2F), Math.round(y * 2F), color, false);
		g.pose().popMatrix();
	}

	private void drawCentered(GuiGraphics g, String text, int x, int y, int w, int color) {
		g.drawString(font, Component.literal(text), x + (w - font.width(Component.literal(text))) / 2, y, color, true);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (inside(mouseX, mouseY, singleX, singleY, buttonW, buttonH)) {
			playClick();
			minecraft.setScreen(new SelectWorldScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, singleX, multiY, buttonW, buttonH)) {
			playClick();
			ensurePortalBdServer();
			minecraft.setScreen(new JoinMultiplayerScreen(this));
			return true;
		}
		if (inside(mouseX, mouseY, singleX, optionsY, buttonW, buttonH)) {
			playClick();
			minecraft.setScreen(new OptionsScreen(this, minecraft.options));
			return true;
		}
		if (inside(mouseX, mouseY, singleX, quitY, buttonW, buttonH) || inside(mouseX, mouseY, closeX, closeY, closeSize, closeSize)) {
			playClick();
			minecraft.stop();
			return true;
		}
		if (inside(mouseX, mouseY, discordX, iconY, iconSize, iconSize)) {
			playClick();
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, youtubeX, iconY, iconSize, iconSize)) {
			playClick();
			openUrl("https://www.youtube.com/@Cosa_5023_YT");
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

	private void ensurePortalBdServer() {
		PortalBDPromotedServer.ensure(minecraft);
	}

	private void ensureDurbinPanoramaPackInstalled() {
		if (packCopied || minecraft == null) {
			return;
		}
		packCopied = true;
		try (InputStream in = DurbinMainMenuScreen.class.getResourceAsStream(BUNDLED_PACK_PATH)) {
			if (in == null) {
				return;
			}
			Path resourcePacks = minecraft.gameDirectory.toPath().resolve("resourcepacks");
			Files.createDirectories(resourcePacks);
			Files.copy(in, resourcePacks.resolve(PACK_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
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
