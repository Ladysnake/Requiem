package ladysnake.pandemonium.client.render.entity;

import com.mojang.authlib.GameProfile;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class ShellClientPlayerEntity extends OtherClientPlayerEntity {
    private final PlayerShellEntity shell;

    public ShellClientPlayerEntity(PlayerShellEntity shell, GameProfile profile) {
        super((ClientWorld) shell.world, profile);
        this.shell = shell;
        this.copyPositionAndRotation(shell);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.shell.getEquippedStack(slot);
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return this.shell.isPartVisible(modelPart);
    }

    @Override
    public CompoundTag getShoulderEntityLeft() {
        return this.shell.getShoulderEntityLeft();
    }

    @Override
    public CompoundTag getShoulderEntityRight() {
        return this.shell.getShoulderEntityRight();
    }

    @Override
    public boolean isOnFire() {
        return this.shell.isOnFire();
    }

    @Override
    public boolean hasVehicle() {
        return this.shell.hasVehicle();
    }

    @Override
    public @Nullable Entity getVehicle() {
        return this.shell.getVehicle();
    }

    public void updateData() {
        this.copyPositionAndRotation(this.shell);
        this.hurtTime = this.shell.hurtTime;
        this.deathTime = this.shell.deathTime;
    }
}
