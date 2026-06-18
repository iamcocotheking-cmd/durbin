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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Shadow
	public abstract void addCritParticles(Entity target);

	@Shadow
	public abstract void addEnchantedHitParticles(Entity target);

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void onAttack(Entity target, CallbackInfo ci) {
		Events.PLAYER_ATTACK.invoker().accept((PlayerEntity) (Object) this, target);
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;onAttacking(Lnet/minecraft/entity/Entity;)V"))
	private void onAttack(Entity target, CallbackInfo ci, @Local(ordinal = 2) boolean crit, @Local(ordinal = 1) float enchantedDamage) {
		if (Particles.getInstance().getAlwaysOn(ParticleTypes.CRIT) && !crit) {
			addCritParticles(target);
		}
		if (Particles.getInstance().getAlwaysOn(ParticleTypes.ENCHANTED_HIT) && enchantedDamage == 0) {
			addEnchantedHitParticles(target);
		}
	}
}
