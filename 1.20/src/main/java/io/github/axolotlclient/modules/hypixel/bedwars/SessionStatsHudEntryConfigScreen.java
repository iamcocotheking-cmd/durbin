/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;

import io.github.axolotlclient.AxolotlClientCommon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;


public class SessionStatsHudEntryConfigScreen extends Screen {
	private final Screen parent;
	private final SessionStatisticsOverlay hud = BedwarsMod.getInstance().getSessionStatsOverlay();

	public SessionStatsHudEntryConfigScreen(Screen parent) {
		super(tr("title"));
		this.parent = parent;
	}

	private static Text tr(String key) {
		return Text.translatable("bedwars.session_stats.configure." + key);
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
		AxolotlClientCommon.getInstance().saveConfig();
	}

	@Override
	protected void init() {
		var title = addDrawableChild(new TextWidget(getTitle(), textRenderer));
		title.setPosition(width / 2 - title.getWidth() / 2, 33 / 2 - title.getHeight() / 2);
		addDrawableChild(new EntryList(client, width, height, 33, height - 33, 24));
		var back = addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, btn -> closeScreen()).build());
		back.setPosition(width / 2 - back.getWidth() / 2, height - 33 / 2 - back.getHeight() / 2);
	}

	private class EntryList extends ElementListWidget<ListEntry> {

		private final int rowWidth;

		public EntryList(MinecraftClient minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
			rowWidth = hud.getEntries().stream().mapToInt(e -> textRenderer.getWidth((Text) e.name())).max().orElse(100) + 180;
			hud.getEntries().forEach(e -> addEntry(new ListEntry(e, this)));
		}

		void moveEntry(ListEntry entry, int direction) {
			var index = children().indexOf(entry);
			var next = index + direction;
			var n = children().get(next);
			children().set(index, n);
			children().set(next, entry);
			var v = hud.getEntries().get(next);
			hud.getEntries().set(index, v);
			hud.getEntries().set(next, entry.entry);
		}

		@Override
		public int getRowWidth() {
			return rowWidth;
		}

		@Override
		protected int getScrollbarPositionX() {
			return this.getRowRight() + 6 + 2;
		}
	}

	private class ListEntry extends ElementListWidget.Entry<ListEntry> {
		private final TextWidget name;
		private final CyclingButtonWidget<Boolean> showHide;
		private final SessionStatisticsOverlay.SessionStatsEntry entry;
		private final EntryList list;
		private final ButtonWidget up, down;

		private ListEntry(SessionStatisticsOverlay.SessionStatsEntry entry, EntryList list) {
			this.entry = entry;
			this.list = list;
			name = new TextWidget((Text) entry.name(), textRenderer)
				.alignLeft();
			showHide = CyclingButtonWidget.onOffBuilder(tr("entry.hide"), tr("entry.show")).initially(entry.enabled().get())
				.omitKeyText()
				.build(0, 0, 50, 20, Text.empty(), (b, v) -> entry.enabled().set(v));
			up = ButtonWidget.builder(tr("entry.move_up"), btn ->
				client.execute(() -> list.moveEntry(this, -1))).width(50).build();
			down = ButtonWidget.builder(tr("entry.move_down"), btn ->
				client.execute(() -> list.moveEntry(this, 1))).width(50).build();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(name, showHide, up, down);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int entryY, int entryX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {

			var x = entryX + 2;
			var y = entryY + 2;
			name.setPosition(x, y + (entryHeight - 4) / 2 - name.getHeight() / 2);
			var right = entryX + entryWidth - 2;
			down.setPosition(right - down.getWidth(), y);
			up.setPosition(down.getX() - up.getWidth(), y);
			showHide.setPosition(up.getX() - showHide.getWidth(), y);

			var size = list.children().size();

			down.active = index != size - 1;
			up.active = index != 0;

			name.render(guiGraphics, mouseX, mouseY, partialTick);
			down.render(guiGraphics, mouseX, mouseY, partialTick);
			up.render(guiGraphics, mouseX, mouseY, partialTick);
			showHide.render(guiGraphics, mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(name, showHide, up, down);
		}
	}
}
