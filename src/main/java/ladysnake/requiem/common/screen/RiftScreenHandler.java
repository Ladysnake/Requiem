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
package ladysnake.requiem.common.screen;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public class RiftScreenHandler extends ScreenHandler {
    private final BlockPos source;
    private final Predicate<PlayerEntity> canBeUsedBy;
    private final Set<BlockPos> obeliskPositions;

    public RiftScreenHandler(int syncId, BlockPos source, Set<BlockPos> obeliskPositions) {
        this(RequiemScreenHandlers.RIFT_SCREEN_HANDLER, syncId, source, p -> true, obeliskPositions);
    }

    public RiftScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, BlockPos source, Predicate<PlayerEntity> canBeUsedBy, Set<BlockPos> obeliskPositions) {
        super(type, syncId);
        this.source = source;
        this.canBeUsedBy = canBeUsedBy;
        this.obeliskPositions = obeliskPositions;
    }

    public Collection<BlockPos> getObeliskPositions() {
        return obeliskPositions;
    }

    public BlockPos getSource() {
        return source;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return RemnantComponent.isIncorporeal(player) && canBeUsedBy.test(player);
    }

    public void useRift(ServerPlayerEntity player, BlockPos target) {
        if (this.obeliskPositions.contains(target) && !this.source.equals(target)) {
            RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, player.world, target).ifPresent(respawnPosition -> {
                Vec3d towardsObelisk = Vec3d.ofBottomCenter(target).subtract(respawnPosition).normalize();
                float yaw = (float) MathHelper.wrapDegrees(MathHelper.atan2(towardsObelisk.z, towardsObelisk.x) * 180.0F / (float)Math.PI - 90.0);
                player.teleport(respawnPosition.x, respawnPosition.y, respawnPosition.z, true);
                player.setYaw(yaw);
                player.closeHandledScreen();
            });
        }
    }
}
