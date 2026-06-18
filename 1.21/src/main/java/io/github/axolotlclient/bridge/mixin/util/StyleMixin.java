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

package io.github.axolotlclient.bridge.mixin.util;

import io.github.axolotlclient.bridge.util.AxoText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Style.class)
public abstract class StyleMixin implements AxoText.Style {
	@Shadow
	public abstract Style withColor(TextColor par1);

	@Shadow
	public abstract Style withFormatting(Formatting par1);


	@Shadow
	public abstract Style withHoverEvent(HoverEvent par1);

	@Override
	public AxoText.Style br$color(AxoText.Color color) {
		return withFormatting(switch (color) {
			case BLACK -> Formatting.BLACK;
			case DARK_BLUE -> Formatting.DARK_BLUE;
			case DARK_GREEN -> Formatting.DARK_GREEN;
			case DARK_AQUA -> Formatting.DARK_AQUA;
			case DARK_RED -> Formatting.DARK_RED;
			case DARK_PURPLE -> Formatting.DARK_PURPLE;
			case GOLD -> Formatting.GOLD;
			case GRAY -> Formatting.GRAY;
			case DARK_GRAY -> Formatting.DARK_GRAY;
			case BLUE -> Formatting.BLUE;
			case GREEN -> Formatting.GREEN;
			case AQUA -> Formatting.AQUA;
			case RED -> Formatting.RED;
			case LIGHT_PURPLE -> Formatting.LIGHT_PURPLE;
			case YELLOW -> Formatting.YELLOW;
			case WHITE -> Formatting.WHITE;
		});
	}

	@Override
	public AxoText.Style br$color(int color) {
		return withColor(TextColor.fromRgb(color));
	}

	@Override
	public AxoText.Style br$tooltip(AxoText text) {
		return withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Text) text));
	}
}
