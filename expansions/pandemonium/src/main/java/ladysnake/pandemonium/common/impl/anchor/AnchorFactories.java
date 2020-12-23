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
package ladysnake.pandemonium.common.impl.anchor;

import ladysnake.pandemonium.api.anchor.FractureAnchorFactory;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AnchorFactories {
    public static FractureAnchorFactory fromEntityUuid(UUID entityUuid) {
        return (manager, uuid, id) -> new EntityFractureAnchor(entityUuid, manager, uuid, id);
    }

    @Nonnull
    public static FractureAnchorFactory fromTag(CompoundTag anchorTag) {
        if (anchorTag.getString("AnchorType").equals("requiem:entity")) {
            return entityAnchorFromTag(anchorTag);
        }
        return trackedAnchorFromTag(anchorTag);
    }

    private static FractureAnchorFactory entityAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new EntityFractureAnchor(manager, serializedAnchor, id);
    }

    private static FractureAnchorFactory trackedAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new TrackedFractureAnchor(manager, serializedAnchor, id);
    }
}
