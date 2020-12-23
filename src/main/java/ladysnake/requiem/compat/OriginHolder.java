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
package ladysnake.requiem.compat;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponents;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class OriginHolder implements Component {
    public static final ComponentKey<OriginHolder> KEY = ComponentRegistry.getOrCreate(Requiem.id("origin_holder"), OriginHolder.class);

    protected @Nullable CompoundTag originData;

    public void storeOrigin(PlayerEntity player) {
        if (!player.world.isClient && this.originData == null) {
            try {
                CompoundTag originData = new CompoundTag();
                ModComponents.ORIGIN.get(player).writeToNbt(originData);
                this.originData = originData;
            } catch (RuntimeException e) {
                Requiem.LOGGER.error("[Requiem] Failed to serialize origin data", e);
            }
        }
    }

    public void restoreOrigin(PlayerEntity player) {
        if (!player.world.isClient && this.originData != null) {
            OriginComponent originComponent = ModComponents.ORIGIN.get(player);
            CompoundTag backup = Util.make(new CompoundTag(), originComponent::writeToNbt);
            try {
                originComponent.readFromNbt(this.originData);
            } catch (RuntimeException e) {
                Requiem.LOGGER.error("[Requiem] Failed to deserialize origin data", e);
                originComponent.readFromNbt(backup);
            }
            this.originData = null;
            ModComponents.ORIGIN.sync(player);
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("originData")) {
            this.originData = tag.getCompound("originData");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if (this.originData != null) {
            tag.put("originData", this.originData);
        }
    }
}
