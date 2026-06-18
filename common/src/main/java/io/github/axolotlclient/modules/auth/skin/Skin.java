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
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.modules.auth.MSApi;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;

public interface Skin extends Asset {
	boolean classicVariant();

	void classicVariant(boolean classic);

	interface Local extends Skin, Asset.Local {

	}

	interface Online extends Skin, Asset.Online {
	}

	final class LocalSkin implements Local {
		public static final String META_DIR = ".meta";
		public static final String METADATA_SUFFIX = ".meta";
		public static final String CLASSIC_METADATA_KEY = "variant_classic";
		private boolean classic;
		private final Path file;
		private final byte[] data;
		private final String textureKey;

		public LocalSkin(boolean classic, Path file, byte[] image, String textureKey) {
			this.classic = classic;
			this.file = file;
			this.data = image;
			this.textureKey = textureKey;
		}

		@SuppressWarnings("unchecked")
		public static Map<String, Object> readMetadata(Path skinFile) throws IOException {
			var metadataFile = getMetadataFile(skinFile);
			if (!Files.exists(metadataFile)) return null;

			try (var in = Files.newInputStream(metadataFile)) {
				return (Map<String, Object>) GsonHelper.read(in);
			}
		}

		public static void writeMetadata(Path skinFile, Map<String, Object> metadata) throws IOException {
			var metadataFile = getMetadataFile(skinFile);
			Files.createDirectories(metadataFile.getParent());
			try (var out = Files.newOutputStream(metadataFile);
				 var writer = new OutputStreamWriter(out)) {
				GsonHelper.GSON.toJson(metadata, writer);
			}
		}

		public static void deleteMetadata(Path skinFile) throws IOException {
			Files.deleteIfExists(getMetadataFile(skinFile));
		}

		private static @NotNull Path getMetadataFile(Path skinFile) {
			return skinFile.resolveSibling(META_DIR).resolve(skinFile.getFileName().toString() + METADATA_SUFFIX);
		}

		@Override
		public boolean classicVariant() {
			return classic;
		}

		@Override
		public void classicVariant(boolean classic) {
			if (classic != this.classic) {
				try {
					var metadata = readMetadata(file());
					if (metadata == null) metadata = new HashMap<>();
					metadata.put(CLASSIC_METADATA_KEY, classic);
					writeMetadata(file(), metadata);
				} catch (IOException ignored) {
				}
			}
			this.classic = classic;
		}

		@Override
		public byte[] image() {
			return data;
		}

		@Override
		public boolean active() {
			return false;
		}

		@Override
		public CompletableFuture<MSApi.MCProfile> equip(MSApi api, Account account) {
			return api.uploadAndSetSkin(account, this);
		}

		@Override
		public Path file() {
			return file;
		}

		@Override
		public String sha256() {
			return textureKey;
		}
	}

	record Shared(Skin.Local local, MSApi.MCProfile.OnlineSkin online) implements Local, Online {

		@Override
		public boolean classicVariant() {
			return local.classicVariant();
		}

		@Override
		public void classicVariant(boolean classic) {
			local.classicVariant(classic);
		}

		@Override
		public byte[] image() {
			return local.image();
		}

		@Override
		public boolean active() {
			return online.active();
		}

		@Override
		public CompletableFuture<MSApi.MCProfile> equip(MSApi api, Account account) {
			return online.equip(api, account);
		}

		@Override
		public String sha256() {
			return online.sha256();
		}

		@Override
		public Path file() {
			return local.file();
		}

		@Override
		public String url() {
			return online.url();
		}

		@Override
		public boolean supportsDownload() {
			return true;
		}
	}

	static Skin getDefaultSkin(Account account) {
		return getDefaultSkin(UUIDHelper.fromUndashed(account.getUuid()));
	}

	static Skin getDefaultSkin(UUID uuid) {
		return DefaultSkin.get(uuid);
	}
}

record DefaultSkin(String name, String stringUrl, URI url, boolean wide) {
	private static final HttpClient client = NetworkUtil.createHttpClient();
	private static final Map<DefaultSkin, Skin> DEFAULT_SKIN_CACHE = new HashMap<>();

	DefaultSkin(String name, String url, boolean wide) {
		this(name, url, URI.create(url), wide);
	}

	private static final DefaultSkin[] SKINS = new DefaultSkin[]{
		new DefaultSkin("alex", "https://minecraft.wiki/images/Alex_%28slim_texture%29_JE3.png", false),
		new DefaultSkin("ari", "https://minecraft.wiki/images/Ari_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("efe", "https://minecraft.wiki/images/Efe_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("kai", "https://minecraft.wiki/images/Kai_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("makena", "https://minecraft.wiki/images/Makena_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("noor", "https://minecraft.wiki/images/Noor_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("steve", "https://minecraft.wiki/images/Steve_%28slim_texture%29_JE2.png", false),
		new DefaultSkin("sunny", "https://minecraft.wiki/images/Sunny_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("zuri", "https://minecraft.wiki/images/Zuri_%28slim_texture%29_JE1.png", false),
		new DefaultSkin("alex", "https://minecraft.wiki/images/Alex_%28classic_texture%29_JE2.png", true),
		new DefaultSkin("ari", "https://minecraft.wiki/images/Ari_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("efe", "https://minecraft.wiki/images/Efe_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("kai", "https://minecraft.wiki/images/Kai_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("makena", "https://minecraft.wiki/images/Makena_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("noor", "https://minecraft.wiki/images/Noor_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("steve", "https://minecraft.wiki/images/Steve_%28classic_texture%29_JE6.png", true),
		new DefaultSkin("sunny", "https://minecraft.wiki/images/Sunny_%28classic_texture%29_JE1.png", true),
		new DefaultSkin("zuri", "https://minecraft.wiki/images/Zuri_%28classic_texture%29_JE1.png", true)
	};

	@SuppressWarnings("UnstableApiUsage")
	public Skin getSkin() {
		return DEFAULT_SKIN_CACHE.computeIfAbsent(this, unused -> {
			var wrapper = new Skin.Online() {
				private DefaultSkin wrapped = DefaultSkin.this;
				private byte[] data = null;
				private String hash = null;

				private void load() {
					data = client.sendAsync(HttpRequest.newBuilder().GET().uri(wrapped.url()).build(), HttpResponse.BodyHandlers.ofByteArray())
						.thenApply(HttpResponse::body).join();
					hash = Hashing.sha256().hashBytes(data).toString();
				}

				@Override
				public boolean classicVariant() {
					return wrapped.wide();
				}

				@Override
				public void classicVariant(boolean classic) {
					int i = switch (name()) {
						case "alex" -> 0;
						case "ari" -> 1;
						case "efe" -> 2;
						case "kai" -> 3;
						case "makena" -> 4;
						case "noor" -> 5;
						case "steve" -> 6;
						case "sunny" -> 7;
						case "zuri" -> 8;
						default -> throw new IllegalStateException();
					};
					if (classic) i += 9;
					wrapped = SKINS[i];
					load();
				}

				@Override
				public byte[] image() {
					return data;
				}

				@Override
				public boolean active() {
					return false;
				}

				@Override
				public CompletableFuture<MSApi.MCProfile> equip(MSApi api, Account account) {
					return api.setSkin(account, this);
				}

				@Override
				public String sha256() {
					return hash;
				}

				@Override
				public String url() {
					return wrapped.stringUrl();
				}
			};
			wrapper.load();
			return wrapper;
		});
	}

	static Skin get(UUID uuid) {
		return SKINS[Math.floorMod(uuid.hashCode(), SKINS.length)].getSkin();
	}
}
