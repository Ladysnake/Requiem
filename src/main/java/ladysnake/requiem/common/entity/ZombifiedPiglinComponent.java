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
package ladysnake.requiem.common.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ZombifiedPiglinComponent implements Component {
    public static final ComponentKey<ZombifiedPiglinComponent> KEY =
        ComponentRegistry.getOrCreate(Requiem.id("zombified_piglin"), ZombifiedPiglinComponent.class);

    private @Nullable EntityType<?> originalPiglinType;
    private final ZombifiedPiglinEntity zombie;

    public ZombifiedPiglinComponent(ZombifiedPiglinEntity zombie) {
        this.zombie = zombie;
    }

    public @Nullable MobEntity createCuredEntity() {
        MobEntity curedEntity = createCuredEntity0();
        CurableEntityComponent.KEY.maybeGet(curedEntity).ifPresent(CurableEntityComponent::setCured);
        return curedEntity;
    }

    private @Nullable MobEntity createCuredEntity0() {
        @SuppressWarnings("unchecked") EntityType<? extends MobEntity> originalPiglinType = (EntityType<? extends MobEntity>) this.originalPiglinType;
        try {
            return this.zombie.method_29243(originalPiglinType, true);
        } catch (ClassCastException e) {
            Requiem.LOGGER.error("[Requiem] Invalid original piglin type", e);
        }
        return this.zombie.method_29243(EntityType.PIGLIN, true);
    }

    public EntityType<?> getOriginalPiglinType() {
        if (originalPiglinType == null) {
            return EntityType.PIGLIN;
        }
        return originalPiglinType;
    }

    public void setOriginalPiglinType(EntityType<?> originalPiglinType) {
        this.originalPiglinType = originalPiglinType;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("original_piglin_type")) {
            this.originalPiglinType = Registry.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(tag.getString("original_piglin_type"))).orElse(null);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if (this.originalPiglinType != null) {
            tag.putString("original_piglin_type", Registry.ENTITY_TYPE.getId(this.originalPiglinType).toString());
        }
    }
}
