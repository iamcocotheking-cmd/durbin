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

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.events.types.ScoreboardRenderEvent;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.PotionsHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.*;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.hud.chat.ChatHud;
import net.minecraft.client.gui.hud.in_game.InGameHud;
import net.minecraft.client.render.DeltaTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow
	private @Nullable Text overlayMessage;

	@Shadow
	private int overlayRemaining;

	@Inject(method = "render", at = @At(value = "TAIL"))
	private void onHudRender(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		//noinspection ConstantValue
		if (!MinecraftClient.getInstance().options.hudHidden && !(AxoMinecraftClient.getInstance().br$getScreen() instanceof HudEditScreen)) {
			HudManager.getInstance().render(graphics, tracker.getLastDuration());
		}
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderStatusEffect(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		if (!HudManager.getInstance().hudsEnabled()) return;
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCrosshair(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		if (!HudManager.getInstance().hudsEnabled()) return;
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (MinecraftClient.getInstance().inGameHud.getDebugHud().chartsVisible() && !hud.overridesF3()) {
				return;
			}
			ci.cancel();
		}
	}

	@Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderScoreboard(GuiGraphics graphics, ScoreboardObjective objective, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().accept(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderOverlayMessage", at = @At(value = "HEAD"))
	public void axolotlclient$clearActionBar(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (overlayMessage == null || overlayRemaining <= 0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@WrapOperation(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"))
	public int axolotlclient$getActionBar(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int width, int color, Operation<Integer> original) {
		if (!HudManager.getInstance().hudsEnabled()) return original.call(instance, renderer, text, x, y, width, color);
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			hud.setActionBar(text, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return original.call(instance, renderer, text, x, y, width, color);
		}
	}

	@WrapMethod(method = "renderHotbar")
	public void axolotlclient$customHotbar(GuiGraphics graphics, DeltaTracker tracker, Operation<Void> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (!hud.isHidden()) {
			graphics.br$pushMatrix();
			if (hud.isEnabled()) {
				graphics.br$translateMatrix(-graphics.getScaledWindowWidth() / 2f + 182 / 2f, -graphics.getScaledWindowHeight() + 22);
				graphics.br$translateMatrix(hud.getRawTrueX(), hud.getRawTrueY());
			}
			original.call(graphics, tracker);
			graphics.br$popMatrix();
		}
	}

	@WrapMethod(method = "renderExperienceLevel")
	public void axolotlclient$customHotbar$xpLevel(GuiGraphics graphics, DeltaTracker tracker, Operation<Void> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (!hud.isHidden()) {
			graphics.br$pushMatrix();
			if (hud.isEnabled()) {
				graphics.getMatrices().translate(-graphics.getScaledWindowWidth() / 2f, -graphics.getScaledWindowHeight() + 22, 0);
				graphics.getMatrices().translate(hud.getRawTrueX() + hud.getWidth() / 2f, hud.getRawTrueY(), 0);
			}
			original.call(graphics, tracker);
			graphics.getMatrices().pop();
		}
	}

	@Inject(
		method = "renderHealthBar",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;ceil(D)I")
	)
	public void axolotlclient$displayHardcoreHearts(GuiGraphics graphics, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci, @Local(ordinal = 1) LocalBooleanRef hardcore) {
		//noinspection OptionalGetWithoutIsPresent
		if (BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed()) {
			hardcore.set(true);
		}
	}

	@Expression("? == 0")
	@ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
	public boolean axolotlclient$dontHunger(boolean original) {
		if (original && BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().showHunger.get()) {
			return false;
		}
		return original;
	}

	@Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$removeVignette(GuiGraphics graphics, Entity entity, CallbackInfo ci) {
		if (AxolotlClient.config().removeVignette.get()) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;renderArmorBar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/entity/player/PlayerEntity;IIII)V"))
	private static void axolotlclient$dontShowArmor(GuiGraphics graphics, PlayerEntity player, int y, int uncappedMaxHealth, int cappedMaxHealth, int x, Operation<Void> original) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && !BedwarsMod.getInstance().displayArmor.get()) {
			return;
		}
		original.call(graphics, player, y, uncappedMaxHealth, cappedMaxHealth, x);
	}


	@WrapWithCondition(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/chat/ChatHud;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
	private boolean hideChat(ChatHud instance, GuiGraphics graphics, int tickDelta, int mouseX, int mouseY, boolean chatScreenOpen) {
		return !AxolotlClient.config().hideChat.get();
	}

	@WrapOperation(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
	private void translateTabOverlay(PlayerListHud instance, GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, Operation<Void> original) {
		var hud = (PlayerTabOverlayHud) HudManager.getInstance().get(PlayerTabOverlayHud.ID);
		if (!hud.isHidden()) {
			graphics.br$pushMatrix();
			if (hud.isEnabled()) {
				graphics.br$translateMatrix(-graphics.br$guiWidth() / 2f, -9);
				graphics.br$translateMatrix(hud.getRawTrueX() + hud.getTrueWidth()/2f, hud.getRawTrueY());
			}
			original.call(instance, graphics, scaledWindowWidth, scoreboard, objective);
			graphics.br$popMatrix();
		}
	}
}
