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

package io.github.axolotlclient.util.options.vanilla;

import java.util.Base64;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.screen.GraphicsEditorScreen;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.GraphicsWidget;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class AxoGraphicsWidget extends GraphicsWidget {
	private final GraphicsOption option;

	public AxoGraphicsWidget(int x, int y, int width, int height, GraphicsOption option) {
		super(x, y, width, height, option);
		this.option = option;
	}

	@Override
	public void onPress() {
		MinecraftClient.getInstance().setScreen(new AxoGraphicsEditorScreen(MinecraftClient.getInstance().currentScreen, this.option));
	}

	public static class AxoGraphicsEditorScreen extends GraphicsEditorScreen {

		public AxoGraphicsEditorScreen(Screen parent, GraphicsOption option) {
			super(parent, option);
		}

		@Override
		public void init() {
			super.init();

			int buttonX = gridX + maxGridWidth + 10;
			int buttonY = gridY + 60;
			var back = (ButtonWidget) children().get(children().size() - 1);
			remove(back);
			addDrawableChild(ButtonWidget.builder(Text.translatable("graphics.copy_text"),
					btn -> client.keyboard.setClipboard(Base64.getEncoder().encodeToString(option.get().getPixelData())))
				.position(buttonX, buttonY + 25).width(100).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("graphics.paste_text"),
					btn -> {
						try {
							option.get().setPixelData(Base64.getDecoder().decode(client.keyboard.getClipboard()));
						} catch (IllegalArgumentException e) {
							Notifications.getInstance().addStatus("graphics.paste_text.failed", "graphics.paste_text.failed.desc");
						}
					})
				.position(buttonX, buttonY + 50).width(100).build());
			addDrawableChild(back);
		}
	}
}
