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

package io.github.axolotlclient.bridge.mixin.world;

import java.util.Collections;
import java.util.List;

import io.github.axolotlclient.bridge.entity.AxoPlayer;
import io.github.axolotlclient.bridge.math.Vec3;
import io.github.axolotlclient.bridge.world.AxoWorld;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldMixin implements AxoWorld, WorldAccess {
	@Shadow
	public abstract long getTimeOfDay();

	@Shadow
	public abstract DimensionType getDimension();

	@Shadow
	public abstract RegistryKey<World> getRegistryKey();

	@Override
	public long br$getTimeOfDay() {
		return getTimeOfDay();
	}

	@Override
	public List<? extends AxoPlayer> br$getPlayers() {
		return Collections.unmodifiableList(getPlayers());
	}

	@Override
	public String br$getBiomeName(Vec3 pos) {
		var biome = getBiome(new BlockPos(MathHelper.floor(pos.x()), MathHelper.floor(pos.y()), MathHelper.floor(pos.z()))).unwrap().left().orElse(null);
		if (biome == null) {
			return I18n.translate("coordshud.unknown_biome");
		}
		return biome.getValue().br$getAsFriendlyString();
	}

	@Override
	public boolean br$isOverworld() {
		return getRegistryKey() == World.OVERWORLD;
	}

	@Override
	public boolean br$isNether() {
		return getRegistryKey() == World.NETHER;
	}
}
