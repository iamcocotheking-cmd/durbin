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

package io.github.axolotlclient.bridge.mixin.internal;

import java.util.Objects;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.entity.effect.AxoStatusEffect;
import io.github.axolotlclient.bridge.entity.effect.AxoStatusEffectInstance;
import io.github.axolotlclient.bridge.impl.AxoSpriteImpl;
import io.github.axolotlclient.bridge.internal.PlatformImplInternal;
import io.github.axolotlclient.bridge.item.AxoItem;
import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.key.AxoKey;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoSprite;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.bridge.scores.AxoTeam;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoProfiler;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.util.keybinds.KeyBinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("OverwriteModifiers")
@Mixin(value = PlatformImplInternal.class, remap = false)
public abstract class PlatformImplInternalMixin {
	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static @Nullable AxoWindow getWindow() {
		return MinecraftClient.getInstance().getWindow();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoMinecraftClient getMinecraftClientInstance() {
		return MinecraftClient.getInstance();
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoProfiler getProfiler() {
		return MinecraftClient.getInstance().getProfiler();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTranslatedString(String nameKey, Object[] args) {
		return I18n.translate(nameKey, args);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoItemStack createItemStack(AxoItem item, int count) {
		return new ItemStack((Item) item, count);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static long getMeasuringTimeMs() {
		return Util.getMeasuringTimeMs();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static int getCurrentFps() {
		return MinecraftClientAccessor.axolotlclient$getCurrentFps();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoKeybinding createKeyBinding(AxoKey defaultKey, String name) {
		int code = ((InputUtil.Key) Objects.requireNonNullElse(defaultKey, AxoKeys.KEY_UNKNOWN)).getKeyCode();
		final var binding = new KeyBind(name, code, "category.axolotlclient");
		KeyBinds.getInstance().register(binding);
		return binding;
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoIdentifier createIdentifier(String ns, String path) {
		return new Identifier(ns, path);
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoIdentifier parseIdentifier(String id) {
		return new Identifier(id);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoStatusEffectInstance createStatusEffectInstance(AxoStatusEffect effect, int duration) {
		return new StatusEffectInstance((StatusEffect) effect, duration);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoSprite createTexture(GraphicsOption option) {
		return new AxoSpriteImpl.Config(option);
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoText.Mutable createEmpty() {
		return Text.empty();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoText.Mutable createLiteral(String text) {
		return Text.literal(text);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoText.Mutable createTranslatable(String key, Object... args) {
		return Text.translatable(key, args);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static int tickCount() {
		return MinecraftClient.getInstance().inGameHud.getTicks();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String stripText(String text) {
		return Formatting.strip(text);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTabNameFor(AxoPlayerListEntry player) {
		// Inlined PlayerListHud#getDisplayName to avoid StackOverflowError due to mixin
		PlayerListEntry p = (PlayerListEntry) player;
		if (p.getDisplayName() != null) {
			MutableText name = p.getDisplayName().copy();
			return Formatting.strip((p.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name).getString());
		}
		MutableText name = Team.decorateName(p.getScoreboardTeam(), Text.literal(p.getProfile().getName()));
		return Formatting.strip((p.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name).getString());
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static void setTabListHeader(AxoText text) {
		MinecraftClient.getInstance().inGameHud.getPlayerListHud().setHeader((Text) text);
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTeamMemberDisplayName(AxoTeam team, String s) {
		return Team.decorateName((AbstractTeam) team, Text.literal(s)).getString();
	}
}
