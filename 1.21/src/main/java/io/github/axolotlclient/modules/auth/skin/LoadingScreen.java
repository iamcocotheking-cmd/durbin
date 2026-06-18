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

package io.github.axolotlclient.modules.auth.skin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.LoadingTextWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.Text;

public class LoadingScreen extends Screen {
	private final Text description;

	public LoadingScreen(Text title, Text description) {
		super(title);
		this.description = description;
	}

	@Override
	protected void init() {
		int headerHeight = 33;
		int contentHeight = height - headerHeight * 2;
		var titleWidget = new TextWidget(width / 2 - textRenderer.getWidth(getTitle()) / 2, headerHeight / 2 - textRenderer.fontHeight / 2, textRenderer.getWidth(getTitle()), textRenderer.fontHeight, getTitle(), textRenderer);
		addDrawableSelectableElement(titleWidget);

		var loadingPlaceholder = new LoadingTextWidget(textRenderer, description);
		loadingPlaceholder.setDimensionsAndPosition(width, contentHeight, 0,
			headerHeight);
		addDrawableSelectableElement(loadingPlaceholder);
	}
}
