package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ItemEyeOfDiscord extends ItemOcculare {

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {

        if (!(entityLiving instanceof EntityPlayer) || this.getMaxItemUseDuration(stack) - timeLeft < 30) return;
        EntityPlayer player = (EntityPlayer) entityLiving;

        if (CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal() && !player.isCreative())
            return;

        ItemStack ammo = DissolutionInventoryHelper.findItem(player, ModItems.SOUL_IN_A_FLASK);

        List<EntityLiving> entities = (worldIn.getEntitiesWithinAABB(EntityLiving.class,
                new AxisAlignedBB(Math.floor(entityLiving.posX), Math.floor(entityLiving.posY), Math.floor(entityLiving.posZ),
                        Math.floor(entityLiving.posX) + 1, Math.floor(entityLiving.posY) + 1, Math.floor(entityLiving.posZ) + 1)
                        .grow(20), e -> e != null && !(e instanceof AbstractMinion) && e.isNonBoss()));

        boolean used = false;
        Collections.shuffle(entities);
        if (!worldIn.isRemote)
            while (entities.size() > 1) {    // swaps AI tasks of entities two by two

                EntityLiving entityLiving1 = entities.remove(1);
                EntityLiving entityLiving2 = entities.remove(0);
                Set<EntityAITasks.EntityAITaskEntry> tasks1 = new LinkedHashSet<>(entityLiving1.tasks.taskEntries);
                Set<EntityAITasks.EntityAITaskEntry> targetTasks1 = new LinkedHashSet<>(entityLiving1.targetTasks.taskEntries);
                Set<EntityAITasks.EntityAITaskEntry> tasks2 = new LinkedHashSet<>(entityLiving2.tasks.taskEntries);
                Set<EntityAITasks.EntityAITaskEntry> targetTasks2 = new LinkedHashSet<>(entityLiving2.targetTasks.taskEntries);

                tasks1.forEach(ai -> entityLiving1.tasks.removeTask(ai.action));
                entityLiving1.tasks.taskEntries.addAll(tasks2);
                targetTasks1.forEach(ai -> entityLiving1.targetTasks.removeTask(ai.action));
                entityLiving1.targetTasks.taskEntries.addAll(targetTasks2);
                entityLiving1.spawnExplosionParticle();

                tasks2.forEach(ai -> entityLiving2.tasks.removeTask(ai.action));
                entityLiving2.tasks.taskEntries.addAll(tasks1);
                targetTasks2.forEach(ai -> entityLiving2.targetTasks.removeTask(ai.action));
                entityLiving2.targetTasks.taskEntries.addAll(targetTasks1);
                entityLiving2.spawnExplosionParticle();

                used = true;
            }
        if (used) {
            stack.damageItem(1, player);
            ammo.shrink(1);
            StatBase stat = StatList.getObjectUseStats(this);
            if (stat != null)
                player.addStat(stat);
        }
    }

}
