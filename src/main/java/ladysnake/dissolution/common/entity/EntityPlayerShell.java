package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@SuppressWarnings("Guava")
public class EntityPlayerShell extends EntityLiving implements ISoulInteractable {

    public static final ResourceLocation LOOT = new ResourceLocation(Ref.MOD_ID, "inject/human.json");

    protected static final float SIZE_X = 0.6F, SIZE_Y = 1.95F;

    private static DataParameter<Optional<UUID>> PLAYER = EntityDataManager
            .createKey(EntityPlayerShell.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Boolean> INERT = EntityDataManager
            .createKey(EntityPlayerShell.class, DataSerializers.BOOLEAN);

    protected InventoryPlayerCorpse inventory;
    private GameProfile profile;

    public EntityPlayerShell(World worldIn) {
        super(worldIn);
        inventory = new InventoryPlayerCorpse(this);
    }

    public GameProfile getProfile() {
        return profile;
    }

    public UUID getPlayer() {
        return this.getDataManager().get(PLAYER).orNull();
    }

    public void setPlayer(UUID id) {
        this.getDataManager().set(PLAYER, Optional.fromNullable(id));
    }

    public void setInert(boolean isCorpse) {
        this.getDataManager().set(INERT, isCorpse);

        if (isCorpse) {
            // I know what I'm doing for once
            //noinspection SuspiciousNameCombination
            this.setSize(SIZE_Y, SIZE_X);
        } else {
            this.setSize(SIZE_X, SIZE_Y);
        }
    }

    /**
     * @return Whether this player corpse is lying on the ground
     */
    public boolean isInert() {
        return this.getDataManager().get(INERT);
    }

    @Override
    public EnumActionResult applySoulInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (this.ticksExisted > 20) {
            player.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
            if (!world.isRemote) {
                for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                    player.inventory.setInventorySlotContents(i, this.inventory.getStackInSlot(i));
                    this.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
                DissolutionInventoryHelper.transferEquipment(this, player);
                this.posY -= 500;
                this.setDead();
                CapabilityIncorporealHandler.getHandler(player).setCorporealityStatus(SoulStates.BODY);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    /**
     * Applies the given player interaction to this Entity.
     */
    @Nonnull
    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (isSuitableForInteraction(player) && itemstack.getItem() != Items.NAME_TAG) {
            if (!this.world.isRemote && !player.isSpectator()) {
                EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

                if (itemstack.isEmpty()) {
                    EntityEquipmentSlot entityEquipmentSlot2 = this.getClickedSlot(vec);

                    if (this.hasItemInSlot(entityEquipmentSlot2)) {
                        this.swapItem(player, entityEquipmentSlot2, itemstack, hand);
                    } else {
                        return EnumActionResult.PASS;
                    }
                } else {

                    this.swapItem(player, entityequipmentslot, itemstack, hand);
                }
//                if (entityequipmentslot == EntityEquipmentSlot.MAINHAND) {
//                    this.setCombatTask();
//                }

                return EnumActionResult.SUCCESS;
            } else {
                return itemstack.isEmpty() && !this.hasItemInSlot(this.getClickedSlot(vec)) ? EnumActionResult.PASS
                        : EnumActionResult.SUCCESS;
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    /**
     * Vanilla code from the armor stand
     *
     * @param raytrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EntityEquipmentSlot getClickedSlot(Vec3d raytrace) {
        EntityEquipmentSlot entityEquipmentSlot = EntityEquipmentSlot.MAINHAND;
        boolean flag = this.isChild();
        double d0 = (this.isInert() ? raytrace.z + 1.2 : raytrace.y) * (flag ? 2.0D : 1.0D);
        EntityEquipmentSlot entityEquipmentSlot1 = EntityEquipmentSlot.FEET;

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(entityEquipmentSlot1)) {
            entityEquipmentSlot = EntityEquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.hasItemInSlot(EntityEquipmentSlot.CHEST)) {
            entityEquipmentSlot = EntityEquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EntityEquipmentSlot.LEGS)) {
            entityEquipmentSlot = EntityEquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.hasItemInSlot(EntityEquipmentSlot.HEAD)) {
            entityEquipmentSlot = EntityEquipmentSlot.HEAD;
        }

        return entityEquipmentSlot;
    }

    protected void swapItem(EntityPlayer player, EntityEquipmentSlot targetedSlot, ItemStack playerItemStack,
                            EnumHand hand) {
        ItemStack itemstack = this.getItemStackFromSlot(targetedSlot);
        if (player.capabilities.isCreativeMode && itemstack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack itemstack2 = playerItemStack.copy();
            itemstack2.setCount(1);
            this.setItemStackToSlot(targetedSlot, itemstack2);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1) {
            if (itemstack.isEmpty()) {
                ItemStack itemstack1 = playerItemStack.copy();
                itemstack1.setCount(1);
                this.setItemStackToSlot(targetedSlot, itemstack1);
                playerItemStack.shrink(1);
            }
        } else {
            this.setItemStackToSlot(targetedSlot, playerItemStack);
            player.setHeldItem(hand, itemstack);
        }
    }

    protected boolean isSuitableForInteraction(EntityPlayer player) {
        return !CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal() || player.isCreative();
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            player.openGui(Dissolution.instance, GuiProxy.PLAYER_CORPSE, this.world, (int) this.posX, (int) this.posY, (int) this.posZ);
        }
        return true;
    }

    public InventoryPlayerCorpse getInventory() {
        return inventory;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, @Nonnull DamageSource cause) {
        super.dropLoot(wasRecentlyHit, lootingModifier, cause);
        if (this.inventory != null) {
            this.inventory.dropAllItems(this);
        }
    }

    // ensures that this entity's equipment is dropped with a 100% chance
    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        for (ItemStack stack : this.getEquipmentAndArmor()) {
            this.entityDropItem(stack, 0.5f);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(INERT, false);
        this.getDataManager().register(PLAYER, Optional.absent());
    }

    @Override
    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        if (PLAYER.equals(key)) {
            this.profile = TileEntitySkull.updateGameProfile(new GameProfile(getPlayer(), getName()));
        }
        super.notifyDataManagerChange(key);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.inventory.readFromNBT(compound.getTagList("Inventory", 10));
        this.setInert(compound.getBoolean("inert"));
        this.setPlayer(compound.getUniqueId("player"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("inert", this.isInert());
        compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        UUID player = getPlayer();
        if (player != null) {
            compound.setUniqueId("player", player);
        }
    }
}
