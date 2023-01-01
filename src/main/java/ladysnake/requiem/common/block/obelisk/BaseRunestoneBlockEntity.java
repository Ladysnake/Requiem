/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.block.obelisk;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class BaseRunestoneBlockEntity extends BlockEntity {
    private static final int POWER_TRANSITION_TIME = 10;
    @Nullable
    protected Text customName;
    private long lastPowerUpdateTime;
    private float previousPowerRate;
    private float powerRate;

    public BaseRunestoneBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    public Optional<Text> getCustomName() {
        return Optional.ofNullable(this.customName);
    }

    public float getPowerRate(float tickDelta) {
        assert world != null;
        return MathHelper.lerp(MathHelper.clamp(((world.getTime() - lastPowerUpdateTime) + tickDelta) / POWER_TRANSITION_TIME, 0f, 1f), previousPowerRate, powerRate);
    }

    public float getPowerRate() {
        return powerRate;
    }

    public boolean isPowered() {
        return this.powerRate > 0.5f;
    }

    public void setPowerRate(float powerRate) {
        this.previousPowerRate = this.powerRate;
        this.powerRate = powerRate;
        this.lastPowerUpdateTime = this.getWorld() == null ? 0 : this.getWorld().getTime();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("custom_name", NbtElement.STRING_TYPE)) {
            this.customName = Text.Serializer.fromJson(nbt.getString("custom_name"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (this.customName != null) {
            nbt.putString("custom_name", Text.Serializer.toJson(this.customName));
        }
    }
}
