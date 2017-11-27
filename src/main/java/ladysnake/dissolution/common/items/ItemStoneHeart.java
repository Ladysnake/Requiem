package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public class ItemStoneHeart extends Item {

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        if (!playerIn.world.isRemote && target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
            if (target instanceof EntityPlayerCorpse) {
                if (!((EntityPlayerCorpse) target).hasLifeStone()) {
                    ((EntityPlayerCorpse) target).setLifeStone(getQuality(stack));
                    stack.shrink(1);
                }
            } else {
                IPossessable corpse = AbstractMinion.createMinion(target);
                if (corpse instanceof EntityLivingBase) {
                    DissolutionInventoryHelper.transferEquipment(target, (EntityLivingBase) corpse);
                    target.world.spawnEntity((Entity) corpse);
                    target.world.removeEntity(target);
                }
            }
            return true;
        }
        return false;
    }

    public ItemStack withQuality(byte quality) {
        ItemStack ret = new ItemStack(this);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("lifeStone", quality);
        ret.setTagCompound(nbt);
        return ret;
    }

    private byte getQuality(ItemStack lifeStone) {
        NBTTagCompound compound = lifeStone.getTagCompound();
        if (compound == null)
            return 0;
        return compound.getByte("lifeStone");
    }

    public static boolean isGemUsed(int quality) {
        return quality >> 7 > 0;
    }
}
