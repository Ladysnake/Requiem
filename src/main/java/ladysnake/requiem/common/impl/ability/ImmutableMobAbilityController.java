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
package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ImmutableMobAbilityController<T extends LivingEntity> implements MobAbilityController {
    private final IndirectAbilityContainer<? super T> indirectAttack;
    private final IndirectAbilityContainer<? super T> indirectInteraction;
    private final DirectAbilityContainer<? super T, ?> directAttack;
    private final DirectAbilityContainer<? super T, ?> directInteraction;
    private final T owner;

    public ImmutableMobAbilityController(MobAbilityConfig<? super T> config, T owner) {
        this.directAttack = new DirectAbilityContainer<>(config.getDirectAbility(owner, AbilityType.ATTACK));
        this.directInteraction = new DirectAbilityContainer<>(config.getDirectAbility(owner, AbilityType.INTERACT));
        this.indirectAttack = new IndirectAbilityContainer<>(config.getIndirectAbility(owner, AbilityType.ATTACK));
        this.indirectInteraction = new IndirectAbilityContainer<>(config.getIndirectAbility(owner, AbilityType.INTERACT));
        this.owner = owner;
    }

    @Override
    public double getRange(AbilityType type) {
        return this.getDirect(type).getRange();
    }

    @Override
    public boolean canTarget(AbilityType type, Entity target) {
        return this.getDirect(type).canTrigger(target);
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        if (this.getDirect(type).trigger(target)) {
            MobAbilityController.KEY.sync(this.owner);
            return true;
        }
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        if (this.getIndirect(type).trigger()) {
            MobAbilityController.KEY.sync(this.owner);
            return true;
        }
        return false;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.directAttack.getCooldown());
        buf.writeVarInt(this.directInteraction.getCooldown());
        buf.writeVarInt(this.indirectAttack.getCooldown());
        buf.writeVarInt(this.indirectInteraction.getCooldown());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.directAttack.setCooldown(buf.readVarInt());
        this.directInteraction.setCooldown(buf.readVarInt());
        this.indirectAttack.setCooldown(buf.readVarInt());
        this.indirectInteraction.setCooldown(buf.readVarInt());
    }

    @Override
    public void tick() {
        this.directAttack.update();
        this.indirectAttack.update();
        this.directInteraction.update();
        this.indirectInteraction.update();
    }

    private DirectAbilityContainer<? super T, ?> getDirect(AbilityType type) {
        switch (type) {
            case ATTACK:
                return this.directAttack;
            case INTERACT:
                return this.directInteraction;
        }
        throw new IllegalArgumentException("Unrecognized ability type " + type);
    }

    private IndirectAbilityContainer<? super T> getIndirect(AbilityType type) {
        switch (type) {
            case ATTACK:
                return this.indirectAttack;
            case INTERACT:
                return this.indirectInteraction;
        }
        throw new IllegalArgumentException("Unrecognized ability type " + type);
    }
}
