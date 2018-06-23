package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityPlayerShell extends EntityLiving {
    protected InventoryPlayerCorpse inventory;

    public EntityPlayerShell(World worldIn) {
        super(worldIn);
        inventory = new InventoryPlayerCorpse(this);
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
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.inventory.readFromNBT(compound.getTagList("Inventory", 10));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
    }
}
