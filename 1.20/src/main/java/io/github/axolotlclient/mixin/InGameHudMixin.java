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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldProperties;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
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

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void axolotlclient$onHudRender(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
		//noinspection ConstantValue
		if (!MinecraftClient.getInstance().options.hudHidden && !(AxoMinecraftClient.getInstance().br$getScreen() instanceof HudEditScreen)) {
			HudManager.getInstance().render(graphics, tickDelta);
		}
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderStatusEffect(GuiGraphics graphics, CallbackInfo ci) {
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCrosshair(GuiGraphics graphics, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			if (MinecraftClient.getInstance().options.debugEnabled && !hud.overridesF3()) {
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

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;overlayMessage:Lnet/minecraft/text/Text;", ordinal = 0, opcode = Opcodes.GETFIELD))
	public void axolotlclient$clearActionBar(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			if (overlayMessage == null || overlayRemaining <= 0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0))
	public int axolotlclient$getActionBar(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int color, Operation<Integer> original) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			hud.setActionBar(text, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return original.call(instance, renderer, text, x, y, color);
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customHotbar(float tickDelta, GuiGraphics graphics, CallbackInfo ci) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
	public int axolotlclient$setItemNamePos(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int color, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			x = hud.getRawTrueX() + (int) ((hud.getWidth() * hud.getScale()) - MinecraftClient.getInstance().textRenderer.getWidth(text)) / 2;
			y = hud.getRawTrueY() - 36 + (!MinecraftClient.getInstance().interactionManager.hasStatusBars() ? 14 : 0);
		}
		return original.call(instance, renderer, text, x, y, color);
	}

	@WrapOperation(method = {"renderMountJumpBar", "renderExperienceBar"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
	public void axolotlclient$moveHorseHealth(GuiGraphics instance, Identifier texture, int x, int y, int u, int v, int width, int height, Operation<Void> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			x = hud.getRawTrueX();
			y = hud.getRawTrueY() - 7;
		}
		original.call(instance, texture, x, y, u, v, width, height);
	}

	@WrapOperation(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I", opcode = Opcodes.GETFIELD))
	public int axolotlclient$moveXPBarHeight(InGameHud instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			return hud.getRawTrueY() + 22;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I", opcode = Opcodes.GETFIELD))
	public int axolotlclient$moveXPBarWidth(InGameHud instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			return hud.getRawTrueX() * 2 + hud.getWidth();
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I", opcode = Opcodes.GETFIELD))
	public int axolotlclient$moveStatusBarsHeight(InGameHud instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			return hud.getRawTrueY() + 22;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I", opcode = Opcodes.GETFIELD))
	public int axolotlclient$moveStatusBarsWidth(InGameHud instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud != null && hud.isEnabled()) {
			return hud.getRawTrueX() * 2 + hud.getWidth();
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderHealthBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProperties;isHardcore()Z"))
	private boolean axolotlclient$displayHardcoreHearts(WorldProperties instance, Operation<Boolean> original) {
		//noinspection OptionalGetWithoutIsPresent
		if (BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed()) return true;
		return original.call(instance);
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

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"))
	private int axolotlclient$dontShowArmor(PlayerEntity instance, Operation<Integer> original) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && !BedwarsMod.getInstance().displayArmor.get()) {
			return 0;
		}
		return original.call(instance);
	}

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
	private boolean hideChat(ChatHud instance, GuiGraphics graphics, int tickDelta, int mouseX, int mouseY) {
		return !AxolotlClient.config().hideChat.get();
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
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
