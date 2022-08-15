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
package ladysnake.requiem.common;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.ObeliskDescriptor;
import ladysnake.requiem.api.v1.record.EntityPointer;
import ladysnake.requiem.api.v1.record.RecordPointer;
import ladysnake.requiem.api.v1.record.RecordType;
import ladysnake.requiem.core.RequiemCore;

public final class RequiemRecordTypes {
    public static final RecordType<Unit> RELEASED_SOUL = register("released_soul", Codec.unit(Unit.INSTANCE));
    public static final RecordType<Unit> RIFT_OBELISK = register("rift_obelisk", Codec.unit(Unit.INSTANCE));
    public static final RecordType<EntityPointer> ENTITY_REF = RecordType.register(RequiemCore.id("entity_ref"), EntityPointer.CODEC, EntityPointer::world);
    public static final RecordType<ObeliskDescriptor> OBELISK_REF = RecordType.register(Requiem.id("obelisk_ref"), ObeliskDescriptor.CODEC, ObeliskDescriptor::dimension);
    public static final RecordType<RecordPointer> PROJECTED_MORTICIAN = RecordType.register(Requiem.id("projected_mortician"), RecordPointer.CODEC);

    public static void init() {
        // NO-OP
    }

    private static <T> RecordType<T> register(String id, Codec<T> codec) {
        return RecordType.register(Requiem.id(id), codec);
    }
}
