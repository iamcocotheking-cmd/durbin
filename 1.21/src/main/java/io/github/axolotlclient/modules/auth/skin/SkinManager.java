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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class SkinManager {

	private final Set<AxoIdentifier> loadedTextures = new ConcurrentSkipListSet<>(Comparator.comparing(Object::toString));

	public Skin read(Path p) {
		return read(p, true);
	}

	public Skin read(Path p, boolean fix) {
		boolean slim;
		String sha256;
		try {
			var in = Files.readAllBytes(p);
			sha256 = Hashing.sha256().hashBytes(in).toString();
			try (var img = NativeImage.read(in)) {
				int width = img.getWidth();
				int height = img.getHeight();
				if (width != 64) return null;
				if (height == 32) {
					if (fix) {
						try (var img2 = remapTexture(img)) {
							img2.writeFile(p);
						}
					}
					slim = false;
				} else if (height != 64) {
					return null;
				} else {
					slim = ClientColors.ARGB.alpha(img.getPixelColor(50, 16)) == 0;
				}
				var metadata = Skin.LocalSkin.readMetadata(p);
				if (metadata != null && metadata.containsKey(Skin.LocalSkin.CLASSIC_METADATA_KEY)) {
					slim = !(boolean) metadata.get(Skin.LocalSkin.CLASSIC_METADATA_KEY);
				}
			}
			return new Skin.LocalSkin(!slim, p, in, sha256);
		} catch (Exception e) {
			AxolotlClientCommon.getInstance().getLogger().warn("Failed to probe skin: ", e);
		}
		return null;
	}

	private static NativeImage remapTexture(NativeImage skinImage) {
		boolean legacySkin = skinImage.getHeight() == 32;
		if (legacySkin) {
			NativeImage nativeImage = new NativeImage(64, 64, true);
			nativeImage.copyFrom(skinImage);
			skinImage.close();
			skinImage = nativeImage;
			nativeImage.fillRect(0, 32, 64, 32, 0);
			nativeImage.copyRectangle(4, 16, 16, 32, 4, 4, true, false);
			nativeImage.copyRectangle(8, 16, 16, 32, 4, 4, true, false);
			nativeImage.copyRectangle(0, 20, 24, 32, 4, 12, true, false);
			nativeImage.copyRectangle(4, 20, 16, 32, 4, 12, true, false);
			nativeImage.copyRectangle(8, 20, 8, 32, 4, 12, true, false);
			nativeImage.copyRectangle(12, 20, 16, 32, 4, 12, true, false);
			nativeImage.copyRectangle(44, 16, -8, 32, 4, 4, true, false);
			nativeImage.copyRectangle(48, 16, -8, 32, 4, 4, true, false);
			nativeImage.copyRectangle(40, 20, 0, 32, 4, 12, true, false);
			nativeImage.copyRectangle(44, 20, -8, 32, 4, 12, true, false);
			nativeImage.copyRectangle(48, 20, -16, 32, 4, 12, true, false);
			nativeImage.copyRectangle(52, 20, -8, 32, 4, 12, true, false);
		}

		stripAlpha(skinImage, 0, 0, 32, 16);
		if (legacySkin) {
			stripColor(skinImage, 32, 0, 64, 32);
		}

		stripAlpha(skinImage, 0, 16, 64, 32);
		stripAlpha(skinImage, 16, 48, 48, 64);
		return skinImage;
	}

	@SuppressWarnings("SameParameterValue")
	private static void stripColor(NativeImage image, int x1, int y1, int x2, int y2) {
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				int k = image.getPixelColor(x, y);
				if ((k >> 24 & 0xFF) < 128) {
					return;
				}
			}
		}

		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				image.setPixelColor(x, y, image.getPixelColor(x, y) & 16777215);
			}
		}
	}

	private static void stripAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				image.setPixelColor(x, y, image.getPixelColor(x, y) | 0xFF000000);
			}
		}
	}

	public AxoIdentifier loadSkin(Skin skin) {
		var rl = AxoIdentifier.of(AxolotlClientCommon.MODID, "skins/" + skin.sha256());
		if (loadedTextures.contains(rl)) {
			return rl;
		}

		try {
			var tex = new NativeImageBackedTexture(NativeImage.read(skin.image()));
			MinecraftClient.getInstance().getTextureManager().registerTexture((Identifier) rl, tex);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		loadedTextures.add(rl);
		return rl;
	}

	public AxoIdentifier loadCape(Cape cape) {
		var rl = AxoIdentifier.of(AxolotlClientCommon.MODID, "capes/" + cape.id());
		if (loadedTextures.contains(rl)) {
			return rl;
		}

		try {
			var tex = new NativeImageBackedTexture(NativeImage.read(cape.image()));
			MinecraftClient.getInstance().getTextureManager().registerTexture((Identifier) rl, tex);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		loadedTextures.add(rl);
		return rl;
	}

	public void releaseAll() {
		loadedTextures.forEach(id -> MinecraftClient.getInstance().getTextureManager().destroyTexture((Identifier) id));
		loadedTextures.clear();
	}
}
