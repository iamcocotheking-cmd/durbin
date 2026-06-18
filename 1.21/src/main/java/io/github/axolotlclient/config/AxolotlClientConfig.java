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

package io.github.axolotlclient.config;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.RecreatableScreen;
import io.github.axolotlclient.AxolotlClientConfigCommon;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.config.screen.CreditsScreen;
import io.github.axolotlclient.config.screen.ProfilesScreen;
import io.github.axolotlclient.mixin.OverlayTextureAccessor;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class AxolotlClientConfig extends AxolotlClientConfigCommon {
	public final BooleanOption customSky = new BooleanOption("customSky", false);

	public final BooleanOption lowShield = new BooleanOption("lowShield", false);

	public final BooleanOption flatItems = new BooleanOption("flatItems", false);

	public final BooleanOption customLoadingScreenColor = new BooleanOption("custom_loading_bg_color", false);
	public final ColorOption loadingScreenColor = new ColorOption("loadingBgColor", new Color(239, 50, 61, 255));
	public final BooleanOption nightMode = new BooleanOption("nightMode", false);

	public final GenericOption openCredits = new GenericOption("Credits", "Open Credits", () ->
		MinecraftClient.getInstance().setScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen))
	);

	@Getter
	private final List<Option<?>> options = new ArrayList<>();

	public AxolotlClientConfig() {
		general.add(customLoadingScreenColor);
		general.add(loadingScreenColor);
		general.add(nightMode);
		general.add(openCredits);

		ConfigUI.getInstance().runWhenLoaded(() -> {
			general.getOptions().removeIf(o -> "configStyle".equals(o.getName()));
			String[] themes = ConfigUI.getInstance().getStyleNames().stream().map(s -> "configStyle." + s)
				.filter(s -> AxolotlClientCommon.SHADERS_SUPPORTED || !s.startsWith("rounded"))
				.toArray(String[]::new);
			if (themes.length > 1) {
				general.add(new StringArrayOption("configStyle", themes,
					"configStyle." + ConfigUI.getInstance().getCurrentStyle().getName(), s -> {
					ConfigUI.getInstance().setStyle(s.split("\\.")[1]);

					AxoMinecraftClient.getInstance().execute(() -> {
						Screen newScreen = RecreatableScreen.tryRecreate(MinecraftClient.getInstance().currentScreen);
						if (newScreen != null) {
							MinecraftClient.getInstance().setScreen(newScreen);
						}
					});
				}) {
					@Override
					public void fromSerializedValue(String value) {
						super.fromSerializedValue(value);
						changeListener.onChange(get());
					}
				});
				AxolotlClient.getInstance().getConfigManager().load();
			} else {
				AxolotlClient.getInstance().getConfigManager().load();
			}
		});

		rendering.add(customSky,
			lowShield,
			flatItems);

		general.add(new GenericOption("profiles.title", "profiles.configure", () ->
			MinecraftClient.getInstance().setScreen(new ProfilesScreen(MinecraftClient.getInstance().currentScreen))), false);
	}

	@Override
	protected void updateWindowTitle(boolean useCustom) {
		MinecraftClient.getInstance().updateWindowTitle();
	}

	@Override
	protected void updateHitColor(Color value) {
		//noinspection resource
		NativeImageBackedTexture texture = ((OverlayTextureAccessor) MinecraftClient.getInstance().gameRenderer.getOverlayTexture()).axolotlclient$getTexture();
		NativeImage nativeImage = texture.getImage();
		if (nativeImage != null) {
			nativeImage.fillRect(0, 0, 16, 8, ClientColors.ARGB.toABGR(ClientColors.ARGB.invertAlpha(value.toInt())));

			RenderSystem.activeTexture(33985);
			texture.bindTexture();
			nativeImage.upload(0, 0, 0, 0, 0,
				nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
			RenderSystem.activeTexture(33984);
		}
	}
}
