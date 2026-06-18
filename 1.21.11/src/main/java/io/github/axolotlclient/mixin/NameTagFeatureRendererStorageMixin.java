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

import java.util.List;

import io.github.axolotlclient.util.duck.NameTagFeatureRendererStorageExtension;
import io.github.axolotlclient.util.duck.NameTagSubmitExtension;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NameTagFeatureRenderer.Storage.class)
public abstract class NameTagFeatureRendererStorageMixin implements NameTagFeatureRendererStorageExtension {

	@Shadow
	@Final
	List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsNormal;

	@Override
	public void axolotlclient$lastNameTagSubmitHasBadge() {
		((NameTagSubmitExtension) (Object) nameTagSubmitsNormal.getLast()).axolotlclient$hasBadge(true);
	}

	@Override
	public void axolotlclient$lastNameTagSubmitIsLevelHead() {
		((NameTagSubmitExtension) (Object) nameTagSubmitsNormal.getLast()).axolotlclient$isForLevelHead(true);
	}
}
