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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {

	@Shadow
	private Component header;
	@Shadow
	private Component footer;
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapMethod(method = "getNameForDisplay")
	private Component nickHider(PlayerInfo entry, Operation<Component> original) {
		var orig = original.call(entry);
		if (minecraft.player == null) {
			return orig;
		}
		if (entry.getProfile().equals(minecraft.player.getGameProfile()) && NickHider.getInstance().hideOwnName.get()) {
			return (Component) NickHider.getInstance().editComponent(orig, entry.getProfile().name(), NickHider.getInstance().hiddenNameSelf.get());
		} else if (!entry.getProfile().equals(minecraft.player.getGameProfile()) &&
			NickHider.getInstance().hideOtherNames.get()) {
			return (Component) NickHider.getInstance().editComponent(orig, entry.getProfile().name(), NickHider.getInstance().hiddenNameOthers.get());
		}
		return orig;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I", ordinal = 0))
	private int axolotlclient$moveName(Font instance, FormattedText text, Operation<Integer> original, @Local PlayerInfo entry) {
		var width = original.call(instance, text);
		if (AxolotlClient.config().showBadges.get()) {
			if (AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED || UserRequest.getOnline(entry.getProfile().id().toString())) {
				width += 9;
			}
		}
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).numericalPing.get()) {
			width += (instance.width(String.valueOf(entry.getLatency())) - 10);
		}
		return width;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0))
	public void axolotlclient$moveName2(GuiGraphics instance, Font renderer, Component text, int x, int y, int color, Operation<Integer> original, @Local PlayerInfo entry) {
		if (AxolotlClient.config().showBadges.get() &&
			(AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME ||
				AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED)) {
			if (UserRequest.getOnline(entry.getProfile().id().toString())) {
				instance.blit(RenderPipelines.GUI_TEXTURED, (Identifier) AxolotlClientCommon.BADGE_PATH, x, y, 0, 0, 8, 8,8, 8, 8, 8);
				x += 9;
			} else if (AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_NAME_ALIGNED) {
				x += 9;
			}
		}
		original.call(instance, renderer, text, x, y, color);
	}

	@Inject(method = "renderPingIcon", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(GuiGraphics graphics, int width, int x, int y, PlayerInfo entry, CallbackInfo ci) {
		if (AxolotlClient.config().showBadges.get() && AxolotlClient.config().tabBadgeMode.get() == AxolotlClientConfigCommon.TabBadgeMode.BEFORE_PING
			&& UserRequest.getOnline(entry.getProfile().id().toString())) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, (Identifier) AxolotlClientCommon.BADGE_PATH, x + width - 11 - 9, y, 0, 0, 8, 8, 8, 8, 8, 8);
		}
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().customTabList.get()
			&& BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).renderNumericPing(graphics, width, x, y, entry)) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
	private boolean showPlayerHeads$1(Minecraft instance, Operation<Boolean> original) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$1(Connection instance, Operation<Boolean> original) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@Definition(id = "header", field = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;")
	@Expression("this.header != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkHeaderOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showHeader.get();
	}

	@Definition(id = "footer", field = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;footer:Lnet/minecraft/network/chat/Component;")
	@Expression("this.footer != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkFooterOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showFooter.get();
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;IIIZZI)V"),
		index = 5)
	private boolean axolotlclient$renderHatLayer(boolean drawHat) {
		return drawHat || ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).alwaysShowHeadLayer.get();
	}

	@Inject(method = "renderTablistScore", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"),
		cancellable = true)
	private void axolotlclient$renderCustomScoreboardObjective(Objective objective, int y, PlayerTabOverlay.ScoreDisplayEntry entry, int startX, int endX, UUID uuid, GuiGraphics graphics, CallbackInfo ci) {
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

		game.renderCustomScoreboardObjective(graphics, entry.name().getString(), entry.score(), y, endX);

		ci.cancel();
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 1)
	public int axolotlclient$changeWidth(int value) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return value;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return value;
		}
		if (BedwarsMod.getInstance().blockLatencyIcon() &&
			(BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
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
		BedwarsPlayer player = game.getPlayer(entry.getProfile().id()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(Component.literal(player.getTabListDisplay()));
	}

	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
	public List<PlayerInfo> axolotlclient$overrideSortedPlayers(List<PlayerInfo> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		List<?> players = BedwarsMod.getInstance().getGame().orElseThrow().getTabPlayerList(Collections.unmodifiableList(original));
		if (players == null) {
			return original;
		}
		return (List<PlayerInfo>) players;
	}

	@Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeHeader(Component header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabHeader.get()) {
			return;
		}
		this.header = (Component) BedwarsMod.getInstance().getGame().orElseThrow().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Component footer, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabFooter.get()) {
			return;
		}
		this.footer = (Component) BedwarsMod.getInstance().getGame().orElseThrow().getBottomBarText();
		ci.cancel();
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;", ordinal = 1))
	private List<FormattedCharSequence> captureFooterLines(Font instance, FormattedText text, int width, Operation<List<FormattedCharSequence>> original, @Share("footerLines") LocalRef<List<FormattedCharSequence>> footerLines) {
		var lines = original.call(instance, text, width);
		footerLines.set(lines);
		return lines;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0))
	private void renderBackgroundWithOptions$header(GuiGraphics graphics, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci, @Local(index = 18) int t, @Local(index = 17) int s, @Local(ordinal = 2) List<FormattedCharSequence> headerLines, @Share("footerLines") LocalRef<List<FormattedCharSequence>> footerLines, @Local(index = 11) int o) {
		renderBackgroundWithOptions(graphics, width, t, s, headerLines, footerLines.get(), o);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1))
	private void renderBackgroundWithOptions$noHeader(GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci, @Local(index = 18) int t, @Local(index = 17) int s, @Local(ordinal = 2) List<FormattedCharSequence> headerLines, @Share("footerLines") LocalRef<List<FormattedCharSequence>> footerLines, @Local(index = 11) int o) {
		if (headerLines == null) {
			renderBackgroundWithOptions(graphics, scaledWindowWidth, t, s, null, footerLines.get(), o);
		}
	}

	@Unique
	private void renderBackgroundWithOptions(GuiGraphics g, int width, int maxLineWidth, int yyo, List<FormattedCharSequence> headerLines, List<FormattedCharSequence> footerLines, int rows) {
		var tablist = (PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID);
		if (!tablist.isEnabled() || tablist.backgroundDisabled()) {
			return;
		}
		var x = width / 2 - maxLineWidth / 2 - 1;
		var y = yyo - 1;
		var x2 = width / 2 + maxLineWidth / 2 + 1;
		var headerLineCount = headerLines != null ? headerLines.size() : 0;
		var footerLineCount = footerLines != null ? footerLines.size() : 0;
		var y2 = yyo + (headerLineCount + rows + footerLineCount) * g.br$getFont().br$getFontHeight() + (footerLineCount > 0 ? 1 : 0) + (headerLineCount > 0 ? 1 : 0);
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

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V")))
	private void modifyBackground$footer(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		applyBackgroundOptions(instance, x1, y1, x2, y2, color, original);
	}
}
