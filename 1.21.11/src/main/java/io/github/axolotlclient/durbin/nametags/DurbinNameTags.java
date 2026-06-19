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

package io.github.axolotlclient.durbin.nametags;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.util.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public final class DurbinNameTags {
	private static final DurbinNameTags INSTANCE = new DurbinNameTags();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final int MAX_LINES_PER_PLAYER = 6;
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

	private final AtomicBoolean refreshing = new AtomicBoolean(false);
	private final HttpClient http = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(4))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	private volatile boolean initialized;
	private volatile boolean enabled = true;
	private volatile String firebaseUrl = "";
	private volatile int autoRefreshSeconds = 300;
	private volatile long lastRefreshAttemptMillis;
	private volatile long lastSuccessfulRefreshMillis;
	private volatile String status = "Not loaded yet";
	private volatile Map<String, List<TagLine>> tags = Map.of();

	private DurbinNameTags() {
	}

	public static DurbinNameTags getInstance() {
		return INSTANCE;
	}

	public void init() {
		if (initialized) {
			return;
		}
		initialized = true;
		loadSettings();
		loadCache();
		refreshInBackground(false);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void toggleEnabled() {
		enabled = !enabled;
		status = enabled ? "Enabled" : "Disabled";
		saveSettings();
	}

	public void refreshNow() {
		refreshInBackground(true);
	}

	public List<Component> getComponentsFor(String username) {
		if (!enabled || username == null || username.isBlank()) {
			return List.of();
		}
		tickAutoRefresh();
		List<TagLine> lines = tags.get(normalizeName(username));
		if (lines == null || lines.isEmpty()) {
			return List.of();
		}
		ArrayList<Component> out = new ArrayList<>(lines.size());
		for (TagLine line : lines) {
			out.add(line.toComponent());
		}
		return out;
	}

	public int playerCount() {
		return tags.size();
	}

	public String statusText() {
		return refreshing.get() ? "Syncing..." : status;
	}

	public String shortFirebaseUrl() {
		if (firebaseUrl == null || firebaseUrl.isBlank()) {
			return "Not set";
		}
		String out = firebaseUrl.replace("https://", "").replace("http://", "");
		return out.length() > 48 ? out.substring(0, 45) + "..." : out;
	}

	public String lastSyncText() {
		if (lastSuccessfulRefreshMillis <= 0) {
			return "Never";
		}
		return TIME_FORMAT.format(Instant.ofEpochMilli(lastSuccessfulRefreshMillis));
	}

	public Path settingsPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("durbin-nametags.properties");
	}

	public Path cachePath() {
		return FabricLoader.getInstance().getConfigDir().resolve("durbin-nametags-cache.json");
	}

	private void tickAutoRefresh() {
		long now = System.currentTimeMillis();
		long delayMillis = Math.max(60, autoRefreshSeconds) * 1000L;
		if (now - lastRefreshAttemptMillis >= delayMillis) {
			refreshInBackground(false);
		}
	}

	private void refreshInBackground(boolean manual) {
		lastRefreshAttemptMillis = System.currentTimeMillis();
		if (!hasFirebaseUrl()) {
			status = tags.isEmpty() ? "No Firebase URL set" : "Using local cache";
			return;
		}
		if (!refreshing.compareAndSet(false, true)) {
			return;
		}
		status = manual ? "Manual sync started" : "Online sync started";
		CompletableFuture.runAsync(() -> {
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(firebaseUrl.trim()))
					.timeout(Duration.ofSeconds(8))
					.header("Accept", "application/json")
					.GET()
					.build();
				HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				if (response.statusCode() < 200 || response.statusCode() >= 300) {
					status = "Firebase error " + response.statusCode() + "; cache kept";
					return;
				}
				JsonElement json = JsonParser.parseString(response.body());
				Map<String, List<TagLine>> parsed = parseRoot(json);
				tags = parsed;
				lastSuccessfulRefreshMillis = System.currentTimeMillis();
				status = "Online sync OK (" + parsed.size() + " players)";
				saveCache(parsed);
			} catch (Exception e) {
				status = tags.isEmpty() ? "Offline/no cache" : "Offline, using cache";
				try {
					AxolotlClientCommon.getInstance().getLogger().warn("Durbin nametag sync failed, keeping local cache.", e);
				} catch (Exception ignored) {
				}
			} finally {
				refreshing.set(false);
			}
		});
	}

	private boolean hasFirebaseUrl() {
		return firebaseUrl != null && !firebaseUrl.isBlank() && firebaseUrl.startsWith("http");
	}

	private void loadSettings() {
		Properties props = new Properties();
		Path path = settingsPath();
		try {
			Files.createDirectories(path.getParent());
			if (Files.notExists(path)) {
				writeDefaultSettings(path);
			}
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				props.load(reader);
			}
			enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
			firebaseUrl = props.getProperty("firebase_url", "").trim();
			autoRefreshSeconds = parseInt(props.getProperty("auto_refresh_seconds", "300"), 300);
			if (autoRefreshSeconds < 60) {
				autoRefreshSeconds = 60;
			}
			status = "Settings loaded";
		} catch (IOException e) {
			status = "Settings error";
		}
	}

	private void saveSettings() {
		Path path = settingsPath();
		Properties props = new Properties();
		props.setProperty("enabled", Boolean.toString(enabled));
		props.setProperty("firebase_url", firebaseUrl == null ? "" : firebaseUrl);
		props.setProperty("auto_refresh_seconds", Integer.toString(autoRefreshSeconds));
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			props.store(writer, "Durbin Client Firebase nametag settings");
		} catch (IOException ignored) {
		}
	}

	private void writeDefaultSettings(Path path) throws IOException {
		String defaultFile = "# Durbin Client Firebase nametag settings\n"
			+ "# Paste your Firebase Realtime Database REST JSON URL here.\n"
			+ "# Example: https://your-project-id-default-rtdb.firebaseio.com/nametags.json\n"
			+ "enabled=true\n"
			+ "firebase_url=\n"
			+ "auto_refresh_seconds=300\n";
		Files.writeString(path, defaultFile, StandardCharsets.UTF_8);
	}

	private void loadCache() {
		Path path = cachePath();
		if (Files.notExists(path)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			Map<String, List<TagLine>> parsed = parseRoot(JsonParser.parseReader(reader));
			tags = parsed;
			status = "Loaded cache (" + parsed.size() + " players)";
		} catch (Exception e) {
			status = "Cache read error";
		}
	}

	private void saveCache(Map<String, List<TagLine>> parsed) {
		Path path = cachePath();
		try {
			Files.createDirectories(path.getParent());
			JsonObject root = new JsonObject();
			for (Map.Entry<String, List<TagLine>> entry : parsed.entrySet()) {
				JsonArray lines = new JsonArray();
				for (TagLine line : entry.getValue()) {
					JsonObject obj = new JsonObject();
					obj.addProperty("text", line.text());
					obj.addProperty("color", String.format(Locale.ROOT, "#%06X", line.color() & 0xFFFFFF));
					lines.add(obj);
				}
				root.add(entry.getKey(), lines);
			}
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(root, writer);
			}
		} catch (IOException ignored) {
		}
	}

	private Map<String, List<TagLine>> parseRoot(JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return Map.of();
		}
		JsonElement players = json;
		if (json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			if (obj.has("players")) {
				players = obj.get("players");
			} else if (obj.has("nametags")) {
				players = obj.get("nametags");
			}
		}
		if (!players.isJsonObject()) {
			return Map.of();
		}
		HashMap<String, List<TagLine>> out = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : players.getAsJsonObject().entrySet()) {
			String name = normalizeName(entry.getKey());
			List<TagLine> lines = parsePlayerLines(entry.getValue());
			if (!name.isBlank() && !lines.isEmpty()) {
				out.put(name, Collections.unmodifiableList(lines));
			}
		}
		return Collections.unmodifiableMap(out);
	}

	private List<TagLine> parsePlayerLines(JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return List.of();
		}
		JsonElement linesElement = json;
		if (json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			if (obj.has("enabled") && !obj.get("enabled").getAsBoolean()) {
				return List.of();
			}
			if (obj.has("lines")) {
				linesElement = obj.get("lines");
			} else if (obj.has("nametag")) {
				linesElement = obj.get("nametag");
			}
		}

		ArrayList<TagLine> lines = new ArrayList<>();
		if (linesElement.isJsonArray()) {
			for (JsonElement element : linesElement.getAsJsonArray()) {
				parseLine(element).ifPresent(lines::add);
			}
		} else if (linesElement.isJsonObject()) {
			List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(linesElement.getAsJsonObject().entrySet());
			entries.sort((a, b) -> a.getKey().compareTo(b.getKey()));
			for (Map.Entry<String, JsonElement> entry : entries) {
				parseLine(entry.getValue()).ifPresent(lines::add);
			}
		} else {
			parseLine(linesElement).ifPresent(lines::add);
		}

		if (lines.size() > MAX_LINES_PER_PLAYER) {
			return new ArrayList<>(lines.subList(0, MAX_LINES_PER_PLAYER));
		}
		return lines;
	}

	private Optional<TagLine> parseLine(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return Optional.empty();
		}
		String text;
		int color = 0xFFFFFF;
		if (element.isJsonPrimitive()) {
			text = element.getAsString();
		} else if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();
			text = getString(obj, "text", getString(obj, "label", ""));
			color = parseColor(getString(obj, "color", getString(obj, "colour", "#FFFFFF")), 0xFFFFFF);
		} else {
			return Optional.empty();
		}
		text = text == null ? "" : text.trim();
		if (text.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(new TagLine(text, color));
	}

	private String getString(JsonObject obj, String key, String fallback) {
		return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : fallback;
	}

	private int parseColor(String raw, int fallback) {
		if (raw == null || raw.isBlank()) {
			return fallback;
		}
		String color = raw.trim();
		try {
			if (color.startsWith("#")) {
				return Integer.parseInt(color.substring(1), 16) & 0xFFFFFF;
			}
			if (color.startsWith("0x") || color.startsWith("0X")) {
				return Integer.parseUnsignedInt(color.substring(2), 16) & 0xFFFFFF;
			}
			return Integer.parseUnsignedInt(color, 16) & 0xFFFFFF;
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private String normalizeName(String username) {
		return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
	}

	public record TagLine(String text, int color) {
		public Component toComponent() {
			String fixed = text.replace('&', '§');
			if (fixed.contains("§")) {
				return Util.formatFromCodes(fixed);
			}
			return Component.literal(fixed).withColor(color & 0xFFFFFF);
		}
	}
}
