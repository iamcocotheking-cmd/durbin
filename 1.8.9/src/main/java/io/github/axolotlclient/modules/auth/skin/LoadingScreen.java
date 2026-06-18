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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class LoadingScreen extends Screen {
	private final String description;
	private final String title;

	public LoadingScreen(String title, String description) {
		super();
		this.title = title;
		this.description = description;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		drawCenteredString(textRenderer, title, width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
		int centerX = width / 2;
		int centerY = height / 2;
		var text = description;
		textRenderer.draw(text, centerX - textRenderer.getWidth(text) / 2, centerY - 9, -1);
		String string = switch ((int) (Minecraft.getTime() / 300L % 4L)) {
			case 1, 3 -> "o O o";
			case 2 -> "o o O";
			default -> "O o o";
		};
		textRenderer.draw(string, centerX - textRenderer.getWidth(string) / 2, centerY + 9, 0xFF808080);
	}
}
