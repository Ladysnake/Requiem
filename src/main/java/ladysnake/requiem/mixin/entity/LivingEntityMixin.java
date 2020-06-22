/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.mixin.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.common.util.DamageHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    @Shadow
    public abstract Hand getActiveHand();

    @Shadow
    public abstract void setStackInHand(Hand hand, ItemStack stack);

    @Shadow
    protected int playerHitTimer;

    @Shadow
    protected boolean dead;

    @Shadow
    protected PlayerEntity attackingPlayer;

    @Shadow
    public int deathTime;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Allows mobs to drop player-restricted loot when wielding a humanity weapon
     */
    @ModifyVariable(method = "drop", at = @At(value = "HEAD"), argsOnly = true)
    private DamageSource enableHumanity(DamageSource deathCause) {
        if (DamageHelper.getHumanityLevel(deathCause) > 0) {
            assert deathCause.getAttacker() != null : "Humanity implies attacker";
            PlayerEntity possessor = ((Possessable) deathCause.getAttacker()).getPossessor();
            if (possessor != null) {
                this.playerHitTimer = 100;
                this.attackingPlayer = possessor;
                DamageSource proxiedDamage = DamageHelper.createProxiedDamage(deathCause, possessor);
                if (proxiedDamage != null) {
                    return proxiedDamage;
                }
            }
        }
        return deathCause;
    }

    @Inject(
        method = "drop",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"),
        cancellable = true
    )
    private void fireDropEvent(DamageSource deathCause, CallbackInfo ci) {
        if (LivingEntityDropCallback.EVENT.invoker().onEntityDrop((LivingEntity) (Object) this, deathCause)) {
            ci.cancel();
        } else if (this instanceof MobResurrectable) {
            ((MobResurrectable) this).spawnResurrectionEntity();
        }
        // prevent xp drops if not enough humanity
        if (DamageHelper.getHumanityLevel(deathCause) == 1) {
            this.playerHitTimer = 0;
        }
    }

    /**
     * Marks possessed entities as the attacker for any damage caused by their possessor,
     * or the opposite when the killer wields a Humanity enchanted weapon.
     *
     * @param source damage dealt
     * @param amount amount of damage dealt
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private DamageSource proxyDamage(DamageSource source, DamageSource s, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity) {
            DamageSource newSource = DamageHelper.tryProxyDamage(source, (LivingEntity) attacker);
            if (newSource != null) {
                return newSource;
            }
        }
        return source;
    }

    /**
     * Fixes a bug in vanilla minecraft that gives back {@link ItemStack#finishUsing(World, LivingEntity)}'s result
     * even after an inventory drop
     */
    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearActiveItem()V"))
    private void dropUsedItemAsSoul(CallbackInfo ci) {
        if (this instanceof RequiemPlayer && ((RequiemPlayer) this).asRemnant().isIncorporeal() && !world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.dropStack(this.getStackInHand(this.getActiveHand()));
            this.setStackInHand(this.getActiveHand(), ItemStack.EMPTY);
        }
    }
}
