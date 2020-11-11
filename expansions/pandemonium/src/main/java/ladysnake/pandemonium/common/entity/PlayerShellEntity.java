/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.pandemonium.common.entity;

import com.mojang.authlib.GameProfile;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.ladysnake.impersonate.Impersonate;
import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.client.render.entity.ShellClientPlayerEntity;
import ladysnake.pandemonium.mixin.common.entity.mob.LivingEntityAccessor;
import ladysnake.pandemonium.mixin.common.entity.player.PlayerEntityAccessor;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.util.InventoryHelper;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import nerdhub.cardinal.components.api.util.container.AbstractComponentContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.apiguardian.api.API.Status.MAINTAINED;

// TODO add inventory access
public class PlayerShellEntity extends MobEntity {
    public static final TrackedData<CompoundTag> PLAYER_PROFILE = DataTracker.registerData(PlayerShellEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    public static final TrackedData<Byte> PLAYER_MODEL_PARTS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<CompoundTag> LEFT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    public static final TrackedData<CompoundTag> RIGHT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    /**
     * Saves the content of the inventory the player had when this shell was created
     */
    protected SimpleInventory inventory;
    private UUID playerUuid;
    /**
     * The full NBT data representing the player when this shell was created
     */
    @Nullable
    protected CompoundTag playerNbt;

    @API(status = MAINTAINED)
    protected PlayerShellEntity(EntityType<? extends PlayerShellEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(PLAYER_PROFILE, new CompoundTag());
        this.getDataTracker().startTracking(PLAYER_MODEL_PARTS, (byte) 0);
        this.getDataTracker().startTracking(LEFT_SHOULDER_ENTITY, new CompoundTag());
        this.getDataTracker().startTracking(RIGHT_SHOULDER_ENTITY, new CompoundTag());
    }

    /* * * * * * * * * * * * client stuff * * * * * * * * * * * * * */

    @Nullable
    @Environment(EnvType.CLIENT)
    private ShellClientPlayerEntity renderedPlayer;

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        if (this.world.isClient) {
            if (PLAYER_PROFILE.equals(key)) {
                GameProfile profile = getGameProfile();
                this.renderedPlayer = profile == null ? null : new ShellClientPlayerEntity(this, profile);
            } else if (this.renderedPlayer != null) {
                if (LivingEntityAccessor.getStuckArrowCountTrackedData().equals(key)) {
                    this.renderedPlayer.setStuckArrowCount(this.getStuckArrowCount());
                } else if (LivingEntityAccessor.getStuckStingerCountTrackedData().equals(key)) {
                    this.renderedPlayer.setStingerCount(this.getStingerCount());
                }
            }
        }
        super.onTrackedDataSet(key);
    }

    public GameProfile getGameProfile() {
        CompoundTag tag = this.getDataTracker().get(PLAYER_PROFILE);
        return NbtHelper.toGameProfile(tag);
    }

    public void setPlayerProfile(@CheckForNull GameProfile profile) {
        CompoundTag tag = new CompoundTag();
        if (profile != null) {
            NbtHelper.fromGameProfile(tag, profile);
        }
        this.getDataTracker().set(PLAYER_PROFILE, tag);
    }

