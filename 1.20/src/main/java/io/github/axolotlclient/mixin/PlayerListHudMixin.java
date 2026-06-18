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

package io.github.axolotlclient.mixin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfigCommon;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hud.HudManagerCommon;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.PlayerTabOverlayHud;
import io.github.axolotlclient.modules.hypixel.NickHider;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

	@Shadow
	private Text header;
	@Shadow
	private Text footer;
	@Shadow
	@Final
	private MinecraftClient client;

	@WrapMethod(method = "getPlayerName")
	private Text nickHider(PlayerListEntry entry, Operation<Text> original) {
		var orig = original.call(entry);
		if (client.player == null) {
			return orig;
		}
		if (entry.getProfile().equals(client.player.getGameProfile()) && NickHider.getInstance().hideOwnName.get()) {
			return (Text) NickHider.getInstance().editComponent(orig, entry.getProfile().getName(), NickHider.getInstance().hiddenNameSelf.get());
		} else if (!entry.getProfile().equals(client.player.getGameProfile()) &&
			NickHider.getInstance().hideOtherNames.get()) {
			return (Text) NickHider.getInstance().editComponent(orig, entry.getProfile().getName(), NickHider.getInstance().hiddenNameOthers.get());
		}
		return orig;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I", ordinal = 0))
	private int axolotlclient$moveName(TextRenderer instance, StringVisitable text, Operation<Integer> original, @Local PlayerListEntry entry) {
		var width = original.call(instance, text);
		if (AxolotlClient.config().showBadges.get()) {
			if (AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED || UserRequest.getOnline(entry.getProfile().getId().toString())) {
				width += 9;
			}
		}
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).numericalPing.get())
			width += (instance.getWidth(String.valueOf(entry.getLatency())) - 10);
		return width;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
	public int axolotlclient$moveName2(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int color, @Local PlayerListEntry entry) {
		if (AxolotlClient.config().showBadges.get() &&
			(AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME ||
				AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED)) {
			if (UserRequest.getOnline(entry.getProfile().getId().toString())) {
				RenderSystem.setShaderColor(1, 1, 1, 1);
				instance.drawTexture((Identifier) AxolotlClientCommon.BADGE_PATH, x, y, 8, 8, 0, 0, 8, 8, 8, 8);
				x += 9;
			} else if (AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED) {
				x += 9;
			}
		}
		return instance.drawShadowedText(renderer, text, x, y, color);
	}

	@Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(GuiGraphics graphics, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
		if (AxolotlClient.config().showBadges.get() && AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_PING
			&& UserRequest.getOnline(entry.getProfile().getId().toString())) {
			RenderSystem.setShaderColor(1, 1, 1, 1);
			graphics.drawTexture((Identifier) AxolotlClientCommon.BADGE_PATH, x + width - 11 - 9, y, 8, 8, 0, 0, 8, 8, 8, 8);
		}
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().customTabList.get()
			&& BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).renderNumericPing(graphics, width, x, y, entry)) {
			ci.cancel();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
	private boolean showPlayerHeads$1(MinecraftClient instance) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return instance.isInSingleplayer();
		}
		return false;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$1(ClientConnection instance) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return instance.isEncrypted();
		}
		return false;
	}

	@Definition(id = "header", field = "Lnet/minecraft/client/gui/hud/PlayerListHud;header:Lnet/minecraft/text/Text;")
	@Expression("this.header != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkHeaderOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showHeader.get();
	}

	@Definition(id = "footer", field = "Lnet/minecraft/client/gui/hud/PlayerListHud;footer:Lnet/minecraft/text/Text;")
	@Expression("this.footer != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkFooterOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showFooter.get();
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/util/Identifier;IIIZZ)V"), index = 5)
	private boolean axolotlclient$renderHatLayer(boolean drawHat) {
		return drawHat || ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).alwaysShowHeadLayer.get();
	}

	@Inject(
		method = "renderScoreboardObjective",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
		),
		cancellable = true
	)
	private void axolotlclient$renderCustomScoreboardObjective(
		ScoreboardObjective objective, int y, String player, int startX, int endX, UUID uuid, GuiGraphics graphics, CallbackInfo ci
	) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}

		game.renderCustomScoreboardObjective(graphics, player, objective.getScoreboard().getPlayerScore(player, objective).getScore(), y, endX);

		ci.cancel();
	}

	@ModifyVariable(
		method = "render",
		at = @At(
			value = "STORE"
		),
		ordinal = 5
	)
	public int axolotlclient$changeWidth(int value) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return value;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return value;
		}
		if (BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerListEntry playerEntry, CallbackInfoReturnable<Text> cir) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return;
		}
		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null || !game.isStarted()) {
			return;
		}
		BedwarsPlayer player = game.getPlayer(playerEntry.getProfile().getId()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(Text.of(player.getTabListDisplay()));
	}

	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
	public List<PlayerListEntry> axolotlclient$overrideSortedPlayers(List<PlayerListEntry> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return original;
		}
		List<?> players = BedwarsMod.getInstance().getGame().orElseThrow().getTabPlayerList(Collections.unmodifiableList(original));
		if (players == null) {
			return original;
		}
		return (List<PlayerListEntry>) players;
	}

	@Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeHeader(Text header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabHeader.get()) {
			return;
		}
		this.header = (Text) BedwarsMod.getInstance().getGame().orElseThrow().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Text footer, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabFooter.get()) {
			return;
		}
		this.footer = (Text) BedwarsMod.getInstance().getGame().orElseThrow().getBottomBarText();
		ci.cancel();
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;wrapLines(Lnet/minecraft/text/StringVisitable;I)Ljava/util/List;", ordinal = 1))
	private List<OrderedText> captureFooterLines(TextRenderer instance, StringVisitable text, int width, Operation<List<OrderedText>> original, @Share("footerLines") LocalRef<List<OrderedText>> footerLines) {
		var lines = original.call(instance, text, width);
		footerLines.set(lines);
		return lines;
	}

	@Definition(id = "list2", local = @Local(type = List.class, ordinal = 1))
	@Expression("list2 != null")
	@Inject(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void renderBackgroundWithOptions(GuiGraphics g, int width, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci, @Local(ordinal = 10) int r, @Local(ordinal = 9) int q, @Local(ordinal = 1) List<OrderedText> headerLines, @Share("footerLines") LocalRef<List<OrderedText>> footerLines, @Local(ordinal = 4) int m) {
		var tablist = (PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID);
		if (!tablist.isEnabled() || tablist.backgroundDisabled()) {
			return;
		}
		var x = width / 2 - r / 2 - 1;
		var y = q - 1;
		var x2 = width / 2 + r / 2 + 1;
		var headerLineCount = headerLines != null ? headerLines.size() : 0;
		var footerLineCount = footerLines.get() != null ? footerLines.get().size() : 0;
		var y2 = q + (headerLineCount + m + footerLineCount) * g.br$getFont().br$getFontHeight() + (footerLineCount > 0 ? 1 : 0) + (headerLineCount > 0 ? 1 : 0);
		int padding = tablist.getBackgroundPadding();
		x -= padding;
		x2 += padding;
		y -= padding;
		y2 += padding;
		if (y < 0) {
			y2 -= y;
			y = 0;
		}
		if (tablist.hasRoundBackground()) {
			var rounding = Math.min(tablist.getBackgroundRounding(), Math.min(x2 - x, y2 - y) / 2f);
			g.br$fillRectRound(x, y, x2 - x, y2 - y,
				tablist.customBackgroundColor.get() ? tablist.getBackgroundColor().toInt() : Integer.MIN_VALUE,
				rounding);

			if (tablist.hasOutline()) {
				g.br$outlineRectRound(x, y, x2 - x, y2 - y, tablist.getOutlineColor(), rounding);
			}
		} else {
			g.br$fillRect(x, y, x2 - x, y2 - y,
				tablist.customBackgroundColor.get() ? tablist.getBackgroundColor().toInt() : Integer.MIN_VALUE);
			if (tablist.hasOutline()) {
				g.br$outlineRect(x, y, x2 - x, y2 - y, tablist.getOutlineColor());
			}
		}
	}

	@Unique
	private void applyBackgroundOptions(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		var tablist = (PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID);
		if (!tablist.isEnabled()) {
			original.call(instance, x1, y1, x2, y2, color);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), slice = @Slice(to = @At(value = "CONSTANT", args = "intValue=553648127")))
	private void modifyBackground$headerAndMain(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		applyBackgroundOptions(instance, x1, y1, x2, y2, color, original);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderLatencyIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/network/PlayerListEntry;)V")))
	private void modifyBackground$footer(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		applyBackgroundOptions(instance, x1, y1, x2, y2, color, original);
	}
}
