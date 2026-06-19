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

	private final Screen parent;
	private String selectedTab = "Mods";
	private String selectedFilter = "All";
	private final long openedAt = System.currentTimeMillis();

	public DurbinClientScreen(Screen parent) {
		super(Component.literal("Durbin Client"));
		this.parent = parent;
	}

	private static Identifier uiIcon(String name) {
		return Identifier.fromNamespaceAndPath("axolotlclient", "textures/durbin/ui/" + name);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private float openProgress() {
		float raw = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 220.0F);
		return raw * raw * (3.0F - 2.0F * raw);
	}

	private int panelW() {
		return Math.min(620, Math.max(360, width - 24));
	}

	private int panelH() {
		return Math.min(320, Math.max(236, height - 24));
	}

	private int panelX() {
		return (width - panelW()) / 2;
	}

	private int panelY() {
		return (height - panelH()) / 2 + (int) ((1.0F - openProgress()) * 5.0F);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		int panelW = panelW();
		int panelH = panelH();
		int x = panelX();
		int y = panelY();

		fill(graphics, 0, 0, width, height, argb(35, 0, 0, 0));

		// Compact Badlion-style black transparent panel. Sized to not cut off at high GUI scale.
		rect(graphics, x, y, panelW, panelH, argb(110, 82, 94, 112), argb(190, 8, 10, 14));
		fill(graphics, x + 1, y + 1, x + panelW - 1, y + 28, argb(125, 18, 22, 29));
		fill(graphics, x + 1, y + 28, x + panelW - 1, y + 29, argb(95, 255, 255, 255));

		renderHeader(graphics, x, y, panelW, mouseX, mouseY);
		renderTabs(graphics, x, y, panelW, mouseX, mouseY);
		renderLeft(graphics, x, y, panelH, mouseX, mouseY);
		renderBody(graphics, x, y, panelW, panelH, mouseX, mouseY);
	}

	private void renderHeader(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		drawScaledTexture(g, LOGO, x + 9, y + 4, 108, 24, 360, 80);

		int closeX = x + panelW - 20;
		int closeY = y + 7;
		boolean hover = inside(mouseX, mouseY, closeX, closeY, 12, 12);
		rect(g, closeX, closeY, 12, 12, argb(105, 220, 230, 240), hover ? argb(165, 55, 58, 65) : argb(80, 0, 0, 0));
		drawIcon(g, ICON_CLOSE, closeX + 2, closeY + 2, 8);
	}

	private void renderTabs(GuiGraphics g, int x, int y, int panelW, int mouseX, int mouseY) {
		int tabX = x + 8;
		int tabY = y + 34;
		for (String tab : TABS) {
			int tabW = tabWidth(tab);
			boolean active = tab.equals(selectedTab);
			boolean hover = inside(mouseX, mouseY, tabX, tabY, tabW, 18);
			int fill = active ? argb(225, 34, 152, 210) : hover ? argb(105, 34, 42, 53) : argb(70, 13, 17, 24);
			int border = active ? argb(170, 74, 205, 255) : argb(85, 82, 94, 112);
			rect(g, tabX, tabY, tabW, 18, border, fill);
			drawIcon(g, tabIcon(tab), tabX + 7, tabY + 4, 10);
			draw(g, tab, tabX + 21, tabY + 5, 0xFFFFFFFF, true);
			tabX += tabW + 5;
		}
	}

	private void renderLeft(GuiGraphics g, int x, int y, int panelH, int mouseX, int mouseY) {
		int leftX = x + 8;
		int top = y + 64;

		draw(g, "Filters", leftX + 2, top - 12, 0xFFE9EEF6, true);
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 20;
			boolean active = FILTERS[i].equals(selectedFilter);
			boolean hover = inside(mouseX, mouseY, leftX, by, 102, 16);
			int fill = active ? argb(225, 34, 152, 210) : hover ? argb(105, 35, 45, 58) : argb(55, 10, 14, 20);
			int border = active ? argb(160, 74, 205, 255) : argb(65, 82, 94, 112);
			rect(g, leftX, by, 102, 16, border, fill);
			drawIcon(g, filterIcon(FILTERS[i]), leftX + 8, by + 4, 8);
			draw(g, FILTERS[i], leftX + 23, by + 4, 0xFFFFFFFF, true);
		}

		int linkY = y + panelH - 43;
		iconButton(g, leftX, linkY, 102, 16, "Discord", ICON_DISCORD, mouseX, mouseY);
		iconButton(g, leftX, linkY + 19, 102, 16, "YouTube", ICON_YOUTUBE, mouseX, mouseY);
	}

	private void renderBody(GuiGraphics g, int x, int y, int panelW, int panelH, int mouseX, int mouseY) {
		int bx = x + 122;
		int by = y + 64;
		int bw = panelW - 132;

		if (selectedTab.equals("HUD Editor")) {
			draw(g, "HUD Editor", bx, by, 0xFFFFFFFF, true);
			draw(g, "Move and resize Axolotl HUD entries.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 38, 120, 20, "Open Editor", mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("Profiles")) {
			draw(g, "Profiles", bx, by, 0xFFFFFFFF, true);
			draw(g, "Open Axolotl profile manager.", bx, by + 14, 0xFFD0D0D0, true);
			button(g, bx, by + 38, 120, 20, "Open Profiles", mouseX, mouseY);
			return;
		}
		if (selectedTab.equals("About")) {
			draw(g, "Durbin Client", bx, by, 0xFFFFFFFF, true);
			draw(g, "Minecraft Fabric 1.21.11", bx, by + 14, 0xFFD0D0D0, true);
			draw(g, "Based on AxolotlClient HUD code.", bx, by + 28, 0xFFD0D0D0, true);
			draw(g, "Credits kept: moehreag + contributors", bx, by + 42, 0xFFD0D0D0, true);
			draw(g, "License: LGPL-3.0", bx, by + 56, 0xFFD0D0D0, true);
			return;
		}

		draw(g, "Modules", bx, by - 12, 0xFFFFFFFF, true);
		draw(g, selectedFilter, x + panelW - 60, by - 12, 0xFFBFC6D2, true);

		List<HudEntry> entries = visibleEntries();
		int cols = Math.max(3, Math.min(4, bw / 86));
		int gap = 5;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 48;
		int rows = Math.max(1, Math.min(3, (y + panelH - 10 - by) / (cardH + gap)));
		int maxSlots = cols * rows;
		int slot = 0;

		if (selectedFilter.equals("All") && slot < maxSlots) {
			int cx = slotX(bx, cardW, gap, cols, slot);
			int cy = slotY(by, cardH, gap, cols, slot);
			renderModuleCard(g, "Motion Blur", ICON_GENERIC, DurbinMotionBlur.isEnabled(), cx, cy, cardW, cardH, mouseX, mouseY);
			slot++;
		}

		for (int i = 0; i < entries.size() && slot < maxSlots; i++, slot++) {
			HudEntry entry = entries.get(i);
			int cx = slotX(bx, cardW, gap, cols, slot);
			int cy = slotY(by, cardH, gap, cols, slot);
			renderHudCard(g, entry, cx, cy, cardW, cardH, mouseX, mouseY);
		}

		if (slot == 0) {
			draw(g, "No modules in this filter.", bx, by + 14, 0xFFD0D0D0, true);
		}
	}

	private int slotX(int bx, int cardW, int gap, int cols, int slot) {
		return bx + (slot % cols) * (cardW + gap);
	}

	private int slotY(int by, int cardH, int gap, int cols, int slot) {
		return by + (slot / cols) * (cardH + gap);
	}

	private void renderHudCard(GuiGraphics g, HudEntry entry, int x, int y, int w, int h, int mouseX, int mouseY) {
		renderModuleCard(g, cleanName(entry), iconForEntry(entry), entry.isEnabled(), x, y, w, h, mouseX, mouseY);
	}

	private void renderModuleCard(GuiGraphics g, String name, Identifier icon, boolean enabled, int x, int y, int w, int h, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		int fill = enabled ? argb(128, 22, 38, 48) : hover ? argb(84, 34, 44, 57) : argb(58, 11, 15, 21);
		int border = enabled ? argb(145, 74, 205, 255) : argb(75, 82, 94, 112);
		rect(g, x, y, w, h, border, fill);
		drawCentered(g, trim(name, w - 8), x, y + 4, w, 0xFFFFFFFF);
		drawIcon(g, icon, x + (w - 18) / 2, y + 17, 18);
		toggle(g, x + (w - 22) / 2, y + h - 11, enabled);
	}

	private int tabWidth(String tab) {
		return switch (tab) {
			case "HUD Editor" -> 94;
			case "Profiles" -> 78;
			case "About" -> 70;
			default -> 72;
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

	private void button(GuiGraphics g, int x, int y, int w, int h, String text, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(90, 82, 94, 112), hover ? argb(95, 34, 44, 57) : argb(60, 11, 15, 21));
		drawCentered(g, text, x, y + (h - 8) / 2, w, 0xFFFFFFFF);
	}

	private void iconButton(GuiGraphics g, int x, int y, int w, int h, String text, Identifier icon, int mouseX, int mouseY) {
		boolean hover = inside(mouseX, mouseY, x, y, w, h);
		rect(g, x, y, w, h, argb(70, 82, 94, 112), hover ? argb(88, 34, 44, 57) : argb(50, 11, 15, 21));
		drawIcon(g, icon, x + 8, y + 4, 8);
		draw(g, text, x + 23, y + 4, 0xFFFFFFFF, true);
	}

	private void toggle(GuiGraphics g, int x, int y, boolean enabled) {
		rect(g, x, y, 22, 9, argb(80, 230, 235, 245), enabled ? argb(230, 34, 152, 210) : argb(95, 36, 40, 52));
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

		if (inside(mouseX, mouseY, x + panelW - 20, y + 7, 12, 12)) {
			minecraft.setScreen(parent);
			return true;
		}

		int tabX = x + 8;
		int tabY = y + 34;
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
		int top = y + 64;
		for (int i = 0; i < FILTERS.length; i++) {
			int by = top + i * 20;
			if (inside(mouseX, mouseY, leftX, by, 102, 16)) {
				selectedFilter = FILTERS[i];
				selectedTab = "Mods";
				return true;
			}
		}

		int linkY = y + panelH - 43;
		if (inside(mouseX, mouseY, leftX, linkY, 102, 16)) {
			openUrl("https://discord.gg/PqnbXNrtHR");
			return true;
		}
		if (inside(mouseX, mouseY, leftX, linkY + 19, 102, 16)) {
			openUrl("https://www.youtube.com/@Cosa_5023_YT");
			return true;
		}

		if (selectedTab.equals("HUD Editor")) {
			int bx = x + 122;
			int by = y + 64;
			if (inside(mouseX, mouseY, bx, by + 38, 120, 20)) {
				minecraft.setScreen(new HudEditScreen(this));
				return true;
			}
		}
		if (selectedTab.equals("Profiles")) {
			int bx = x + 122;
			int by = y + 64;
			if (inside(mouseX, mouseY, bx, by + 38, 120, 20)) {
				minecraft.setScreen(new ProfilesScreen(this));
				return true;
			}
		}

		if (motionBlurAt(mouseX, mouseY, x, y, panelW, panelH)) {
			DurbinMotionBlur.toggle();
			return true;
		}

		HudEntry clicked = hudAt(mouseX, mouseY, x, y, panelW, panelH);
		if (clicked != null) {
			clicked.setEnabled(!clicked.isEnabled());
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private boolean motionBlurAt(double mouseX, double mouseY, int x, int y, int panelW, int panelH) {
		if (!selectedTab.equals("Mods") || !selectedFilter.equals("All")) return false;
		int bx = x + 122;
		int by = y + 64;
		int bw = panelW - 132;
		int cols = Math.max(3, Math.min(4, bw / 86));
		int gap = 5;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 48;
		return inside(mouseX, mouseY, bx, by, cardW, cardH);
	}

	private HudEntry hudAt(double mouseX, double mouseY, int x, int y, int panelW, int panelH) {
		if (!selectedTab.equals("Mods")) return null;
		int bx = x + 122;
		int by = y + 64;
		int bw = panelW - 132;
		int cols = Math.max(3, Math.min(4, bw / 86));
		int gap = 5;
		int cardW = (bw - gap * (cols - 1)) / cols;
		int cardH = 48;
		int rows = Math.max(1, Math.min(3, (y + panelH - 10 - by) / (cardH + gap)));
		int maxSlots = cols * rows;
		int slot = selectedFilter.equals("All") ? 1 : 0;
		List<HudEntry> entries = visibleEntries();

		for (int i = 0; i < entries.size() && slot < maxSlots; i++, slot++) {
			int cx = slotX(bx, cardW, gap, cols, slot);
			int cy = slotY(by, cardH, gap, cols, slot);
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
				(selectedFilter.equals("Info") && (key.contains("ping") || key.contains("coord") || key.contains("compass") || key.contains("memory") || key.contains("time") || key.contains("speed")))) {
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
