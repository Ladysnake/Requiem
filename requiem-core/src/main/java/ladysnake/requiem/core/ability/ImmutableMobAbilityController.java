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
package ladysnake.requiem.core.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class ImmutableMobAbilityController<T extends LivingEntity> implements MobAbilityController {
    private List<MobAbility<? super T>> abilities;
    private IndirectAbility<? super T> indirectAttack;
    private IndirectAbility<? super T> indirectInteraction;
    private DirectAbility<? super T, ?> directAttack;
    private DirectAbility<? super T, ?> directInteraction;

    /**
     * Deferred initialization constructor
     *
     * <p>Required because some abilities require perusing through a mob's brain, which is not yet initialized during component initialization
     */
    public ImmutableMobAbilityController() {
        super();
    }

    public ImmutableMobAbilityController(T owner, MobAbilityConfig<? super T> config) {
        this.init(owner, config);
    }

    public void init(T owner, MobAbilityConfig<? super T> config) {
        this.directAttack = config.getDirectAbility(owner, AbilityType.ATTACK);
        this.directInteraction = config.getDirectAbility(owner, AbilityType.INTERACT);
        this.indirectAttack = config.getIndirectAbility(owner, AbilityType.ATTACK);
        this.indirectInteraction = config.getIndirectAbility(owner, AbilityType.INTERACT);
        this.abilities = Arrays.asList(this.directAttack, this.directInteraction, this.indirectAttack, this.indirectInteraction);
    }

    @Override
    public double getRange(AbilityType type) {
        return this.getDirect(type).getRange();
    }

    @Override
    public boolean canTarget(AbilityType type, Entity target) {
        return this.canTarget(target, this.getDirect(type));
    }

    @Override
    public ActionResult useDirect(AbilityType type, Entity target) {
        return this.use(this.getDirect(type), target);
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        return this.getIndirect(type).trigger();
    }

    @Override
    public float getCooldownProgress(AbilityType type) {
        return this.getDirect(type).getCooldownProgress();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        for (MobAbility<? super T> ability : this.abilities) {
            ability.writeToPacket(buf);
        }
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        for (MobAbility<? super T> ability : this.abilities) {
            ability.readFromPacket(buf);
        }
    }

    @Override
    public void tick() {
        for (MobAbility<? super T> ability : this.abilities) {
            ability.update();
        }
    }

    @Override
    public Identifier getIconTexture(AbilityType type) {
        return this.getDirect(type).getIconTexture();
    }

    private <E extends Entity> boolean canTarget(Entity target, DirectAbility<? super T, E> ability) {
        Class<E> targetType = ability.getTargetType();
        if (targetType.isInstance(target)) {
            return ability.canTarget(targetType.cast(target));
        }
        return false;
    }

    private <E extends Entity> ActionResult use(DirectAbility<? super T, E> ability, Entity target) {
        Class<E> targetType = ability.getTargetType();
        if (targetType.isInstance(target)) {
            return ability.trigger(targetType.cast(target));
        }
        return ActionResult.FAIL;
    }

    private DirectAbility<? super T, ?> getDirect(AbilityType type) {
        return switch (type) {
            case ATTACK -> this.directAttack;
            case INTERACT -> this.directInteraction;
        };
    }

    private IndirectAbility<? super T> getIndirect(AbilityType type) {
        return switch (type) {
            case ATTACK -> this.indirectAttack;
            case INTERACT -> this.indirectInteraction;
        };
    }
}
