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

import com.mojang.serialization.DataResult;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.record.RecordType;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.util.DataResults;
import ladysnake.requiem.core.util.MoreStreams;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonRecordKeeper implements GlobalRecordKeeper {
    private final Map<UUID, GlobalRecord> records = new HashMap<>();
    private final Deque<GlobalRecord> invalidationQueue = new ArrayDeque<>();

    protected void addRecord(GlobalRecord anchor) {
        if (this.checkValidityForInsertion(anchor)) {
            records.put(anchor.getUuid(), anchor);
        }
    }

    protected void invalidate(GlobalRecord record) {
        this.invalidationQueue.push(record);
    }

    protected boolean checkValidityForInsertion(GlobalRecord anchor) {
        return true;
    }

    protected <T> boolean checkFieldValidity(GlobalRecord record, RecordType<T> type, T value) {
        return true;
    }

    @Override
    public Collection<GlobalRecord> getRecords() {
        // Quick copy to avoid CME's
        return Arrays.asList(this.records.values().toArray(new GlobalRecord[0]));
    }

    @Override
    public Stream<GlobalRecord> stream() {
        return Arrays.stream(this.records.values().toArray(new GlobalRecord[0])).filter(GlobalRecord::isValid);
    }

    @Override
    public void tick() {
        while (!this.invalidationQueue.isEmpty()) {
            GlobalRecord invalidated = this.invalidationQueue.pop();
            if (invalidated.isValid()) throw new IllegalStateException();
            this.records.remove(invalidated.getUuid());
        }
    }

    @Override
    public GlobalRecord createRecord() {
        GlobalRecordImpl record = new GlobalRecordImpl(this, UUID.randomUUID());
        this.addRecord(record);
        return record;
    }

    @Override
    public Optional<GlobalRecord> getRecord(UUID anchorUuid) {
        return Optional.ofNullable(this.records.get(anchorUuid)).filter(GlobalRecord::isValid);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        tag.getList("records", NbtElement.COMPOUND_TYPE).stream()
            .mapMulti(MoreStreams.instanceOf(NbtCompound.class))
            .forEach(nbt -> DataResults.ifPresentOrElse(
                this.deserialize(nbt),
                this::addRecord,
                partialResult -> RequiemCore.LOGGER.error("Invalid save data - failed to decode global entity: %s %s".formatted(partialResult.message(), nbt))
            ));
    }

    private DataResult<GlobalRecord> deserialize(NbtCompound anchorTag) {
        return DataResult.unbox(DataResult.instance().apply2(
            (uuid, data) -> new GlobalRecordImpl(this, uuid, data),
            DataResults.tryGet(() -> anchorTag.getUuid(GlobalRecordImpl.ANCHOR_UUID_NBT)),
            this.deserializeRawData(anchorTag.getCompound("data"))
        ));
    }

    private DataResult<Map<RecordType<?>, Object>> deserializeRawData(NbtCompound nbtData) {
        return nbtData.getKeys().stream()
            // Step 1: equivalent of nbtData.entrySet() (sadly such a method doesn't exist)
            .map(key -> Map.entry(key, Objects.requireNonNull(nbtData.get(key))))
            // Step 2: DataResult go brr
            .map(entry -> this.tryParseRecordType(entry.getKey()).flatMap(type -> deserializeRawDatum(type, entry.getValue())))
            // Step 3: box every data result in a stream
            .map(result -> result.map(Stream::of))
            // Step 4: functional programming go brr
            .reduce(DataResult.success(Stream.of()), (s1, s2) -> s1.apply2(Stream::concat, s2))
            // Step 5: boom, we got a map
            .map(s -> s.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private DataResult<RecordType<?>> tryParseRecordType(String key) {
        // Pre-2.0.0-beta.14 backward compatibility
        if (key.equals("requiem:body_ref") || key.equals("requiem:soul_owner_ref") || key.equals("requiem:mortician_ref")) {
            key = "requiem:entity_ref";
        }

        RecordType<?> type = RecordType.REGISTRY.get(Identifier.tryParse(key));
        if (type == null) return DataResult.error("Unknown record type %s".formatted(key));
        return DataResult.success(type);
    }

    private <T> DataResult<Map.Entry<RecordType<?>, Object>> deserializeRawDatum(RecordType<T> type, NbtElement nbtData) {
        return type.getCodec().parse(NbtOps.INSTANCE, nbtData).map(value -> Map.entry(type, value));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        for (GlobalRecord anchor : this.records.values()) {
            list.add(anchor.toTag());
        }
        if (!list.isEmpty()) tag.put("records", list);
    }

    @Override
    public String toString() {
        return this.records.toString();
    }
}
