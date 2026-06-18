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

package io.github.axolotlclient.mixin;

import io.github.axolotlclient.util.duck.NameTagSubmitExtension;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SubmitNodeStorage.NameTagSubmit.class)
public abstract class NameTagSubmitMixin implements NameTagSubmitExtension {
	@Unique
	private boolean hasBadge, forLevelHead;

	@Override
	public void axolotlclient$hasBadge(boolean b) {
		this.hasBadge = b;
	}

	@Override
	public boolean axolotlclient$hasBadge() {
		return hasBadge;
	}

	@Override
	public boolean axolotlclient$isForLevelHead() {
		return forLevelHead;
	}

	@Override
	public void axolotlclient$isForLevelHead(boolean b) {
		forLevelHead = b;
	}
}
