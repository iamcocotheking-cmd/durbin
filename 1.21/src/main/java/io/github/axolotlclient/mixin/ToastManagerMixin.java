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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.util.duck.ToastInstanceExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {

	@Shadow
	@Final
	private List<ToastManager.Entry<?>> visibleEntries;

	@Shadow
	@Final
	private static int field_39929;

	// Intentional @Redirect as we do not want to invoke the original method
	@Redirect(method = "method_45075", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;method_45073(I)I"))
	private int limitDisplayToScreenSize(ToastManager instance, int slots, @Local(argsOnly = true) Toast toast) {

		var toastHeight = toast.getHeight();
		var y = nextY();
		var wHeight = AxoWindow.getWindow().br$getScaledHeight();
		if (visibleEntries.size() < field_39929 && y <= wHeight / 2f && y + toastHeight < wHeight) {
			return 0;
		}

		return -1;
	}

	@Unique
	private int nextY() {
		return visibleEntries.stream().map(ToastManager.Entry::getInstance).mapToInt(Toast::getHeight).sum();
	}

	@Inject(method = "draw", at = @At(value = "INVOKE", target = "Ljava/util/List;removeIf(Ljava/util/function/Predicate;)Z"))
	private void preRender(GuiGraphics graphics, CallbackInfo ci) {
		int y = 0;
		for (ToastManager.Entry<?> e : visibleEntries) {
			var ex = ((ToastInstanceExtension) e);
			var lerpY = y + (ex.axolotlclient$getY() - y) / 2;
			ex.axolotlclient$setY(lerpY);
			y += e.getInstance().getHeight();
		}
	}
}
