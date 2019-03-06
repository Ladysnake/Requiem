package ladysnake.dissolution.common.entity;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.util.InventoryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

import static org.apiguardian.api.API.Status.MAINTAINED;

// TODO add inventory access
public class PlayerShellEntity extends MobEntity {
    public static final TrackedData<Optional<UUID>>PLAYER_UUID = DataTracker.registerData(PlayerShellEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    /**
     * Saves the content of the inventory the player had when this shell was created
     */
    protected BasicInventory inventory;
    /**
     * The player's game profile
     */
    private GameProfile profile;
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
        this.getDataTracker().startTracking(PLAYER_UUID, Optional.empty());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        if (PLAYER_UUID.equals(key)) {
            this.getPlayerUuid().ifPresent(uuid -> this.profile = new GameProfile(uuid, getName().getString()));
        }
        super.onTrackedDataSet(key);
    }

    public Optional<UUID> getPlayerUuid() {
        return this.getDataTracker().get(PLAYER_UUID);
    }

    public void setPlayerUuid(@CheckForNull UUID id) {
        this.getDataTracker().set(PLAYER_UUID, Optional.ofNullable(id));
    }

    public GameProfile getProfile() {
        return profile;
    }

    @CheckForNull
    public Possessable onSoulInteract(@Nullable PlayerEntity possessor) {
        if (possessor != null) {
            if (!world.isClient) {
                possessor.inventory.dropAll();
                // restore the player to their previous state
                if (playerNbt != null) {
                    possessor.fromTag(playerNbt);
                }
                // override common data that may have been altered during this shell's existence
                possessor.inventory.clear();
                performNbtCopy(this, possessor);
                ((ServerPlayerEntity)possessor).networkHandler.teleportRequest(this.x, this.y, this.z, this.yaw, this.pitch);
                if (this.inventory != null) {
                    transferInventory(this.inventory, possessor.inventory, Math.min(possessor.inventory.main.size(), this.inventory.getInvSize()));
                    this.dropInventory();
                }
                InventoryHelper.transferEquipment(this, possessor);
                this.invalidate();
                ((DissolutionPlayer) possessor).getRemnantState().setSoul(false);
            }
        }
        return null;
    }

    public void transferInventory(Inventory from, Inventory to, int size) {
        for (int i = 0; i < size; i++) {
            if (to.getInvStack(i).isEmpty()) {
                to.setInvStack(i, from.getInvStack(i));
                from.setInvStack(i, ItemStack.EMPTY);
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
                    if (this.isEquippedStackValid(clickedSlot)) {
                        this.swapItem(player, clickedSlot, stack, hand);
                    } else {
                        return ActionResult.PASS;
                    }
                } else {
                    this.swapItem(player, slot, stack, hand);
                }
                return ActionResult.SUCCESS;
            } else {
                return stack.isEmpty() && !this.isEquippedStackValid(this.getClickedSlot(vec))
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
        EquipmentSlot slot = EquipmentSlot.HAND_MAIN;
        boolean flag = this.isChild();
        double d0 = (rayTrace.y) * (flag ? 2.0D : 1.0D);

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.isEquippedStackValid(EquipmentSlot.FEET)) {
            slot = EquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.isEquippedStackValid(EquipmentSlot.CHEST)) {
            slot = EquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.isEquippedStackValid(EquipmentSlot.LEGS)) {
            slot = EquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.isEquippedStackValid(EquipmentSlot.HEAD)) {
            slot = EquipmentSlot.HEAD;
        }

        return slot;
    }

    protected void swapItem(PlayerEntity player, EquipmentSlot targetedSlot, ItemStack playerItemStack,
                            Hand hand) {
        ItemStack equippedStack = this.getEquippedStack(targetedSlot);
        if (player.abilities.creativeMode && equippedStack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack copy = playerItemStack.copy();
            copy.setAmount(1);
            this.setEquippedStack(targetedSlot, copy);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getAmount() > 1) {
            if (equippedStack.isEmpty()) {
                ItemStack copy = playerItemStack.copy();
                copy.setAmount(1);
                this.setEquippedStack(targetedSlot, copy);
                playerItemStack.subtractAmount(1);
            }
        } else {
            this.setEquippedStack(targetedSlot, playerItemStack);
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
            for(int int_1 = 0; int_1 < this.inventory.getInvSize(); ++int_1) {
                ItemStack itemStack_1 = this.inventory.getInvStack(int_1);
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
    protected float method_5929(EquipmentSlot slot) {
        return 2.0F;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        if (tag.containsKey("Items")) {
            ListTag items = tag.getList("Items", 10);
            this.inventory = new BasicInventory(tag.getInt("InvSize"));

            for(int i = 0; i < items.size(); ++i) {
                CompoundTag compoundTag_2 = items.getCompoundTag(i);
                int slot = compoundTag_2.getByte("Slot") & 255;
                if (slot >= 2 && slot < this.inventory.getInvSize()) {
                    this.inventory.setInvStack(slot, ItemStack.fromTag(compoundTag_2));
                }
            }
        }
        if (tag.containsKey("PlayerNbt")) {
            this.playerNbt = tag.getCompound("PlayerNbt");
        }
        this.setPlayerUuid(tag.getUuid("Player"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        if (this.inventory != null) {
            ListTag items = new ListTag();

            for(int i = 2; i < this.inventory.getInvSize(); ++i) {
                ItemStack stack = this.inventory.getInvStack(i);
                if (!stack.isEmpty()) {
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putByte("Slot", (byte)i);
                    stack.toTag(slotTag);
                    items.add(slotTag);
                }
            }

            compound.put("Items", items);
            compound.putInt("InvSize", this.inventory.getInvSize());
        }
        this.getPlayerUuid().ifPresent(uuid -> compound.putUuid("Player", uuid));
    }

    /* Static Methods */

    public static PlayerShellEntity fromPlayer(PlayerEntity player) {
        PlayerShellEntity shell = new PlayerShellEntity(DissolutionEntities.PLAYER_SHELL, player.world);
        shell.playerNbt = performNbtCopy(player, shell);
        int invSize = player.inventory.main.size();
        shell.inventory = new BasicInventory(invSize);
        InventoryHelper.transferEquipment(player, shell);
        shell.transferInventory(player.inventory, shell.inventory, invSize);
        shell.setPlayerUuid(player.getUuid());
        shell.setCustomName(new StringTextComponent(player.getEntityName()));
        return shell;
    }

    private static CompoundTag performNbtCopy(Entity from, Entity to) {
        UUID fromUuid = to.getUuid();
        // Save the complete representation of the player
        CompoundTag serialized = new CompoundTag();
        // We write every attribute of the destination entity to the tag, then we override.
        // That way, attributes that do not exist in the base entity are kept intact during the copy.
        to.toTag(serialized);
        from.toTag(serialized);
        to.fromTag(serialized);
        // Restore UUID
        to.setUuid(fromUuid);
        return serialized;
    }

}
