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

package io.github.axolotlclient.modules.screenshotUtils;

import java.util.OptionalLong;

import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.MathUtil;
import io.github.axolotlclient.util.duck.ToastExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.jetbrains.annotations.NotNull;

public class ScreenshotToast implements Toast, ToastExtension {
	private static final int TOAST_HEIGHT = 100;
	private static final int DISPLAY_TIME_MILLIS = 3500;
	private final ImageInstance image;
	private final int width;

	@SuppressWarnings("resource")
	public ScreenshotToast(ImageInstance instance) {
		this.image = instance;
		this.width = ((int) (instance.image().getWidth() * (TOAST_HEIGHT / (float) instance.image().getHeight())));
	}

	@Override
	public int getWidth() {
		return 2 + width;
	}

	@Override
	public int getHeight() {
		return 2 + TOAST_HEIGHT;
	}

	@Override
	public Visibility draw(GuiGraphics guiGraphics, @NotNull ToastManager toastManager, long visibilityTime) {
		var color = ScreenshotUtils.getInstance().toastBorderColor.get().toInt();
		guiGraphics.fill(0, 0, getWidth(), getHeight(), color);
		guiGraphics.drawTexture(image.id(), 1, 1, 0, 0, width, TOAST_HEIGHT, width, TOAST_HEIGHT);
		float prog = MathUtil.lerp(MathUtil.clamp(visibilityTime / 300f, 0f, 1f), 1f, 0f);
		guiGraphics.br$fillRect(1, 1, width, TOAST_HEIGHT, ClientColors.ARGB.color(prog * ClientColors.ARGB.alphaFloat(color), color));
		var time = DISPLAY_TIME_MILLIS * toastManager.method_48221();
		return visibilityTime < time ? Visibility.SHOW : Visibility.HIDE;
	}

	@Override
	public OptionalLong axolotlclient$animationDuration() {
		return OptionalLong.of(300);
	}
}
