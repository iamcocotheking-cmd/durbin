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

package io.github.axolotlclient.bridge.mixin.item;

import java.util.List;
import java.util.stream.IntStream;

import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.item.AxoPlayerInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements AxoPlayerInventory {
	@Shadow
	public abstract ItemStack getMainHandStack();

	@Shadow
	public abstract ItemStack getStack(int slot);

	@Shadow
	public abstract int size();

	@Shadow
	public abstract ItemStack getArmorStack(int slot);

	@Shadow
	@Final
	public DefaultedList<ItemStack> armor;

	@Shadow
	@Final
	public DefaultedList<ItemStack> main;

	@Shadow
	@Final
	public DefaultedList<ItemStack> offHand;

	@Override
	public AxoItemStack br$getMainHand() {
		return getMainHandStack();
	}

	@Override
	public List<? extends AxoItemStack> br$getItems() {
		return IntStream.range(0, size())
			.mapToObj(this::getStack)
			.toList();
	}

	@Override
	public List<? extends AxoItemStack> br$getArmor() {
		return armor;
	}

	@Override
	public List<? extends AxoItemStack> br$getNonEquipmentItems() {
		return main.subList(9, 36);
	}

	@Override
	public AxoItemStack br$getOffHand() {
		return offHand.getFirst();
	}
}
