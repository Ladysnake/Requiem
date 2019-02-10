package ladysnake.dissolution.common.entity;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.util.InventoryHelper;
import net.minecraft.block.entity.SkullBlockEntity;
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

    protected BasicInventory inventory;
    private GameProfile profile;

    public PlayerShellEntity(PlayerEntity player) {
        this(player.world);
        this.setPositionAndAngles(player);
        this.inventory = new BasicInventory(player.inventory.main.size());
        this.setPlayerUuid(player.getUuid());
        this.setCustomName(new StringTextComponent(player.getEntityName()));
    }

    public PlayerShellEntity(World world) {
        this(DissolutionEntities.PLAYER_SHELL, world);
    }

    @API(status = MAINTAINED)
    protected PlayerShellEntity(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(PLAYER_UUID, Optional.empty());
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

    @Nullable
    public Possessable onSoulInteract(@Nullable PlayerEntity possessor) {
        if (possessor != null) {
            possessor.setPositionAndAngles(this);
            if (!world.isClient) {
                if (this.inventory != null) {
                    transferInventory(this.inventory, possessor.inventory, possessor.inventory.main.size());
                    this.dropInventory();
                }
                InventoryHelper.transferEquipment(this, possessor);
                this.world.removeEntity(this);
                ((DissolutionPlayer) possessor).getRemnantState().setSoul(false);
            }
        }
        return null;
    }

    public BasicInventory getInventory() {
        return inventory;
    }

    public void transferInventory(Inventory from, Inventory to, int size) {
        for (int i = 0; i < size; i++) {
            if (to.getInvStack(i).isEmpty()) {
                to.setInvStack(i, from.getInvStack(i));
                from.setInvStack(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        if (PLAYER_UUID.equals(key)) {
            this.getPlayerUuid().ifPresent(uuid -> this.profile = SkullBlockEntity.loadProperties(new GameProfile(uuid, getName().getString())));
        }
        super.onTrackedDataSet(key);
    }

    /**
     * Applies the given player interaction to this Entity.
     */
    @Nonnull
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);

        if (itemstack.getItem() != Items.NAME_TAG) {
            if (!this.world.isClient && !player.isSpectator()) {
                EquipmentSlot entityequipmentslot = MobEntity.getPreferredEquipmentSlot(itemstack);

                if (itemstack.isEmpty()) {
                    EquipmentSlot entityEquipmentSlot2 = this.getClickedSlot(vec);

                    if (this.isEquippedStackValid(entityEquipmentSlot2)) {
                        this.swapItem(player, entityEquipmentSlot2, itemstack, hand);
                    } else {
                        return ActionResult.PASS;
                    }
                } else {

                    this.swapItem(player, entityequipmentslot, itemstack, hand);
                }

                return ActionResult.SUCCESS;
            } else {
                return itemstack.isEmpty() && !this.isEquippedStackValid(this.getClickedSlot(vec)) ? ActionResult.PASS
                        : ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    /**
     * Vanilla code from the armor stand
     *
     * @param raytrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EquipmentSlot getClickedSlot(Vec3d raytrace) {
        EquipmentSlot entityEquipmentSlot = EquipmentSlot.HAND_MAIN;
        boolean flag = this.isChild();
        double d0 = (raytrace.y) * (flag ? 2.0D : 1.0D);
        EquipmentSlot entityEquipmentSlot1 = EquipmentSlot.FEET;

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.isEquippedStackValid(entityEquipmentSlot1)) {
            entityEquipmentSlot = EquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.isEquippedStackValid(EquipmentSlot.CHEST)) {
            entityEquipmentSlot = EquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.isEquippedStackValid(EquipmentSlot.LEGS)) {
            entityEquipmentSlot = EquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.isEquippedStackValid(EquipmentSlot.HEAD)) {
            entityEquipmentSlot = EquipmentSlot.HEAD;
        }

        return entityEquipmentSlot;
    }

    protected void swapItem(PlayerEntity player, EquipmentSlot targetedSlot, ItemStack playerItemStack,
                            Hand hand) {
        ItemStack itemstack = this.getEquippedStack(targetedSlot);
        if (player.abilities.creativeMode && itemstack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack itemstack2 = playerItemStack.copy();
            itemstack2.setAmount(1);
            this.setEquippedStack(targetedSlot, itemstack2);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getAmount() > 1) {
            if (itemstack.isEmpty()) {
                ItemStack itemstack1 = playerItemStack.copy();
                itemstack1.setAmount(1);
                this.setEquippedStack(targetedSlot, itemstack1);
                playerItemStack.subtractAmount(1);
            }
        } else {
            this.setEquippedStack(targetedSlot, playerItemStack);
            player.setStackInHand(hand, itemstack);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double double_1) {
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
    protected float method_5929(EquipmentSlot equipmentSlot_1) {
        return 2.0F;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);
        if (compound.containsKey("Items")) {
            ListTag items = compound.getList("Items", 10);
            this.inventory = new BasicInventory(items.size());

            for(int int_1 = 0; int_1 < items.size(); ++int_1) {
                CompoundTag compoundTag_2 = items.getCompoundTag(int_1);
                int int_2 = compoundTag_2.getByte("Slot") & 255;
                if (int_2 >= 2 && int_2 < this.inventory.getInvSize()) {
                    this.inventory.setInvStack(int_2, ItemStack.fromTag(compoundTag_2));
                }
            }
        }
        this.setPlayerUuid(compound.getUuid("Player"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        if (this.inventory != null) {
            ListTag listTag_1 = new ListTag();

            for(int int_1 = 2; int_1 < this.inventory.getInvSize(); ++int_1) {
                ItemStack itemStack_1 = this.inventory.getInvStack(int_1);
                if (!itemStack_1.isEmpty()) {
                    CompoundTag compoundTag_2 = new CompoundTag();
                    compoundTag_2.putByte("Slot", (byte)int_1);
                    itemStack_1.toTag(compoundTag_2);
                    listTag_1.add(compoundTag_2);
                }
            }

            compound.put("Items", listTag_1);
        }
        this.getPlayerUuid().ifPresent(uuid -> compound.putUuid("Player", uuid));
    }

}
