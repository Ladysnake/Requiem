/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.record;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UuidUtil;

import java.util.Optional;
import java.util.UUID;

public record RecordPointer(UUID uuid) {
    public RecordPointer(GlobalRecord record) {
        this(record.getUuid());
    }
    public static final Codec<RecordPointer> CODEC = UuidUtil.CODEC.xmap(RecordPointer::new, RecordPointer::uuid);

    public Optional<GlobalRecord> resolve(MinecraftServer server) {
        return GlobalRecordKeeper.get(server).getRecord(this.uuid());
    }

    public Optional<Entity> resolveEntity(MinecraftServer server, RecordType<EntityPointer> ptrType) {
        return this.resolve(server)
            .flatMap(r -> r.get(ptrType))
            .flatMap(ptr -> ptr.resolve(server));
    }
}
