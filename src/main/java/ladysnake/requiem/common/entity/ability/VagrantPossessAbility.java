/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.entity.ability.DirectAbilityBase;
import ladysnake.requiem.core.remnant.VagrantInteraction;
import ladysnake.requiem.core.remnant.VagrantInteractionRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class VagrantPossessAbility extends DirectAbilityBase<PlayerEntity, LivingEntity> {

    public static final int POSSESSION_RANGE = 5;
    public static final int POSSESSION_COOLDOWN = 8;

    private @Nullable LivingEntity target;
    private @Nullable VagrantInteraction interaction;

    public VagrantPossessAbility(PlayerEntity owner) {
        super(owner, POSSESSION_COOLDOWN, POSSESSION_RANGE, LivingEntity.class);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (super.canTarget(target)) {
            this.interaction = VagrantInteractionRegistryImpl.INSTANCE.getAction(target, this.owner);
        } else this.interaction = null;
        return this.interaction != null;
    }

    @Override
    protected boolean run(LivingEntity target) {
        this.target = target;
        if (this.owner.world.isClient && this.owner == MinecraftClient.getInstance().player) {
            RequiemClient.instance().fxRenderer().beginFishEyeAnimation(target);
        }
        target.world.playSound(this.owner, target.getX(), target.getY(), target.getZ(), RequiemSoundEvents.EFFECT_POSSESSION_ATTEMPT, SoundCategory.PLAYERS, 2, 0.6f);
        this.beginCooldown();
        return true;
    }

    @Override
    protected void onCooldownEnd() {
        if (this.owner.world.isClient && this.owner == MinecraftClient.getInstance().player) {
            RequiemClient.instance().fxRenderer().onPossessionAck();
        } else if (this.interaction != null && this.target != null && !this.target.isRemoved() && this.target.isAlive()) {
            this.interaction.action().accept(this.target, this.owner);
        }
        this.interaction = null;
        this.target = null;
    }

    @Override
    public Identifier getIconTexture() {
        return this.interaction != null ? this.interaction.icon() : VagrantInteractionRegistryImpl.POSSESSION_ICON;
    }
}
