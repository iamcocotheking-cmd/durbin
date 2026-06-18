/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.SubtitlesHudHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.SubtitlesHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitlesHud.class)
public abstract class SubtitlesHudMixin {

	@Shadow
	@Final
	private List<SubtitlesHud.SubtitleEntry> entries;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 1))
	private void subtitlesHudPosition(GuiGraphics graphics, CallbackInfo ci, @Local(ordinal = 1) int width) {
		var subtitlesHud = (SubtitlesHudHud) HudManager.getInstance().get(SubtitlesHudHud.ID);
		if (subtitlesHud.isEnabled() && !entries.isEmpty()) {
			var lineHeight = 9;
			var h = entries.size() * (lineHeight + 1) +2;
			var w = width + 4;
			if (!(MinecraftClient.getInstance().currentScreen instanceof HudEditScreen)) {
				var updated = false;
				if (h != subtitlesHud.getContentHeight()) {
					subtitlesHud.setContentHeight(h);
					updated = true;
				}
				if (w != subtitlesHud.getContentWidth()) {
					subtitlesHud.setContentWidth(w);
					updated = true;
				}
				if (updated) {
					subtitlesHud.onBoundsUpdate();
				}
			}
			subtitlesHud.renderHud(graphics, 0);
			subtitlesHud.scale(graphics);
			graphics.br$translateMatrix(subtitlesHud.getContentX(), subtitlesHud.getContentY());
			graphics.br$translateMatrix((float) -graphics.getScaledWindowWidth() + w, (float) -(graphics.getScaledWindowHeight() - 35) + h - (lineHeight + 1) / 2f - 1);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
	private void switchEntryBackground(GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		var subtitlesHud = (SubtitlesHudHud) HudManager.getInstance().get(SubtitlesHudHud.ID);
		if (!subtitlesHud.isEnabled() || subtitlesHud.vanillaEntryBackground.get()) {
			original.call(instance, x1, y1, x2, y2, color);
		}
	}

	@WrapMethod(method = "render")
	private void wrapExtraction(GuiGraphics graphics, Operation<Void> original) {
		graphics.br$pushMatrix();
		original.call(graphics);
		graphics.br$popMatrix();
	}
}
