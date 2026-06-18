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

package io.github.axolotlclient.config.screen;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.PlainTextButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.list.ElementListWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CreditsScreen extends Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final SoundInstance bgm = PositionedSoundInstance.master(SoundEvents.MUSIC_DISC_CHIRP.value(), 1, 1);
	private final HeaderFooterLayoutWidget haF = new HeaderFooterLayoutWidget(this);
	private boolean initialized;

	public CreditsScreen(Screen parent) {
		super(Text.translatable("credits"));
		this.parent = parent;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (AxolotlClient.config().someNiceBackground.get()) { // Credit to pridelib for the colors
			graphics.fill(0, 0, width, height / 6, 0xFFff0018);
			graphics.fill(0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			graphics.fill(0, height * 2 / 6, width, height / 2, 0xFFffff41);
			graphics.fill(0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			graphics.fill(0, height / 2, width, height * 2 / 3, 0xFF008018);
			graphics.fill(0, height * 5 / 6, width, height, 0xFF86007d);
		} else {
			super.renderBackground(graphics, mouseX, mouseY, delta);
		}
	}

	@Override
	public void init() {
		if (initialized) {
			haF.arrangeElements();
			haF.visitWidgets(this::addDrawableSelectableElement);
			return;
		}
		initialized = true;
		haF.addToHeader(getTitle(), textRenderer);
		haF.addToContents(new CreditsList(client, haF.getWidth(), haF.getContentsHeight(), haF.getHeaderHeight(), 25));

		var back = haF.addToFooter(ButtonWidget.builder(CommonTexts.BACK, buttonWidget -> closeScreen()).build());

		haF.arrangeElements();
		haF.visitWidgets(this::addDrawableSelectableElement);

		this.addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("creditsBGM").append(": ")
				.append(Text.translatable(AxolotlClient.config().creditsBGM.get() ? "options.on" : "options.off")),
			buttonWidget -> {
				AxolotlClient.config().creditsBGM.toggle();
				AxolotlClient.getInstance().saveConfig();
				stopBGM();
				buttonWidget.setMessage(Text.translatable("creditsBGM").append(": ").append(
					Text.translatable(AxolotlClient.config().creditsBGM.get() ? "options.on" : "options.off")));
			}).positionAndSize(6, back.getY(), 100, 20).build());
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
		stopBGM();
	}

	@Override
	protected void repositionElements() {
		haF.arrangeElements();
	}

	@Override
	public void tick() {
		tickBGM();
	}

	public void tickBGM() {
		if (AxolotlClient.config().creditsBGM.get() && !client.getSoundManager().isPlaying(bgm)) {
			client.getSoundManager().play(bgm);
		}
	}

	private void stopBGM() {
		client.getSoundManager().stop(bgm);
	}

	private class CreditsList extends ElementListWidget<Entry> {

		public CreditsList(MinecraftClient minecraftClient, int width, int height, int top,
						   int entryHeight) {
			super(minecraftClient, width, height, top, entryHeight);

			addEntry(new SpacerTitle("- - - - - - " + I18n.translate("contributors") + " - - - - - -"));
			Credits.getContributors().forEach(credit -> addEntry(new Credit(credit.getName(), credit.getThings())));

			addEntry(new SpacerTitle("- - - - - - " + I18n.translate("other_people") + " - - - - - -"));
			Credits.getOtherPeople().forEach(credit -> addEntry(new Credit(credit.getName(), credit.getThings())));

			if (!externalModuleCredits.isEmpty()) {
				addEntry(new SpacerTitle("- - - - - - " + I18n.translate("external_modules") + " - - - - - -"));
				externalModuleCredits.forEach((s, s2) -> addEntry(new Credit(s, s2)));
			}
		}
	}

	private abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	private class Credit extends Entry {

		private final String name;
		private final String[] things;
		private final ButtonWidget c;

		public Credit(String name, String... things) {
			this.name = name;
			this.things = things;
			c = new ButtonWidget(0, 0, 200, 20, Text.of(name), buttonWidget -> client.setScreen(new CreditOverlay(this)), Supplier::get) {
				@Override
				protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
					if (isHoveredOrFocused()) {
						RenderUtil.drawOutline(graphics, getX(), getY(), getWidth(), getHeight(), ClientColors.ERROR.toInt());
					}
					int i = this.active ? (isHoveredOrFocused() ? ClientColors.SELECTOR_RED.toInt() : -1) : 10526880;
					this.drawScrollableText(graphics, textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
				}
			};
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			c.setPosition(x, y);
			c.render(graphics, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(c);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(c);
		}
	}

	private class CreditOverlay extends Screen {
		private final Credit credit;

		public CreditOverlay(Credit credit) {
			super(Text.of(credit.name));
			this.credit = credit;
		}

		@Override
		public void closeScreen() {
			client.setScreen(CreditsScreen.this);
		}

		@Override
		public void init() {
			int startY = 100;
			for (String t : credit.things) {
				int textWidth = textRenderer.br$getWidth(t);
				if (t.startsWith("http")) {
					addDrawableSelectableElement(new PlainTextButtonWidget(width / 2 - textWidth / 2, startY, textWidth, 12,
						Text.literal(t).setColor(ClientColors.SELECTOR_GREEN.toInt()), btn ->
						handleTextClick(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, t))), textRenderer));
				} else {
					addDrawable(new TextWidget(width / 2 - textWidth / 2, startY, textWidth, 12,
						Text.literal(t).setColor(ClientColors.SELECTOR_GREEN.toInt()), textRenderer));
				}
				startY += 12;
			}
			addDrawableSelectableElement(ButtonWidget.builder(CommonTexts.BACK, buttonWidget -> closeScreen())
				.position(width / 2 - 75, height - 33 / 2 - 10).build());
		}

		@Override
		public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			super.renderBackground(graphics, mouseX, mouseY, delta);
			RenderUtil.drawRectangle(graphics, 100, 50, width - 200, height - 100,
				ClientColors.DARK_GRAY.withAlpha(127));
			DrawUtil.outlineRect(graphics, 100, 50, width - 200, height - 100,
				ClientColors.BLACK.toInt());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			super.render(graphics, mouseX, mouseY, delta);
			DrawUtil.drawCenteredString(graphics, textRenderer, credit.name,
				width / 2, 57, -16784327, true);
		}

		@Override
		public void tick() {
			CreditsScreen.this.tickBGM();
		}
	}

	private class SpacerTitle extends Entry {

		private final String name;

		public SpacerTitle(String name) {
			this.name = name;
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			DrawUtil.drawCenteredString(graphics, textRenderer, name, x + entryWidth / 2, y, -128374,
				true);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}
	}
}
