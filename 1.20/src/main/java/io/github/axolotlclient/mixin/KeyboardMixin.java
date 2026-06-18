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

import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import net.minecraft.client.Keyboard;
import net.minecraft.client.option.KeyBind;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;screenshotKey:Lnet/minecraft/client/option/KeyBind;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void actionForScreenshotCropKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		var mapping = (KeyBind) ScreenshotUtils.getInstance().screenshotCropBinding;
		if (mapping.matchesKey(key, scancode)) {
			mapping.br$click();
			ci.cancel();
		}
	}
}
