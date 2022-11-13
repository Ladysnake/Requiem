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
package ladysnake.requiem.core.record;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.api.v1.event.minecraft.MobConversionCallback;
import ladysnake.requiem.api.v1.event.requiem.EntityRecordUpdateCallback;
import ladysnake.requiem.api.v1.record.EntityPointer;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.record.RecordType;
import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Maintains a {@link GlobalRecord} describing an entity, allowing it to be referenced across dimensions
 */
public final class EntityPositionClerk implements ServerTickingComponent {
    public static final ComponentKey<EntityPositionClerk> KEY = ComponentRegistry.getOrCreate(RequiemCore.id("entity_clerk"), EntityPositionClerk.class);

    public static EntityPositionClerk get(LivingEntity entity) {
        return KEY.get(entity);
    }

    public static void registerCallbacks() {
        MobConversionCallback.EVENT.register(EntityPositionClerk::transferRecord);
    }

    private final RecordType<EntityPointer> pointerType;
    private final LivingEntity entity;
    private final GlobalRecordKeeper recordKeeper;
    private @Nullable UUID recordId;

    public EntityPositionClerk(RecordType<EntityPointer> pointerType, LivingEntity entity) {
        this.pointerType = pointerType;
        this.entity = entity;
        this.recordKeeper = GlobalRecordKeeper.get(entity.world);
    }

    public Optional<GlobalRecord> getRecord() {
        if (this.recordId == null) return Optional.empty();
        return this.recordKeeper.getRecord(this.recordId);
    }

    public GlobalRecord getOrCreateRecord() {
        Optional<GlobalRecord> existingRecord = this.getRecord();

        if (existingRecord.isPresent()) {
            return existingRecord.get();
        }

        GlobalRecord newRecord = GlobalRecordKeeper.get(this.entity.getWorld()).createRecord();
        this.linkWith(newRecord);
        return newRecord;
    }

    @ApiStatus.Internal
    public void linkWith(GlobalRecord record) {
        this.recordId = record.getUuid();
        this.updateRecord(record);
    }

    /**
     * Unlinks the attached record without destroying it
     */
    @ApiStatus.Internal
    public void unlink() {
        this.getRecord().ifPresent(globalRecord -> globalRecord.remove(this.pointerType));
        this.recordId = null;
    }

    @ApiStatus.Internal
    public static void transferRecord(LivingEntity from, LivingEntity to) {
        EntityPositionClerk original = get(from);
        Optional<GlobalRecord> record = original.getRecord();

        if (record.isPresent()) {
            original.unlink();
            get(to).linkWith(record.get());
        }
    }

    public void destroy() {
        this.getRecord().ifPresent(GlobalRecord::invalidate);
    }

    @Override
    public void serverTick() {
        Optional<GlobalRecord> record = this.getRecord();

        if (record.isPresent()) {
            this.updateRecord(record.get());
        } else {
            this.recordId = null;
        }
    }

    private void updateRecord(GlobalRecord record) {
        if (this.entity.getHealth() <= 0.0F) {
            record.remove(this.pointerType);    // not destroying the record yet
        } else {
            Optional<EntityPointer> ptr = record.get(this.pointerType);

            if (ptr.isEmpty() || !entity.getPos().equals(ptr.get().pos())) {
                record.put(this.pointerType, new EntityPointer(entity));
            }

            EntityRecordUpdateCallback.EVENT.invoker().update(entity, record);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.containsUuid("record")) {
            this.recordKeeper.getRecord(tag.getUuid("record")).ifPresent(this::linkWith);
        } else if (tag.getType("refs") == NbtElement.LIST_TYPE) {
            // Pre 2.0.0-beta.14 backward compatibility
            NbtList refs = tag.getList("refs", NbtElement.COMPOUND_TYPE);

            if (!refs.isEmpty()) {
                NbtCompound refNbt = refs.getCompound(0);
                this.recordKeeper.getRecord(refNbt.getUuid("uuid")).ifPresent(this::linkWith);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.recordId != null) {
            tag.putUuid("record", this.recordId);
        }
    }
}
