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
import io.github.axolotlclient.durbin.nametags.DurbinNameTags;
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
	private static final Identifier ICON_SEARCH = uiIcon("search.png");
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

	private static final String[] TABS = {"Mods", "Nametags", "HUD Editor", "Profiles", "About"};
	private static final String[] FILTERS = {"All", "HUD", "Vanilla", "Items", "Info"};

	private static Identifier uiIcon(String name) {
		return Identifier.fromNamespaceAndPath("axolotlclient", "textures/durbin/ui/" + name);
	}

	private final Screen parent;
	private String selectedTab = "Mods";
	private String selectedFilter = "All";
	private final long openedAt = System.currentTimeMillis();

	public DurbinClientScreen(Screen parent) {
		super(Component.literal("Durbin Client"));
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private float openProgress() {
		float raw = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 260.0F);
		return raw * raw * (3.0F - 2.0F * raw);
	}

	private int panelW() {
		return Math.min(570, width - 34);
	}

	private int panelH() {
		return Math.min(292, height - 34);
	}

	private int panelX() {
		return (width - panelW()) / 2;
	}

	private int panelY() {
		return (height - panelH()) / 2 + (int) ((1.0F - openProgress()) * 6.0F);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		int panelW = panelW();
		int panelH = panelH();
		int x = panelX();
		int y = panelY();

		fill(graphics, 0, 0, width, height, argb(25, 0, 0, 0));

		// Badlion-style compact glass panel.
		rect(graphics, x, y, panelW, panelH, argb(95, 105, 115, 130), argb(150, 10, 14, 18));
		fill(graphics, x + 1, y + 1, x + panelW - 1, y + panelH - 1, argb(35, 255, 255, 255));
		fill(graphics, x + 1, y + 1, x + panelW - 1, y + 25, argb(95, 16, 20, 26));
		fill(graphics, x + 1, y + 25, x + panelW - 1, y + 26, argb(85, 255, 255, 255));

		renderHeader(graphics, x, y, panelW, mouseX, mouseY);
		renderTabs(graphics, x, y, panelW, mouseX, mouseY);
		renderLeft(graphics, x, y, panelH, mouseX, mouseY);
		renderBody(graphics, x, y, panelW, panelH, mouseX, mouseY);
	}

	private void renderHeader(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		drawScaledTexture(g, LOGO, x + 8, y + 3, 102, 20, 360, 80);

		int closeX = x + panelW - 18;
		int closeY = y + 5;
		boolean hover = inside(mouseX, mouseY, closeX, closeY, 12, 12);
		rect(g, closeX, closeY, 12, 12, argb(110, 220, 230, 240), hover ? argb(155, 50, 50, 50) : argb(75, 0, 0, 0));
		drawIcon(g, ICON_CLOSE, closeX + 2, closeY + 2, 8);
	}

	private void renderTabs(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		int tabX = x + 8;
		int tabY = y + 31;
		for (String tab : TABS) {
			int tabW = tabWidth(tab);
			boolean active = tab.equals(selectedTab);
			boolean hover = inside(mouseX, mouseY, tabX, tabY, tabW, 18);
			int fill = active ? argb(230, 45, 164, 218) : hover ? argb(105, 42, 52, 64) : argb(70, 18, 24, 32);
			int border = active ? argb(170, 75, 210, 255) : argb(100, 95, 108, 128);
			rect(g, tabX, tabY, tabW, 18, border, fill);
			drawIcon(g, tabIcon(tab), tabX + 7, tabY + 4, 10);
			draw(g, tab, tabX + 21, tabY + 5, 0xFFFFFFFF, true);
			tabX += tabW + 5;
		}

		int searchW = 95;
		int searchX = x + panelW - searchW - 8;
		if (tabX + 6 < searchX) {
			rect(g, searchX, tabY, searchW, 18, argb(90, 95, 108, 128), argb(80, 8, 12, 18));
			drawIcon(g, ICON_SEARCH, searchX + 7, tabY + 4, 9);
			draw(g, "Search...", searchX + 22, tabY + 5, 0xFFBFC6D2, true);
		}
	}

	private void renderLeft(GuiGraphics g, int x, int y, int panelH, int mouseX, int mouseY) {
		int leftX = x + 8;
		int top = y + 63;

		draw(g, "Filters", leftX + 22, top - 12, 0xFFE9EEF6, true);
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 22;
			boolean active = FILTERS[i].equals(selectedFilter);
			boolean hover = inside(mouseX, mouseY, leftX, by, 95, 17);
			int fill = active ? argb(225, 45, 164, 218) : hover ? argb(105, 45, 56, 69) : argb(55, 8, 12, 18);
			int border = active ? argb(160, 75, 210, 255) : argb(70, 95, 108, 128);
			rect(g, leftX, by, 95, 17, border, fill);
			drawIcon(g, filterIcon(FILTERS[i]), leftX + 8, by + 4, 9);
			draw(g, FILTERS[i], leftX + 23, by + 5, 0xFFFFFFFF, true);
		}

		int linkY = y + panelH - 45;
		iconButton(g, leftX, linkY, 95, 17, "Discord", ICON_DISCORD, mouseX, mouseY);
		iconButton(g, leftX, linkY + 20, 95, 17, "YouTube", ICON_YOUTUBE, mouseX, mouseY);
	}

	private void renderBody(GuiGraphics g, int x, int y, int panelW, int panelH, int mouseX, int mouseY) {
		int bx = x + 112;
		int by = y + 63;
		int bw = panelW - 124;

		if (selectedTab.equals("Nametags")) {
			renderNametagsPage(g, bx, by, bw, mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("HUD Editor")) {
			draw(g, "HUD Editor", bx, by, 0xFFFFFFFF, true);
			draw(g, "Move, resize and configure real Axolotl HUD entries.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 40, 128, 20, "Open HUD Editor", mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("Profiles")) {
			draw(g, "Profiles", bx, by, 0xFFFFFFFF, true);
			draw(g, "Open Axolotl profile manager.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 40, 118, 20, "Open Profiles", mouseX, mouseY);
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

		int favY = by;
		draw(g, "⌄ Favorites", bx, favY - 11, 0xFFFFFFFF, true);
		if (!entries.isEmpty()) {
			renderLargeCard(g, entries.get(0), bx, favY, 78, 65, mouseX, mouseY);
		}

		int defaultY = favY + 86;
		draw(g, "⌄ Default", bx, defaultY - 11, 0xFFFFFFFF, true);
		int cols = 5;
		int gap = 4;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 64;
		for (int i = 1; i < entries.size() && i <= 10; i++) {
			int index = i - 1;
			HudEntry entry = entries.get(i);
			int col = index % cols;
			int row = index / cols;
			int cx = bx + col * (cardW + gap);
			int cy = defaultY + row * (cardH + gap);
			renderLargeCard(g, entry, cx, cy, cardW, cardH, mouseX, mouseY);
		}
	}

	private void renderNametagsPage(GuiGraphics g, int bx, int by, int bw, int mouseX, int mouseY) {
		DurbinNameTags tags = DurbinNameTags.getInstance();
		draw(g, "Firebase Nametags", bx, by, 0xFFFFFFFF, true);
		draw(g, "Colored client-side lines above player names.", bx, by + 14, 0xFFD0D0D0, true);

		int cardY = by + 34;
		rect(g, bx, cardY, bw, 72, argb(75, 105, 116, 136), argb(52, 18, 24, 32));
		draw(g, "Enabled", bx + 10, cardY + 10, 0xFFFFFFFF, true);
		toggle(g, bx + 72, cardY + 10, tags.isEnabled());
		button(g, bx + 110, cardY + 5, 78, 20, "Sync Now", mouseX, mouseY);
		draw(g, "Status: " + trim(tags.statusText(), bw - 20), bx + 10, cardY + 32, 0xFFD0D0D0, true);
		draw(g, "Players: " + tags.playerCount() + "   Last sync: " + tags.lastSyncText(), bx + 10, cardY + 48, 0xFFBFC6D2, true);

		int infoY = cardY + 85;
		rect(g, bx, infoY, bw, 84, argb(65, 105, 116, 136), argb(45, 12, 16, 22));
		draw(g, "Firebase URL: " + tags.shortFirebaseUrl(), bx + 10, infoY + 10, 0xFFE9EEF6, true);
		draw(g, "Edit config/durbin-nametags.properties", bx + 10, infoY + 26, 0xFFD0D0D0, true);
		draw(g, "Cache: config/durbin-nametags-cache.json", bx + 10, infoY + 42, 0xFFD0D0D0, true);
		draw(g, "Offline mode uses the cache automatically.", bx + 10, infoY + 58, 0xFFBFC6D2, true);

		int previewY = infoY + 98;
		draw(g, "Example Firebase lines:", bx, previewY, 0xFFFFFFFF, true);
		draw(g, "COSA -> blue owner / green member / red clan", bx, previewY + 14, 0xFFBFC6D2, true);
	}

	private void renderLargeCard(GuiGraphics g, HudEntry entry, int x, int y, int w, int h, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		if (hover) {
			y -= 1;
		}

		boolean enabled = entry.isEnabled();
		int fill = enabled ? argb(125, 26, 40, 48) : hover ? argb(80, 36, 46, 58) : argb(56, 18, 24, 32);
		int border = enabled ? argb(135, 74, 210, 255) : argb(75, 104, 116, 136);
		rect(g, x, y, w, h, border, fill);

		String name = trim(cleanName(entry), w - 10);
		drawCentered(g, name, x, y + 5, w, 0xFFFFFFFF);

		drawIcon(g, iconForEntry(entry), x + (w - 20) / 2, y + 22, 20);
		toggle(g, x + (w - 22) / 2, y + h - 13, enabled);
	}

	private int tabWidth(String tab) {
		return switch (tab) {
			case "HUD Editor" -> 94;
			case "Nametags" -> 88;
			case "Profiles" -> 78;
			case "About" -> 70;
			default -> 72;
		};
	}

	private Identifier tabIcon(String tab) {
		return switch (tab) {
			case "HUD Editor" -> ICON_TAB_HUD_EDITOR;
			case "Nametags" -> ICON_GENERIC;
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

	private void button(GuiGraphics g, int x, int y, int w, int h, String text, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(90, 105, 116, 136), hover ? argb(95, 45, 56, 69) : argb(56, 18, 24, 32));
		drawCentered(g, text, x, y + (h - 8) / 2, w, 0xFFFFFFFF);
	}

	private void iconButton(GuiGraphics g, int x, int y, int w, int h, String text, Identifier icon, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(75, 105, 116, 136), hover ? argb(88, 45, 56, 69) : argb(50, 18, 24, 32));
		drawIcon(g, icon, x + 8, y + 4, 9);
		draw(g, text, x + 24, y + 5, 0xFFFFFFFF, true);
	}

	private void toggle(GuiGraphics g, int x, int y, boolean enabled) {
		rect(g, x, y, 22, 9, argb(80, 230, 235, 245), enabled ? argb(230, 45, 164, 218) : argb(95, 36, 40, 52));
		fill(g, x + (enabled ? 13 : 1), y + 1, x + (enabled ? 21 : 9), y + 8, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		int panelW = panelW();
		int panelH = panelH();
		int x = panelX();
		int y = panelY();

		if (inside(mouseX, mouseY, x + panelW - 18, y + 5, 12, 12)) {
			minecraft.setScreen(parent);
			return true;
		}

		int tabX = x + 8;
		int tabY = y + 31;
		for (String tab : TABS) {
			int tabW = tabWidth(tab);
			if (inside(mouseX, mouseY, tabX, tabY, tabW, 18)) {
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
		int top = y + 63;
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 22;
			if (inside(mouseX, mouseY, leftX, by, 95, 17)) {
				selectedFilter = FILTERS[i];
				return true;
			}
		}

		int linkY = y + panelH - 45;
		if (inside(mouseX, mouseY, leftX, linkY, 95, 17)) {
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, leftX, linkY + 20, 95, 17)) {
			openUrl("https://www.youtube.com/@Cosa_5023_YT");
			return true;
		}

		if (selectedTab.equals("Nametags")) {
			int bx = x + 112;
			int by = y + 63;
			int cardY = by + 34;
			if (inside(mouseX, mouseY, bx + 72, cardY + 10, 22, 9)) {
				DurbinNameTags.getInstance().toggleEnabled();
				return true;
			}
			if (inside(mouseX, mouseY, bx + 110, cardY + 5, 78, 20)) {
				DurbinNameTags.getInstance().refreshNow();
				return true;
			}
		}

		if (selectedTab.equals("HUD Editor")) {
			int bx = x + 112;
			int by = y + 63;
			if (inside(mouseX, mouseY, bx, by + 40, 128, 20)) {
				minecraft.setScreen(new HudEditScreen(this));
				return true;
			}
		}
		if (selectedTab.equals("Profiles")) {
			int bx = x + 112;
			int by = y + 63;
			if (inside(mouseX, mouseY, bx, by + 40, 118, 20)) {
				minecraft.setScreen(new ProfilesScreen(this));
				return true;
			}
		}

		if (motionBlurAt(mouseX, mouseY, x, y, panelW)) {
			DurbinMotionBlur.toggle();
			return true;
		}

		HudEntry clicked = hudAt(mouseX, mouseY, x, y, panelW);
		if (clicked != null) {
			clicked.setEnabled(!clicked.isEnabled());
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private boolean motionBlurAt(double mouseX, double mouseY, int x, int y, int panelW) {
		if (!selectedTab.equals("Mods")) return false;
		int bx = x + 100;
		int by = y + 55;
		int bw = panelW - 110;
		int cols = 3;
		int gap = 7;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 39;
		int cx = bx + 2 * (cardW + gap);
		int cy = by + 2 * (cardH + gap);
		return inside(mouseX, mouseY, cx, cy, cardW, cardH);
	}

	private HudEntry hudAt(double mouseX, double mouseY, int x, int y, int panelW) {
		if (!selectedTab.equals("Mods")) return null;
		int bx = x + 112;
		int by = y + 63;
		int bw = panelW - 124;
		List<HudEntry> entries = visibleEntries();

		// Favorite card.
		if (!entries.isEmpty() && inside(mouseX, mouseY, bx, by, 78, 65)) {
			return entries.get(0);
		}

		int defaultY = by + 86;
		int cols = 5;
		int gap = 4;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 64;
		for (int i = 1; i < entries.size() && i <= 10; i++) {
			int index = i - 1;
			int col = index % cols;
			int row = index / cols;
			int cx = bx + col * (cardW + gap);
			int cy = defaultY + row * (cardH + gap);
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

	private void drawIcon(GuiGraphics g, Identifier icon, int x, int y, int size) {
		drawScaledTexture(g, icon, x, y, size, size, 24, 24);
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
