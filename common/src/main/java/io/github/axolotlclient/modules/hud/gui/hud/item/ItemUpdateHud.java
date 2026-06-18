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

package io.github.axolotlclient.modules.hud.gui.hud.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.item.AxoItems;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.CommonUtil;
import io.github.axolotlclient.util.ItemUtil;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public class ItemUpdateHud extends TextHudEntry {
	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "itemupdatehud");

	private static final Supplier<List<ItemUtil.TimedItemStorage>> PLACEHOLDER_ADDED = CommonUtil.memoize(() -> List.of(
		new ItemUtil.TimedItemStorage(AxoItemStack.of(AxoItems.DIAMOND, 2), 0)
	));

	private static final Supplier<List<ItemUtil.TimedItemStorage>> PLACEHOLDER_REMOVED = CommonUtil.memoize(() -> List.of(
		new ItemUtil.TimedItemStorage(AxoItemStack.of(AxoItems.EMERALD, 3), 0)
	));

	private final IntegerOption timeout = new IntegerOption("timeout", 6, 1, 60);
	private final ColorOption bracketColor = new ColorOption("itemupdatehud.bracket_color", Colors.DARK_GRAY);
	private final BooleanOption hideIfEmpty = DefaultOptions.getHideIfEmpty();
	private List<ItemUtil.ItemStorage> oldItems = new ArrayList<>();
	private ArrayList<ItemUtil.TimedItemStorage> removed;
	private ArrayList<ItemUtil.TimedItemStorage> added;

	public ItemUpdateHud() {
		super(200, 11 * 6 - 2, true);
		removed = new ArrayList<>();
		added = new ArrayList<>();
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (client.br$getWorld() != null) {
			update();
		}
	}

	public void update() {
		this.removed = ItemUtil.removeOld(removed, timeout.get() * 1000);
		this.added = ItemUtil.removeOld(added, timeout.get() * 1000);
		updateAdded();
		updateRemoved();
		oldItems = ItemUtil.storageFromItem(ItemUtil.getItems(client));
	}

	private void updateAdded() {
		ItemUtil.compare(ItemUtil.storageFromItem(ItemUtil.getItems(client)), oldItems).stream()
			.map(ItemUtil.ItemStorage::timed)
			.forEach(stack -> {
				final var item = ItemUtil.getTimedItemFromItem(stack.stack, this.added);
				if (item.isPresent()) {
					item.get().incrementTimes(stack.times);
				} else {
					this.added.add(stack);
				}
			});

		this.added.sort((o1, o2) -> Float.compare(o1.getPassedTime(), o2.getPassedTime()));
	}

	private void updateRemoved() {
		ItemUtil.compare(oldItems, ItemUtil.storageFromItem(ItemUtil.getItems(client))).stream()
			.map(ItemUtil.ItemStorage::timed)
			.forEach(stack -> {
				final var item = ItemUtil.getTimedItemFromItem(stack.stack, this.removed);
				if (item.isPresent()) {
					item.get().incrementTimes(stack.times);
				} else {
					this.removed.add(stack);
				}
			});

		this.removed.sort((o1, o2) -> Float.compare(o1.getPassedTime(), o2.getPassedTime()));
	}

	private void renderInternal(AxoRenderContext context, List<ItemUtil.TimedItemStorage> added, List<ItemUtil.TimedItemStorage> removed) {
		final AxoText openBracket = AxoText.literal("[").br$color(bracketColor.get().toInt());
		final AxoText closingBracket = AxoText.literal("]").br$color(bracketColor.get().toInt());
		final int deltaY = context.br$getFont().br$getFontHeight() + 2;

		DrawPosition pos = getContentPos();
		int lastY = 1;
		int entryCount = 0;

		for (ItemUtil.ItemStorage item : added) {
			if (entryCount > 5) {
				return;
			}

			AxoText message = AxoText.literal("+ ")
				.br$append(openBracket)
				.br$append(item.times)
				.br$append(closingBracket)
				.br$append(" ")
				.br$append(item.stack.br$getHoverName());

			context.br$drawString(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_GREEN.toInt(), shadow.get());

			lastY += deltaY;
			entryCount++;
		}

		for (ItemUtil.ItemStorage item : removed) {
			if (entryCount > 5) {
				return;
			}

			AxoText message = AxoText.literal("- ")
				.br$append(openBracket)
				.br$append(item.times)
				.br$append(closingBracket)
				.br$append(" ")
				.br$append(item.stack.br$getHoverName());

			context.br$drawString(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_RED.toInt(), shadow.get());

			lastY += deltaY;
			entryCount++;
		}
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (hideIfEmpty.get() && added.isEmpty() && removed.isEmpty()) {
			return;
		}
		super.render(ctx, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		renderInternal(context, added, removed);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		if (BridgeVersion.V26_1.isCurrent()) {
			if (client.br$getWorld() == null) {
				var pos = getContentPos();
				context.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2, textColor.get());
				return;
			}
		}
		renderInternal(context, PLACEHOLDER_ADDED.get(), PLACEHOLDER_REMOVED.get());
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(hideIfEmpty);
		options.add(timeout);
		options.add(bracketColor);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}
}
