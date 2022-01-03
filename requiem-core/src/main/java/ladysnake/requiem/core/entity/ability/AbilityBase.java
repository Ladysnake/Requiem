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
package ladysnake.requiem.core.entity.ability;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.entity.ability.MobAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public abstract class AbilityBase<E extends LivingEntity> implements MobAbility<E> {
    protected final E owner;
    private final int cooldownTime;
    protected int cooldown;

    public AbilityBase(E owner, int cooldownTime) {
        this.owner = owner;
        this.cooldownTime = cooldownTime;
    }

    public void beginCooldown() {
        this.setCooldown(this.getCooldownTime());
    }

    public void setCooldown(int cooldown) {
        Preconditions.checkArgument(cooldown >= 0);

        if (this.cooldown != cooldown) {
            this.cooldown = cooldown;

            this.sync();

            if (cooldown == 0) {
                this.onCooldownEnd();
            }
        }
    }

    protected void sync() {
        E owner = this.owner;

        if (owner instanceof PlayerEntity) {
            MobAbilityController.KEY.sync(owner);
        } else {
            PlayerEntity possessor = ((Possessable) owner).getPossessor();
            if (possessor != null) {
                MobAbilityController.KEY.sync(possessor);
            }
        }
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    @Override
    public float getCooldownProgress() {
        if (this.getCooldownTime() == 0) return 1.0F;
        return 1.0f - (float) this.getCooldown() / this.getCooldownTime();
    }

    @Override
    public void update() {
        int cooldown = this.getCooldown();

        if (cooldown > 0 && !this.owner.world.isClient) {
            this.setCooldown(cooldown - 1);
        }
    }

    protected void onCooldownEnd() {
        // NO-OP
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeVarInt(this.getCooldown());
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        this.setCooldown(buf.readVarInt());
    }
}
