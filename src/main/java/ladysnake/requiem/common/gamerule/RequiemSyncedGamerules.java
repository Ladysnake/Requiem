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
package ladysnake.requiem.common.gamerule;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.Requiem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RequiemSyncedGamerules implements AutoSyncedComponent, TransientComponent {
    public static final ComponentKey<RequiemSyncedGamerules> KEY = ComponentRegistry.getOrCreate(Requiem.id("synced_gamerules"), RequiemSyncedGamerules.class);

    public static RequiemSyncedGamerules get(World world) {
        return KEY.get(world.getScoreboard());
    }

    private final @Nullable MinecraftServer server;
    private boolean showPossessorNametag;
    private StartingRemnantType startingRemnantType;

    public RequiemSyncedGamerules(@Nullable MinecraftServer server) {
        this.server = server;
    }

    public boolean shouldShowPossessorNametag() {
        return server == null ? showPossessorNametag : server.getGameRules().getBoolean(RequiemGamerules.SHOW_POSSESSOR_NAMETAG);
    }

    public StartingRemnantType getStartingRemnantType() {
        return server == null ? startingRemnantType : server.getGameRules().get(RequiemGamerules.STARTING_SOUL_MODE).get();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.shouldShowPossessorNametag());
        buf.writeEnumConstant(this.getStartingRemnantType());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.showPossessorNametag = buf.readBoolean();
        this.startingRemnantType = buf.readEnumConstant(StartingRemnantType.class);
    }
}