    @Environment(EnvType.CLIENT)
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return (getPlayerModelParts() & modelPart.getBitFlag()) == modelPart.getBitFlag();
    }

    public byte getPlayerModelParts() {
        return this.getDataTracker().get(PLAYER_MODEL_PARTS);
    }

    public CompoundTag getShoulderEntityLeft() {
        return this.getDataTracker().get(LEFT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityLeft(CompoundTag entityTag) {
        this.dataTracker.set(LEFT_SHOULDER_ENTITY, entityTag);
    }

    public CompoundTag getShoulderEntityRight() {
        return this.getDataTracker().get(RIGHT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityRight(CompoundTag entityTag) {
        this.dataTracker.set(RIGHT_SHOULDER_ENTITY, entityTag);
    }

    @Environment(EnvType.CLIENT)
    public ShellClientPlayerEntity getRenderedPlayer() {
        if (this.renderedPlayer == null) this.renderedPlayer = new ShellClientPlayerEntity(this, new GameProfile(UUID.randomUUID(), null));
        return this.renderedPlayer;
    }

    public void setPlayerModelParts(byte b) {
        this.dataTracker.set(PLAYER_MODEL_PARTS, b);
    }

    /* * * * * * * * * * * * common stuff * * * * * * * * * * * * * * */

    public void onSoulInteract(@Nullable PlayerEntity possessor) {
        if (possessor != null) {
            if (!world.isClient) {
                possessor.inventory.dropAll();
                this.restorePlayerData((ServerPlayerEntity) possessor);
                this.remove();
                RemnantComponent.get(possessor).setSoul(false);

                if (!Objects.equals(this.playerUuid, possessor.getUuid())) {
                    GameProfile gameProfile = this.getGameProfile();
                    Impersonate.IMPERSONATION.get(possessor).impersonate(Pandemonium.BODY_IMPERSONATION, gameProfile == null ? new GameProfile(this.playerUuid, null) : gameProfile);
                }
            }
        }
    }

    public void restorePlayerData(ServerPlayerEntity possessor) {
        // restore the player to their previous state
        if (this.playerNbt != null) {
            possessor.fromTag(this.playerNbt);
        }
        // override common data that may have been altered during this shell's existence
        possessor.inventory.clear();
        performNbtCopy(this.toTag(new CompoundTag()), possessor);
        possessor.networkHandler.teleportRequest(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch, EnumSet.allOf(PlayerPositionLookS2CPacket.Flag.class));
        if (this.inventory != null) {
            transferInventory(this.inventory, possessor.inventory, Math.min(possessor.inventory.main.size(), this.inventory.size()));
            this.dropInventory();
        }
        InventoryHelper.transferEquipment(this, possessor);
    }

    public void storePlayerData(ServerPlayerEntity player) {
        // Transfer inventory
        InventoryHelper.transferEquipment(player, this);
        int invSize = player.inventory.main.size();
        inventory = new SimpleInventory(invSize);
        transferInventory(player.inventory, inventory, invSize);

        // Save the complete representation of the player
        RespawnNbt respawnNbt = gatherRespawnData(player);
        performNbtCopy(respawnNbt.leftoverData, this);
        this.playerNbt = respawnNbt.leftoverData;

        // player keeps everything that goes through death
        player.fromTag(respawnNbt.persistentData);

        setPlayerModelParts(player.getDataTracker().get(PlayerEntityAccessor.getPlayerModelPartsTrackedData()));

        this.setPlayerProfile(player.getGameProfile());
        this.setCustomName(new LiteralText(player.getEntityName()));
        this.playerUuid = player.getUuid();
    }

    public void transferInventory(Inventory from, Inventory to, int size) {
        for (int i = 0; i < size; i++) {
            if (to.getStack(i).isEmpty()) {
                to.setStack(i, from.getStack(i));
                from.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Applies the given player interaction to this Entity.
     */
    @Nonnull
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() != Items.NAME_TAG) {
            if (!this.world.isClient && !player.isSpectator()) {
                EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
                if (stack.isEmpty()) {
                    EquipmentSlot clickedSlot = this.getClickedSlot(vec);
                    if (this.hasStackEquipped(clickedSlot)) {
                        this.swapItem(player, clickedSlot, stack, hand);
                    } else {
                        return ActionResult.PASS;
                    }
                } else {
                    this.swapItem(player, slot, stack, hand);
                }
                return ActionResult.SUCCESS;
            } else {
                return stack.isEmpty() && !this.hasStackEquipped(this.getClickedSlot(vec))
                        ? ActionResult.PASS
                        : ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    /**
     * Vanilla code from the armor stand
     *
     * @param rayTrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EquipmentSlot getClickedSlot(Vec3d rayTrace) {
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        boolean flag = this.isBaby();
        double d0 = (rayTrace.y) * (flag ? 2.0D : 1.0D);

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasStackEquipped(EquipmentSlot.FEET)) {
            slot = EquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.hasStackEquipped(EquipmentSlot.CHEST)) {
            slot = EquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasStackEquipped(EquipmentSlot.LEGS)) {
            slot = EquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            slot = EquipmentSlot.HEAD;
        }

        return slot;
    }

    protected void swapItem(PlayerEntity player, EquipmentSlot targetedSlot, ItemStack playerItemStack,
                            Hand hand) {
        ItemStack equippedStack = this.getEquippedStack(targetedSlot);
        if (player.abilities.creativeMode && equippedStack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack copy = playerItemStack.copy();
            copy.setCount(1);
            this.equipStack(targetedSlot, copy);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1) {
            if (equippedStack.isEmpty()) {
                ItemStack copy = playerItemStack.copy();
                copy.setCount(1);
                this.equipStack(targetedSlot, copy);
                playerItemStack.decrement(1);
            }
        } else {
            this.equipStack(targetedSlot, playerItemStack);
            player.setStackInHand(hand, equippedStack);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distance) {
        return false;
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.inventory != null) {
            for(int int_1 = 0; int_1 < this.inventory.size(); ++int_1) {
                ItemStack itemStack_1 = this.inventory.getStack(int_1);
                if (!itemStack_1.isEmpty()) {
                    this.dropStack(itemStack_1);
                }
            }

        }
    }

    /**
     * Gets the drop chance of the item in the given slot. > 1 means it must drop with no durability loss.
     */
    @Override
    protected float getDropChance(EquipmentSlot slot) {
        return 2.0F;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        this.updateShoulderEntity(this.getShoulderEntityLeft());
        this.updateShoulderEntity(this.getShoulderEntityRight());
        if (!this.world.isClient && (this.fallDistance > 0.5F || this.isTouchingWater()) || this.isSleeping()) {
            this.dropShoulderEntities();
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (super.damage(source, amount)) {
            this.dropShoulderEntities();
            return true;
        }
        return false;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        this.dropShoulderEntities();
    }

    private void updateShoulderEntity(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null && (!compoundTag.contains("Silent") || !compoundTag.getBoolean("Silent")) && this.world.random.nextInt(200) == 0) {
            String string = compoundTag.getString("id");
            EntityType.get(string).filter((entityType) -> entityType == EntityType.PARROT).ifPresent((entityType) -> {
                if (!ParrotEntity.imitateNearbyMob(this.world, this)) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getRandomSound(this.world, this.world.random), this.getSoundCategory(), 1.0F, ParrotEntity.getSoundPitch(this.world.random));
                }
            });
        }
    }

    protected void dropShoulderEntities() {
        this.dropShoulderEntity(this.getShoulderEntityLeft());
        this.setShoulderEntityLeft(new CompoundTag());
        this.dropShoulderEntity(this.getShoulderEntityRight());
        this.setShoulderEntityRight(new CompoundTag());
    }

    private void dropShoulderEntity(CompoundTag entityNbt) {
        if (!this.world.isClient && !entityNbt.isEmpty()) {
            EntityType.getEntityFromTag(entityNbt, this.world).ifPresent((entity) -> {
                if (entity instanceof TameableEntity) {
                    ((TameableEntity)entity).setOwnerUuid(this.uuid);
                }

                entity.updatePosition(this.getX(), this.getY() + 0.699999988079071D, this.getZ());
                ((ServerWorld)this.world).tryLoadEntity(entity);
            });
        }
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        if (tag.contains("Items")) {
            ListTag items = tag.getList("Items", 10);
            this.inventory = new SimpleInventory(tag.getInt("InvSize"));

            for(int i = 0; i < items.size(); ++i) {
                CompoundTag compoundTag_2 = items.getCompound(i);
                int slot = compoundTag_2.getByte("Slot") & 255;
                if (slot >= 2 && slot < this.inventory.size()) {
                    this.inventory.setStack(slot, ItemStack.fromTag(compoundTag_2));
                }
            }
        }

        if (tag.contains("PlayerNbt")) {
            this.playerNbt = tag.getCompound("PlayerNbt");
        }

        if (tag.contains("Player")) {
            this.playerUuid = tag.getUuid("Player");
        }

        if (tag.contains("PlayerProfile")) {
            this.setPlayerProfile(NbtHelper.toGameProfile(tag.getCompound("PlayerProfile")));
        }

        setPlayerModelParts(tag.getByte("PlayerModelParts"));

        if (tag.contains("ShoulderEntityLeft", 10)) {
            this.setShoulderEntityLeft(tag.getCompound("ShoulderEntityLeft"));
        }

        if (tag.contains("ShoulderEntityRight", 10)) {
            this.setShoulderEntityRight(tag.getCompound("ShoulderEntityRight"));
        }
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        if (this.inventory != null) {
            ListTag items = new ListTag();

            for(int i = 2; i < this.inventory.size(); ++i) {
                ItemStack stack = this.inventory.getStack(i);
                if (!stack.isEmpty()) {
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putByte("Slot", (byte)i);
                    stack.toTag(slotTag);
                    items.add(slotTag);
                }
            }

            compound.put("Items", items);
            compound.putInt("InvSize", this.inventory.size());
        }
        if (this.playerUuid != null) {
            compound.putUuid("Player", this.playerUuid);
        }
        GameProfile gameProfile = this.getGameProfile();
        if (gameProfile != null) {
            compound.put("PlayerProfile", NbtHelper.fromGameProfile(new CompoundTag(), gameProfile));
        }
        compound.putByte("PlayerModelParts", this.getPlayerModelParts());

        if (!this.getShoulderEntityLeft().isEmpty()) {
            compound.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
        }

        if (!this.getShoulderEntityRight().isEmpty()) {
            compound.put("ShoulderEntityRight", this.getShoulderEntityRight());
        }
    }

    /* Static Methods */

    public static PlayerShellEntity fromPlayer(ServerPlayerEntity player) {
        PlayerShellEntity shell = new PlayerShellEntity(PandemoniumEntities.PLAYER_SHELL, player.world);
        shell.storePlayerData(player);
        return shell;
    }

    private static CompoundTag performNbtCopy(CompoundTag from, Entity to) {
        UUID fromUuid = to.getUuid();
        // Save the complete representation of the player
        CompoundTag serialized = new CompoundTag();
        // We write every attribute of the destination entity to the tag, then we override.
        // That way, attributes that do not exist in the base entity are kept intact during the copy.
        to.toTag(serialized);
        serialized.copyFrom(from);
        to.fromTag(serialized);
        // Restore UUID
        to.setUuid(fromUuid);
        return serialized;
    }

    private static RespawnNbt gatherRespawnData(ServerPlayerEntity player) {
        //Setup location for dummy
        GameRules.BooleanRule keepInventory = player.world.getGameRules().get(GameRules.KEEP_INVENTORY);
        boolean keepInv = keepInventory.get();

        try {
            keepInventory.set(false, player.getServer());

            //Setup location for dummy
            ServerPlayerEntity dummy = new ServerPlayerEntity(
                Objects.requireNonNull(player.getServer()),
                player.getServerWorld(),
                player.getGameProfile(),
                new ServerPlayerInteractionManager(player.getServerWorld())
            );
            dummy.networkHandler = player.networkHandler;
            dummy.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
            dummy.fallDistance = 0F;

            CompoundTag baseData = dummy.toTag(new CompoundTag());

            //Clone data
            dummy.copyFrom(player, false);
            dummy.setEntityId(player.getEntityId());

            CompoundTag persistentData = dummy.toTag(new CompoundTag());
            CompoundTag leftoverData = player.toTag(new CompoundTag());

            RespawnNbt respawnNbt = new RespawnNbt(baseData, persistentData, leftoverData);
            respawnNbt.deduplicateVanillaData();
            respawnNbt.deduplicateComponents(ComponentProvider.fromEntity(player).getComponentContainer().keys());
            return respawnNbt;
        } finally {
            keepInventory.set(keepInv, player.getServer());
        }
    }

    private static final class RespawnNbt {
        private final CompoundTag baseData;
        private final CompoundTag persistentData;
        private final CompoundTag leftoverData;

        private RespawnNbt(CompoundTag baseData, CompoundTag persistentData, CompoundTag leftoverData) {
            this.baseData = baseData;
            this.persistentData = persistentData;
            this.leftoverData = leftoverData;
        }

        private void deduplicateComponents(Set<ComponentKey<?>> keys) {
            CompoundTag baseComponents = baseData.getCompound(AbstractComponentContainer.NBT_KEY);
            CompoundTag persistentComponents = persistentData.getCompound(AbstractComponentContainer.NBT_KEY);
            CompoundTag leftoverComponents = leftoverData.getCompound(AbstractComponentContainer.NBT_KEY);

            for (ComponentKey<?> key : keys) {
                String keyId = key.getId().toString();
                if (EntityComponents.getRespawnCopyStrategy(key) == RespawnCopyStrategy.ALWAYS_COPY) {
                    // avoid duplicating fully soulbound data
                    leftoverComponents.remove(keyId);
                } else if (persistentComponents.contains(keyId)){
                    persistentComponents.put(keyId, baseComponents.getCompound(keyId));
                }
            }
        }

        private void deduplicateVanillaData() {
            leftoverData.remove("UUID");
            leftoverData.remove("SpawnX");
            leftoverData.remove("SpawnY");
            leftoverData.remove("SpawnZ");
            leftoverData.remove("SpawnForced");
            leftoverData.remove("SpawnAngle");
            leftoverData.remove("SpawnDimension");
            leftoverData.remove("EnderItems");
            leftoverData.remove("abilities");
            leftoverData.remove("playerGameType");
            leftoverData.remove("previousPlayerGameType");
            leftoverData.remove("seenCredits");
            persistentData.remove("playerGameType");
            persistentData.remove("previousPlayerGameType");
            persistentData.remove("abilities");
            persistentData.remove("seenCredits");
            persistentData.put("ShoulderEntityLeft", baseData.get("ShoulderEntityLeft"));
            persistentData.put("ShoulderEntityRight", baseData.get("ShoulderEntityRight"));
            persistentData.put("Attributes", baseData.get("Attributes"));
        }
    }
}
