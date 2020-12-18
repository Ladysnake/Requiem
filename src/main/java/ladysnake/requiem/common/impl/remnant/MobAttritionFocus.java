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
package ladysnake.requiem.common.impl.remnant;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import it.unimi.dsi.fastutil.ints.IntIterator;
import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;
import java.util.UUID;

public class MobAttritionFocus extends AttritionFocusBase implements ClientTickingComponent, AutoSyncedComponent {
    private final LivingEntity holder;
    private transient int focusedAttrition;

    public MobAttritionFocus(LivingEntity holder) {
        this.holder = holder;
    }

    @Override
    public void addAttrition(UUID playerUuid, int level) {
        super.addAttrition(playerUuid, level);
        KEY.sync(this.holder);
    }

    @Override
    public void applyAttrition(PlayerEntity player) {
        super.applyAttrition(player);
        KEY.sync(this.holder);
    }

    @Override
    public void transferAttrition(AttritionFocus other) {
        super.transferAttrition(other);
        KEY.sync(this.holder);
    }

    @Override
    public void clientTick() {
        if (this.focusedAttrition > 0) {
            Random random = this.holder.getRandom();
            int nbParticles = random.nextInt(this.focusedAttrition + 1);

            for(int i = 0; i < nbParticles; ++i) {
                this.holder.world.addParticle(
                    RequiemParticleTypes.ATTRITION,
                    this.holder.getParticleX(0.5D),
                    this.holder.getRandomBodyY() - 0.25D,
                    this.holder.getParticleZ(0.5D),
                    (random.nextDouble() - 0.5D) * 2.0D,
                    -random.nextDouble(),
                    (random.nextDouble() - 0.5D) * 2.0D
                );
            }
        }
    }

    private int getFocusedAttrition() {
        int attrition = 0;
        for (IntIterator it = this.attritionLevels.values().iterator(); it.hasNext();) {
            attrition += it.nextInt();
        }
        return attrition;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.getFocusedAttrition());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.focusedAttrition = buf.readVarInt();
    }
}
