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
package ladysnake.requiem.common.remnant;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import javax.annotation.Nonnegative;
import java.util.UUID;

public class SimpleAttritionFocus implements AttritionFocus {
    protected final Object2IntOpenHashMap<UUID> attritionLevels = new Object2IntOpenHashMap<>();

    @Override
    public void addAttrition(UUID playerUuid, @Nonnegative int level) {
        this.attritionLevels.mergeInt(playerUuid, level, Integer::sum);
    }

    @Override
    public void applyAttrition(PlayerEntity player) {
        int attrition = this.attritionLevels.removeInt(player.getUuid());
        if (attrition > 0) {
            AttritionStatusEffect.apply(player, attrition);
        }
    }

    @Override
    public void transferAttrition(AttritionFocus other) {
        for (ObjectIterator<Object2IntMap.Entry<UUID>> iterator = this.attritionLevels.object2IntEntrySet().fastIterator(); iterator.hasNext(); ) {
            Object2IntMap.Entry<UUID> entry = iterator.next();
            other.addAttrition(entry.getKey(), entry.getIntValue());
            iterator.remove();
        }
    }

    @Override
    public boolean hasAttrition() {
        return !this.attritionLevels.isEmpty();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("attrition_levels")) {
            NbtList levels = tag.getList("attrition_levels", NbtType.COMPOUND);
            this.attritionLevels.clear();
            for (int i = 0; i < levels.size(); i++) {
                NbtCompound playerLevel = levels.getCompound(i);
                int level = playerLevel.getInt("level");
                this.attritionLevels.put(playerLevel.getUuid("player_uuid"), level);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (!this.attritionLevels.isEmpty()) {
            NbtList levels = new NbtList();
            for (Object2IntMap.Entry<UUID> entry : attritionLevels.object2IntEntrySet()) {
                NbtCompound level = new NbtCompound();
                level.putUuid("player_uuid", entry.getKey());
                level.putInt("level", entry.getIntValue());
                levels.add(level);
            }
            tag.put("attrition_levels", levels);
        }
    }
}
