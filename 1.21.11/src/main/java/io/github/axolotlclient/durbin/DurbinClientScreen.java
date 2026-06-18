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

import io.github.axolotlclient.config.screen.ProfilesScreen;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DurbinClientScreen extends Screen {
	private static final Identifier LOGO = Identifier.fromNamespaceAndPath("axolotlclient", "textures/durbin_header_logo.png");
	private static final Identifier ICON_TAB_MODS = uiIcon("tab_mods.png");
	private static final Identifier ICON_TAB_HUD_EDITOR = uiIcon("tab_hud_editor.png");
	private static final Identifier ICON_TAB_PROFILES = uiIcon("tab_profiles.png");
	private static final Identifier ICON_TAB_ABOUT = uiIcon("tab_about.png");
	private static final Identifier ICON_FILTER_ALL = uiIcon("filter_all.png");
	private static final Identifier ICON_FILTER_HUD = uiIcon("filter_hud.png");
	private static final Identifier ICON_FILTER_VANILLA = uiIcon("filter_vanilla.png");
	private static final Identifier ICON_FILTER_ITEMS = uiIcon("filter_items.png");
	private static final Identifier ICON_FILTER_INFO = uiIcon("filter_info.png");
	private static final Identifier ICON_DISCORD = uiIcon("btn_discord.png");
	private static final Identifier ICON_YOUTUBE = uiIcon("btn_youtube.png");
	private static final Identifier ICON_CLOSE = uiIcon("close.png");
	private static final Identifier ICON_FPS = uiIcon("hud_fps.png");
	private static final Identifier ICON_CPS = uiIcon("hud_cps.png");
	private static final Identifier ICON_KEYSTROKES = uiIcon("hud_keystrokes.png");
	private static final Identifier ICON_ARMOR = uiIcon("hud_armor.png");
	private static final Identifier ICON_COORDS = uiIcon("hud_coords.png");
	private static final Identifier ICON_PING = uiIcon("hud_ping.png");
	private static final Identifier ICON_POTION = uiIcon("hud_potion.png");
	private static final Identifier ICON_DIRECTION = uiIcon("hud_direction.png");
	private static final Identifier ICON_ITEMS = uiIcon("hud_items.png");
	private static final Identifier ICON_HUD_ICON = uiIcon("hud_icon.png");
	private static final Identifier ICON_SPEED = uiIcon("hud_speed.png");
	private static final Identifier ICON_GENERIC = uiIcon("hud_generic.png");
	private static final String[] TABS = {"Mods", "HUD Editor", "Profiles", "About"};
	private static final String[] FILTERS = {"All", "HUD", "Vanilla", "Items", "Info"};

	private static Identifier uiIcon(String name) {
		return Identifier.fromNamespaceAndPath("axolotlclient", "textures/durbin/ui/" + name);
	}

	private final Screen parent;
	private String selectedTab = "Mods";
	private String selectedFilter = "All";

	public DurbinClientScreen(Screen parent) {
		super(Component.literal("Durbin Client"));
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		int panelW = Math.min(430, width - 34);
		int panelH = Math.min(226, height - 34);
		int x = (width - panelW) / 2;
		int y = (height - panelH) / 2;

		fill(graphics, 0, 0, width, height, argb(18, 0, 0, 0));

		// black, very transparent, square main panel
		rect(graphics, x, y, panelW, panelH, argb(95, 210, 220, 235), argb(95, 0, 0, 0));
		rect(graphics, x, y, panelW, 24, argb(110, 220, 230, 240), argb(120, 0, 0, 0));
		fill(graphics, x + 1, y + 24, x + panelW - 1, y + 25, argb(75, 255, 255, 255));

		renderHeader(graphics, x, y, panelW, mouseX, mouseY);
		renderTabs(graphics, x, y, panelW, mouseX, mouseY);
		renderLeft(graphics, x, y, panelH, mouseX, mouseY);
		renderBody(graphics, x, y, panelW, panelH, mouseX, mouseY);
	}

	private void renderHeader(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		// Wide DURBIN logo in the header, similar to the reference style
		int logoW = 108;
		int logoH = 24;
		drawScaledTexture(g, LOGO, x + 8, y + 0, logoW, logoH, 360, 80);

		int closeX = x + panelW - 16;
		int closeY = y + 6;
		boolean hover = inside(mouseX, mouseY, closeX, closeY, 10, 10);
		rect(g, closeX, closeY, 10, 10, argb(120, 230, 235, 245), hover ? argb(120, 50, 50, 50) : argb(70, 0, 0, 0));
		drawIcon(g, ICON_CLOSE, closeX + 1, closeY + 1, 8);
	}

	private void renderTabs(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		int tabX = x + 8;
		int tabY = y + 30;
		for (String tab : TABS) {
			int tabW = tabWidth(tab);
			boolean active = tab.equals(selectedTab);
			boolean hover = inside(mouseX, mouseY, tabX, tabY, tabW, 15);
			int fill = active ? argb(120, 0, 0, 0) : hover ? argb(70, 20, 20, 20) : argb(48, 0, 0, 0);
			rect(g, tabX, tabY, tabW, 15, argb(82, 230, 235, 245), fill);
			drawIcon(g, tabIcon(tab), tabX + 5, tabY + 3, 9);
			draw(g, tab, tabX + 18, tabY + 4, 0xFFFFFFFF, true);
			tabX += tabW + 5;
		}
	}

	private void renderLeft(GuiGraphics g, int x, int y, int panelH, int mouseX, int mouseY) {
		int leftX = x + 8;
		int top = y + 55;
		draw(g, "Filter", leftX, top - 10, 0xFFD8D8D8, true);
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 17;
			boolean active = FILTERS[i].equals(selectedFilter);
			boolean hover = inside(mouseX, mouseY, leftX, by, 82, 14);
			int fill = active ? argb(105, 0, 0, 0) : hover ? argb(62, 25, 25, 25) : argb(42, 0, 0, 0);
			rect(g, leftX, by, 82, 14, argb(90, 230, 235, 245), fill);
			drawIcon(g, filterIcon(FILTERS[i]), leftX + 5, by + 3, 8);
			draw(g, FILTERS[i], leftX + 18, by + 3, 0xFFFFFFFF, true);
		}

		int linkY = y + panelH - 38;
		iconButton(g, leftX, linkY, 82, 14, "Discord", ICON_DISCORD, mouseX, mouseY);
		iconButton(g, leftX, linkY + 16, 82, 14, "YouTube", ICON_YOUTUBE, mouseX, mouseY);
	}

	private void renderBody(GuiGraphics g, int x, int y, int panelW, int panelH, int mouseX, int mouseY) {
		int bx = x + 100;
		int by = y + 55;
		int bw = panelW - 110;

		if (selectedTab.equals("HUD Editor")) {
			draw(g, "Axolotl HUD Editor", bx, by, 0xFFFFFFFF, true);
			draw(g, "Move, resize and configure real Axolotl HUD entries.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 38, 130, 18, "Open HUD Editor", mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("Profiles")) {
			draw(g, "Axolotl Profiles", bx, by, 0xFFFFFFFF, true);
			draw(g, "Open Axolotl profile manager.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 38, 120, 18, "Open Profiles", mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("About")) {
			draw(g, "Durbin Client", bx, by, 0xFFFFFFFF, true);
			draw(g, "Uses AxolotlClient HUD code.", bx, by + 14, 0xFFD0D0D0, true);
			draw(g, "Credits: moehreag + Axolotl contributors", bx, by + 28, 0xFFD0D0D0, true);
			draw(g, "License: LGPL-3.0", bx, by + 42, 0xFFD0D0D0, true);
			return;
		}

		List<HudEntry> entries = visibleEntries();
		draw(g, selectedFilter, bx, by - 10, 0xFFFFFFFF, true);
		int cols = 3;
		int gap = 7;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 39;
		for (int i = 0; i < entries.size() && i < 9; i++) {
			HudEntry entry = entries.get(i);
			int col = i % cols;
			int row = i / cols;
			int cx = bx + col * (cardW + gap);
			int cy = by + row * (cardH + gap);
			renderHudCard(g, entry, cx, cy, cardW, cardH, mouseX, mouseY);
		}
	}

	private void renderHudCard(GuiGraphics g, HudEntry entry, int x, int y, int w, int h, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		boolean enabled = entry.isEnabled();
		int fill = enabled ? argb(108, 0, 0, 0) : hover ? argb(62, 20, 20, 20) : argb(44, 0, 0, 0);
		rect(g, x, y, w, h, enabled ? argb(96, 255, 255, 255) : argb(56, 230, 235, 245), fill);
		String name = cleanName(entry);
		drawIcon(g, iconForEntry(entry), x + 6, y + 7, 13);
		draw(g, trim(name, w - 30), x + 24, y + 7, 0xFFFFFFFF, true);
		draw(g, enabled ? "On" : "Off", x + 24, y + 20, enabled ? 0xFFFFFFFF : 0xFFC8C8C8, true);
		toggle(g, x + w - 28, y + h - 13, enabled);
	}

	private int tabWidth(String tab) {
		return switch (tab) {
			case "HUD Editor" -> 88;
			case "Profiles" -> 72;
			case "About" -> 64;
			default -> 62;
		};
	}

	private Identifier tabIcon(String tab) {
		return switch (tab) {
			case "HUD Editor" -> ICON_TAB_HUD_EDITOR;
			case "Profiles" -> ICON_TAB_PROFILES;
			case "About" -> ICON_TAB_ABOUT;
			default -> ICON_TAB_MODS;
		};
	}

	private Identifier filterIcon(String filter) {
		return switch (filter) {
			case "HUD" -> ICON_FILTER_HUD;
			case "Vanilla" -> ICON_FILTER_VANILLA;
			case "Items" -> ICON_FILTER_ITEMS;
			case "Info" -> ICON_FILTER_INFO;
			default -> ICON_FILTER_ALL;
		};
	}

	private Identifier iconForEntry(HudEntry entry) {
		String key = entry.getId().br$getPath().toLowerCase(Locale.ROOT);
		String name = cleanName(entry).toLowerCase(Locale.ROOT);
		String combined = key + " " + name;
		if (combined.contains("fps")) return ICON_FPS;
		if (combined.contains("cps")) return ICON_CPS;
		if (combined.contains("keystroke") || combined.contains("toggle") || combined.contains("sprint")) return ICON_KEYSTROKES;
		if (combined.contains("armor")) return ICON_ARMOR;
		if (combined.contains("coord")) return ICON_COORDS;
		if (combined.contains("ping") || combined.contains("ip")) return ICON_PING;
		if (combined.contains("potion")) return ICON_POTION;
		if (combined.contains("compass") || combined.contains("direction")) return ICON_DIRECTION;
		if (combined.contains("item") || combined.contains("inventory") || combined.contains("arrow")) return ICON_ITEMS;
		if (combined.contains("icon")) return ICON_HUD_ICON;
		if (combined.contains("speed")) return ICON_SPEED;
		return ICON_GENERIC;
	}

	private void drawIcon(GuiGraphics g, Identifier icon, int x, int y, int size) {
		drawScaledTexture(g, icon, x, y, size, size, 24, 24);
	}

	private void button(GuiGraphics g, int x, int y, int w, int h, String text, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(90, 230, 235, 245), hover ? argb(68, 25, 25, 25) : argb(44, 0, 0, 0));
		drawCentered(g, text, x, y + (h - 8) / 2, w, 0xFFFFFFFF);
	}

	private void iconButton(GuiGraphics g, int x, int y, int w, int h, String text, Identifier icon, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(90, 230, 235, 245), hover ? argb(68, 25, 25, 25) : argb(44, 0, 0, 0));
		drawIcon(g, icon, x + 7, y + 3, 8);
		draw(g, text, x + 20, y + (h - 8) / 2, 0xFFFFFFFF, true);
	}

	private void toggle(GuiGraphics g, int x, int y, boolean enabled) {
		rect(g, x, y, 22, 9, argb(82, 230, 235, 245), enabled ? argb(170, 255, 255, 255) : argb(90, 0, 0, 0));
		fill(g, x + (enabled ? 13 : 1), y + 1, x + (enabled ? 21 : 9), y + 8, enabled ? 0xFF050505 : 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int panelW = Math.min(430, width - 34);
		int panelH = Math.min(226, height - 34);
		int x = (width - panelW) / 2;
		int y = (height - panelH) / 2;

		if (inside(mouseX, mouseY, x + panelW - 16, y + 6, 10, 10)) {
			minecraft.setScreen(parent);
			return true;
		}

		int tabX = x + 8;
		int tabY = y + 30;
		for (String tab : TABS) {
			int tabW = tabWidth(tab);
			if (inside(mouseX, mouseY, tabX, tabY, tabW, 15)) {
				selectedTab = tab;
				if (tab.equals("HUD Editor")) {
					minecraft.setScreen(new HudEditScreen(this));
				} else if (tab.equals("Profiles")) {
					minecraft.setScreen(new ProfilesScreen(this));
				}
				return true;
			}
			tabX += tabW + 5;
		}

		int leftX = x + 8;
		int top = y + 55;
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 17;
			if (inside(mouseX, mouseY, leftX, by, 82, 14)) {
				selectedFilter = FILTERS[i];
				return true;
			}
		}

		int linkY = y + panelH - 38;
		if (inside(mouseX, mouseY, leftX, linkY, 82, 14)) {
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, leftX, linkY + 16, 82, 14)) {
			openUrl("https://www.youtube.com/@Cosa_5023_YT");
			return true;
		}

		if (selectedTab.equals("HUD Editor")) {
			int bx = x + 100;
			int by = y + 55;
			if (inside(mouseX, mouseY, bx, by + 38, 130, 18)) {
				minecraft.setScreen(new HudEditScreen(this));
				return true;
			}
		}
		if (selectedTab.equals("Profiles")) {
			int bx = x + 100;
			int by = y + 55;
			if (inside(mouseX, mouseY, bx, by + 38, 120, 18)) {
				minecraft.setScreen(new ProfilesScreen(this));
				return true;
			}
		}

		HudEntry clicked = hudAt(mouseX, mouseY, x, y, panelW);
		if (clicked != null) {
			clicked.setEnabled(!clicked.isEnabled());
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private HudEntry hudAt(double mouseX, double mouseY, int x, int y, int panelW) {
		if (!selectedTab.equals("Mods")) return null;
		int bx = x + 100;
		int by = y + 55;
		int bw = panelW - 110;
		List<HudEntry> entries = visibleEntries();
		int cols = 3;
		int gap = 7;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 39;
		for (int i = 0; i < entries.size() && i < 9; i++) {
			int col = i % cols;
			int row = i / cols;
			int cx = bx + col * (cardW + gap);
			int cy = by + row * (cardH + gap);
			if (inside(mouseX, mouseY, cx, cy, cardW, cardH)) return entries.get(i);
		}
		return null;
	}

	private List<HudEntry> visibleEntries() {
		List<HudEntry> all = HudManager.getInstance().getEntries();
		List<HudEntry> out = new ArrayList<>();
		for (HudEntry entry : all) {
			String key = entry.getId().br$getPath().toLowerCase(Locale.ROOT);
			if (selectedFilter.equals("All") ||
				(selectedFilter.equals("HUD") && (key.contains("fps") || key.contains("cps") || key.contains("keystroke") || key.contains("potion") || key.contains("armor"))) ||
				(selectedFilter.equals("Vanilla") && (key.contains("hotbar") || key.contains("crosshair") || key.contains("scoreboard") || key.contains("bossbar") || key.contains("actionbar"))) ||
				(selectedFilter.equals("Items") && (key.contains("armor") || key.contains("arrow") || key.contains("inventory") || key.contains("item"))) ||
				(selectedFilter.equals("Info") && (key.contains("ping") || key.contains("coords") || key.contains("compass") || key.contains("memory") || key.contains("time") || key.contains("speed")))) {
				out.add(entry);
			}
		}
		return out;
	}

	private String cleanName(HudEntry entry) {
		String name = entry.getName();
		if (name == null || name.isBlank()) name = entry.getId().br$getPath();
		name = name.replace('_', ' ').replace("hud", "").replace("Hud", "").trim();
		String[] words = name.split(" ");
		StringBuilder b = new StringBuilder();
		for (String w : words) {
			if (w.isBlank()) continue;
			b.append(Character.toUpperCase(w.charAt(0))).append(w.length() > 1 ? w.substring(1) : "").append(' ');
		}
		return b.toString().trim();
	}

	private void rect(GuiGraphics g, int x, int y, int w, int h, int border, int fill) {
		fill(g, x, y, x + w, y + h, border);
		fill(g, x + 1, y + 1, x + w - 1, y + h - 1, fill);
	}

	private void fill(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
		g.fill(x1, y1, x2, y2, color);
	}

	private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
	}

	private int argb(int a, int r, int g, int b) {
		return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

	private void drawScaledTexture(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int tw, int th) {
		float sx = (float) w / (float) tw;
		float sy = (float) h / (float) th;
		g.pose().pushMatrix();
		g.pose().scale(sx, sy);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, Math.round(x / sx), Math.round(y / sy), 0, 0, tw, th, tw, th);
		g.pose().popMatrix();
	}

	private Component uiText(String text) {
		return Component.literal(text);
	}

	private void draw(GuiGraphics g, String text, int x, int y, int color, boolean shadow) {
		g.drawString(font, uiText(text), x, y, color, shadow);
	}

	private int textWidth(String text) {
		return font.width(uiText(text));
	}

	private void drawCentered(GuiGraphics g, String text, int x, int y, int w, int color) {
		g.drawString(font, uiText(text), x + (w - textWidth(text)) / 2, y, color, true);
	}

	private String trim(String text, int maxWidth) {
		if (textWidth(text) <= maxWidth) return text;
		String out = text;
		while (!out.isEmpty() && textWidth(out + "...") > maxWidth) {
			out = out.substring(0, out.length() - 1);
		}
		return out + "...";
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
