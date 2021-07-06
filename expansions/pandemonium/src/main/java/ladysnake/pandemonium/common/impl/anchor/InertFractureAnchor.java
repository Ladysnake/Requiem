/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.GlobalEntityPos;
import ladysnake.pandemonium.api.anchor.GlobalEntityTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class InertFractureAnchor implements FractureAnchor {
    public static final String ANCHOR_UUID_NBT = "uuid";
    public static final String SYNCED_NBT = "synced";
    public static final String ANCHOR_POS_NBT = "pos";
    public static final String ANCHOR_TYPE_NBT = "type";
    public static final String INERT_TYPE_ID = "requiem:inert";
    public static final Identifier INERT_TYPE = new Identifier(INERT_TYPE_ID);
    public static final String ENTITY_TYPE_ID = "requiem:entity";
    public static final Identifier ENTITY_TYPE = new Identifier(ENTITY_TYPE_ID);

    protected final GlobalEntityTracker manager;
    private final int id;
    private final UUID uuid;
    private final boolean syncWithClient;
    private GlobalEntityPos pos;
    private boolean invalid;

    public InertFractureAnchor(GlobalEntityTracker manager, UUID uuid, int id, GlobalEntityPos pos, boolean syncWithClient) {
        this.manager = manager;
        this.id = id;
        this.uuid = uuid;
        this.pos = pos;
        this.syncWithClient = syncWithClient;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public GlobalEntityPos getPos() {
        return this.pos;
    }

    @Override
    public void setPos(GlobalEntityPos pos) {
        this.pos = pos;
        if (this.syncWithClient) {
            this.manager.sync((buf, p) -> CommonAnchorManager.writeToPacket(buf, Collections.singleton(this), CommonAnchorManager.ANCHOR_SYNC));
        }
    }

    protected Optional<World> getWorld() {
        return this.manager.getWorld(this.pos.world());
    }

    @Override
    public void update() {
        // NO-OP
    }

    @Override
    public void invalidate() {
        this.invalid = true;
        if (this.syncWithClient) {
            this.manager.sync((buf, p) -> CommonAnchorManager.writeToPacket(buf, Collections.singleton(this), CommonAnchorManager.ANCHOR_REMOVE));
        }
    }

    @Override
    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    public Identifier getType() {
        return INERT_TYPE;
    }

    @Override
    public NbtCompound toTag(NbtCompound tag) {
        tag.putString(ANCHOR_TYPE_NBT, this.getType().toString());
        tag.putUuid(ANCHOR_UUID_NBT, this.getUuid());
        tag.put(ANCHOR_POS_NBT, GlobalEntityPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().orElseThrow());
        tag.putBoolean(SYNCED_NBT, this.syncWithClient);
        return tag;
    }
}
