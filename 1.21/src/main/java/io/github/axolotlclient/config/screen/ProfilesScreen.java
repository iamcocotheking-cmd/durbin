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

package io.github.axolotlclient.config.screen;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.RecreatableScreen;
import io.github.axolotlclient.config.profiles.Profiles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.list.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ProfilesScreen extends Screen implements RecreatableScreen {

	private final HeaderFooterLayoutWidget haL = new HeaderFooterLayoutWidget(this);
	private ProfilesList profilesList;
	private final Screen parent;
	private boolean initialized;

	public ProfilesScreen(Screen parent) {
		super(Text.translatable("profiles.configure.list"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		if (!initialized) {
			initialized = true;
			haL.addToHeader(getTitle(), textRenderer);
			profilesList = new ProfilesList(client, 0, 0, 0, 25);
			haL.addToContents(profilesList);
			var footer = haL.addToFooter(LinearLayoutWidget.createHorizontal()).setSpacing(4);
			footer.add(ButtonWidget.builder(CommonTexts.BACK, btn -> closeScreen()).build());
			haL.visitWidgets(this::addDrawableSelectableElement);
		}

		repositionElements();
	}

	@Override
	protected void repositionElements() {
		haL.arrangeElements();
		profilesList.setDimensionsWithLayout(this.width, this.haL);
	}

	@Override
	public void closeScreen() {
		Profiles.getInstance().saveProfiles();
		//noinspection DataFlowIssue
		client.setScreen(parent);
	}

	@Override
	public @NotNull Screen recreate() {
		return new ProfilesScreen(RecreatableScreen.tryRecreate(parent));
	}

	public class ProfilesList extends ElementListWidget<ProfilesList.Entry> {
		private static final Entry SPACER = new SpacerEntry();
		private final Entry ADD = new NewEntry();

		public ProfilesList(MinecraftClient minecraft, int width, int height, int y, int itemHeight) {
			super(minecraft, width, height, y, itemHeight);
			reload();
		}

		public void reload() {
			clearEntries();
			Profiles.getInstance().iterateAvailable(p -> addEntry(new ProfileEntry(p)));
			addEntry(SPACER);
			addEntry(ADD);
		}

		@Override
		public int getRowWidth() {
			return 340;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			children().stream().filter(e -> e instanceof ProfileEntry)
				.map(e -> (ProfileEntry) e).map(e -> e.profileName).forEach(e -> e.setFocused(false));
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Environment(EnvType.CLIENT)
		public abstract static class Entry extends ElementListWidget.Entry<Entry> {

		}

		public static class SpacerEntry extends Entry {

			@Override
			public List<? extends Selectable> selectableChildren() {
				return List.of();
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

			}

			@Override
			public List<? extends Element> children() {
				return List.of();
			}
		}

		@Environment(EnvType.CLIENT)
		public class ProfileEntry extends Entry {
			private static final Text EXPORT_BUTTON_TITLE = Text.translatable("profiles.profile.export");
			private static final Text CURRENT_TEXT = Text.translatable("profiles.profile.current");
			private static final Text LOAD_BUTTON_TITLE = Text.translatable("profiles.profile.load");
			private static final Text DUPLICATE_BUTTON_TITLE = Text.translatable("profiles.profile.duplicate");
			private static final Text REMOVE_BUTTON_TITLE = Text.translatable("profiles.profile.remove");
			private final TextFieldWidget profileName;
			private final ButtonWidget exportButton, loadButton, duplicateButton, removeButton;
			private final Profiles.Profile profile;

			ProfileEntry(Profiles.Profile profile) {
				this.profile = profile;
				profileName = new TextFieldWidget(textRenderer, 0, 0, 150, 20, Text.empty());
				profileName.setText(profile.name());
				profileName.setChangedListener(profile::setName);
				exportButton = ButtonWidget.builder(EXPORT_BUTTON_TITLE, btn -> {
					btn.active = false;
					Profiles.getInstance().exportProfile(profile).thenRun(() -> btn.active = true);
				}).positionAndSize(0, 0, 50, 20).build();
				loadButton = ButtonWidget.builder(LOAD_BUTTON_TITLE, btn ->
					Profiles.getInstance().switchTo(profile)).positionAndSize(0, 0, 50, 20).build();
				duplicateButton = ButtonWidget.builder(DUPLICATE_BUTTON_TITLE, b -> {
					var dup = Profiles.getInstance().duplicate(profile);
					double d = (double) ProfilesList.this.getMaxScroll() - ProfilesList.this.getScrollAmount();
					ProfilesList.this.children().add(ProfilesList.this.children().indexOf(ProfileEntry.this) + 1, new ProfileEntry(dup));
					ProfilesList.this.setScrollAmount(ProfilesList.this.getMaxScroll() - d);
				}).positionAndSize(0, 0, 50, 20).build();

				this.removeButton = ButtonWidget.builder(REMOVE_BUTTON_TITLE, b -> {
						removeEntry(this);
						Profiles.getInstance().remove(profile);
						refreshScrollAmount();
					}).positionAndSize(0, 0, 50, 20)
					.build();
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				int i = getScrollbarPositionX() - removeButton.getWidth() - 4;
				int j = top - 2;
				this.removeButton.setPosition(i, j);
				this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);

				i -= duplicateButton.getWidth();
				duplicateButton.setPosition(i, j);
				duplicateButton.render(guiGraphics, mouseX, mouseY, partialTick);

				boolean current = Profiles.getInstance().getCurrent() == profile;
				loadButton.setMessage(current ? CURRENT_TEXT : LOAD_BUTTON_TITLE);
				loadButton.active = removeButton.active = !current;
				i -= loadButton.getWidth();
				this.loadButton.setPosition(i, j);
				this.loadButton.render(guiGraphics, mouseX, mouseY, partialTick);
				i -= exportButton.getWidth();
				exportButton.setPosition(i, j);
				exportButton.render(guiGraphics, mouseX, mouseY, partialTick);
				profileName.setWidth(i - left - 4);
				profileName.setPosition(left, j);
				profileName.render(guiGraphics, mouseX, mouseY, partialTick);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(profileName, exportButton, this.loadButton, duplicateButton, removeButton);
			}

			@Override
			public List<? extends Selectable> selectableChildren() {
				return List.of(profileName, exportButton, this.loadButton, duplicateButton, removeButton);
			}
		}

		public class NewEntry extends Entry {

			private final ButtonWidget addButton, importButton;

			public NewEntry() {
				this.addButton = ButtonWidget.builder(Text.translatable("profiles.profile.add"), button -> {
						int i = ProfilesList.this.children().indexOf(this);
						ProfilesList.this.children().add(Math.max(i - 1, 0), new ProfileEntry(Profiles.getInstance().newProfile(I18n.translate("profiles.profile.default_new_name"))));
						Profiles.getInstance().saveProfiles();
						setScrollAmount(getMaxScroll());
					}).positionAndSize(0, 0, 150, 20)
					.build();
				this.importButton = ButtonWidget.builder(Text.translatable("profiles.profile.import"), btn -> {
					btn.active = false;
					Profiles.getInstance().importProfiles().thenRun(() -> {
						btn.active = true;
						ProfilesList.this.reload();
					});
				}).build();
			}

			@Override
			public List<? extends Selectable> selectableChildren() {
				return List.of(addButton, importButton);
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				int i = getScrollbarPositionX() - width / 2 - 10 - addButton.getWidth() + 2;
				int j = top - 2;
				this.addButton.setPosition(i, j);
				this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
				this.importButton.setPosition(addButton.getXEnd() + 2, j);
				this.importButton.render(guiGraphics, mouseX, mouseY, partialTick);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(addButton, importButton);
			}
		}
	}
}
