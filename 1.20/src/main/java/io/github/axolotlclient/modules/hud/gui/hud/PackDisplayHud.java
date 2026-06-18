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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.text.component.TranslatableComponent;
import net.minecraft.util.Identifier;

import static io.github.axolotlclient.util.DrawUtil.*;

public class PackDisplayHud extends TextHudEntry {

	public static final Identifier ID = new Identifier(AxolotlClientCommon.MODID, "packdisplayhud");
	public final List<PackWidget> widgets = new ArrayList<>();
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private PackWidget placeholder;

	public PackDisplayHud() {
		super(120, 18, true);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float f) {
		DrawPosition pos = getContentPos();

		if (widgets.isEmpty())
			init();

		if (background.get()) {
			fillRect((GuiGraphics) graphics, getContentBounds(), backgroundColor.get());
		}

		if (outline.get())
			outlineRect((GuiGraphics) graphics, getContentBounds(), outlineColor.get());

		int y = pos.y() + 1;
		for (int i = widgets.size() - 1; i >= 0; i--) { // Badly reverse the order (I'm sure there are better ways to do this)
			widgets.get(i).render((GuiGraphics) graphics, pos.x() + 1, y);
			y += 17;
		}
		if (y - pos.y() != getContentHeight()) {
			setContentHeight(y - pos.y());
			onBoundsUpdate();
		}
	}

	@Override
	public void init() {
		var selected = MinecraftClient.getInstance().getResourcePackManager().getEnabledProfiles();
		var valid = selected.stream()
			.filter(p -> !(p.getDisplayName().asComponent() instanceof TranslatableComponent tr && tr.getKey().matches("pack\\.name\\.fabricMods?")))
			.toList();
		var listSize = valid.size();
		valid.forEach(profile -> {
			try (ResourcePack pack = profile.createResourcePack()) {

				if (listSize == 1) {
					widgets.add(createWidget(profile.getDisplayName(), pack));
				} else if (!pack.getName().equalsIgnoreCase("vanilla")) {
					widgets.add(createWidget(profile.getDisplayName(), pack));
				}

			} catch (Exception ignored) {
			}
		});

		AtomicInteger w = new AtomicInteger(20);
		widgets.forEach(packWidget -> {
			int textW = MinecraftClient.getInstance().textRenderer.getWidth(packWidget.getName()) + 20;
			if (textW > w.get())
				w.set(textW);
		});
		setContentWidth(w.get());

		setContentHeight(widgets.size() * 17 + 1);
		onBoundsUpdate();
	}

	private PackWidget createWidget(Text displayName, ResourcePack pack) throws IOException, AssertionError {
		ResourceIoSupplier<InputStream> supplier = pack.openRoot("pack.png");
		assert supplier != null;
		InputStream stream = supplier.get();
		if (stream != null) {
			Identifier id = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(ID.getPath(), new NativeImageBackedTexture(NativeImage.read(stream)));
			stream.close();
			return new PackWidget(displayName, id);
		}
		return null;
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float f) {
		boolean updateBounds = false;
		if (getContentHeight() < 18) {
			setContentHeight(18);
			updateBounds = true;
		}
		if (getContentWidth() < 56) {
			setContentWidth(56);
			updateBounds = true;
		}
		if (updateBounds) {
			onBoundsUpdate();
		}
		if (placeholder == null) {
			try (ResourcePack defaultPack = MinecraftClient.getInstance().getDefaultResourcePack()) {
				placeholder = createWidget(Text.of(defaultPack.getName()), defaultPack);
			} catch (Exception ignored) {
			}
		} else {
			placeholder.render((GuiGraphics) graphics, getContentPos().x() + 1, getContentPos().y() + 1);
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(iconsOnly);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public void update() {
		widgets.clear();
		init();
	}

	public class PackWidget {

		@Getter
		public final String name;
		private final Identifier texture;

		public PackWidget(Text name, Identifier id) {
			this.name = name.getString();
			texture = id;
		}

		public void render(GuiGraphics graphics, int x, int y) {
			if (!iconsOnly.get()) {
				RenderSystem.setShaderColor(1, 1, 1, 1F);
				graphics.drawTexture(texture, x, y, 0, 0, 16, 16, 16, 16);
			}
			drawString(graphics, name, x + 18, y + 16 / 2f - 9 / 2f, textColor.get().toInt(), shadow.get());
		}
	}
}
