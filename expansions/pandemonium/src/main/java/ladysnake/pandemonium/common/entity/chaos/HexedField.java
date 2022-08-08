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
package ladysnake.pandemonium.common.entity.chaos;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.BiFunction;

final class HexedField<T> {
    private final FieldFlipper<T> flipper;
    private final Field field;
    private final Class<T> fieldType;

    private HexedField(FieldFlipper<T> flipper, Field field, Class<T> fieldType) {
        this.flipper = flipper;
        this.field = field;
        this.fieldType = fieldType;
    }

    // Hex kind of like byte manipulation, but mostly as in curse
    static <V> HexedField<V> hex(Field target) {
        long offset = AiAbyss.abholos.objectFieldOffset(target);
        @SuppressWarnings("unchecked") Class<V> fieldType = (Class<V>) target.getType();
        FieldFlipper<V> flipper;

        if (fieldType == boolean.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putBoolean(to, offset, AiAbyss.abholos.getBoolean(from, offset));
        } else if (fieldType == byte.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putByte(to, offset, AiAbyss.abholos.getByte(from, offset));
        } else if (fieldType == short.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putShort(to, offset, AiAbyss.abholos.getShort(from, offset));
        } else if (fieldType == char.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putChar(to, offset, AiAbyss.abholos.getChar(from, offset));
        } else if (fieldType == int.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putInt(to, offset, AiAbyss.abholos.getInt(from, offset));
        } else if (fieldType == float.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putFloat(to, offset, AiAbyss.abholos.getFloat(from, offset));
        } else if (fieldType == long.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putLong(to, offset, AiAbyss.abholos.getLong(from, offset));
        } else if (fieldType == double.class) {
            flipper = (field, from, to, objectMapper) -> AiAbyss.abholos.putDouble(to, offset, AiAbyss.abholos.getDouble(from, offset));
        } else if (fieldType.isPrimitive()) {
            throw new IllegalStateException("Unknown primitive type " + fieldType);
        } else {
            flipper = (field, from, to, objectMapper) -> {
                V value = objectMapper.apply(field.cast(AiAbyss.abholos.getObject(from, offset)), field);
                AiAbyss.abholos.putObject(to, offset, value);
            };
        }

        return new HexedField<>(flipper, target, fieldType);
    }

    void copy(Object from, Object to, BiFunction<T, HexedField<T>, T> objectMapper) {
        this.flipper.copy(this, from, to, objectMapper);
    }

    private interface FieldFlipper<T> {
        void copy(HexedField<T> field, Object from, Object to, BiFunction<T, HexedField<T>, T> objectMapper);
    }

    @Override
    public String toString() {
        return "HexedField[" + field + "]";
    }

    public T cast(Object object) {
        return this.fieldType.cast(object);
    }

    public Field field() {
        return field;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HexedField<?>) obj;
        return Objects.equals(this.field, that.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }
}
