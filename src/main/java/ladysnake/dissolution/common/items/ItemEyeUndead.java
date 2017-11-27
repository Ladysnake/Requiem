package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ItemEyeUndead extends ItemOcculare {

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {

        if (!(entityLiving instanceof EntityPlayer) || this.getMaxItemUseDuration(stack) - timeLeft < 30) return;
        EntityPlayer player = (EntityPlayer) entityLiving;

        if (CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal() && !player.isCreative())
            return;

        ItemStack ammo = DissolutionInventoryHelper.findItem(player, ModItems.SOUL_IN_A_FLASK);

        List<AbstractMinion> minions = (worldIn.getEntitiesWithinAABB(AbstractMinion.class,
                new AxisAlignedBB(Math.floor(entityLiving.posX), Math.floor(entityLiving.posY), Math.floor(entityLiving.posZ),
                        Math.floor(entityLiving.posX) + 1, Math.floor(entityLiving.posY) + 1, Math.floor(entityLiving.posZ) + 1)
                        .grow(20)));

        if (minions.isEmpty()) return;

        boolean used = false;
        if (!(minions instanceof EntityPlayer)) {
            for (AbstractMinion m : minions) {
                if (ammo.isEmpty() && m.isInert()) {
                    ((EntityPlayer) entityLiving).sendStatusMessage(new TextComponentTranslation(this.getUnlocalizedName() + ".nosoul"), true);
                    break;
                }
                if (worldIn.isRemote) {
                    for (int i = 0; i < (m.isInert() ? 50 : 5); i++) {
                        Random rand = new Random();
                        double motionX = rand.nextGaussian() * 0.1D;
                        double motionY = rand.nextGaussian() * 0.1D;
                        double motionZ = rand.nextGaussian() * 0.1D;
                        worldIn.spawnParticle(m.isInert() ? EnumParticleTypes.DRAGON_BREATH : EnumParticleTypes.CLOUD, m.posX, m.posY + 1.0D, m.posZ, motionX, motionY, motionZ);
                    }
                }
                if (m.isInert()) {
                    ammo.shrink(1);
                    used = true;
                }
                if (!worldIn.isRemote) {
                    m.setOwnerId(player.getUniqueID());
                }
            }

        }
        if (used) {
            stack.damageItem(1, player);
            StatBase stat = StatList.getObjectUseStats(this);
            if (stat != null)
                player.addStat(stat);
        }
    }

}
