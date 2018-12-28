package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerShell;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ItemAethereus extends Item {

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        EntityPlayer player = entityLiving instanceof EntityPlayer ? (EntityPlayer) entityLiving : null;

        if (player == null || !player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
        }

        if (!worldIn.isRemote) {
            Optional<IIncorporealHandler> handler = CapabilityIncorporealHandler.getHandler(entityLiving);
            if (handler.isPresent()) {
                if (handler.get().isStrongSoul()) {
                    EntityLivingBase possessed = handler.get().getPossessed();
                    if (possessed != null) {
                        handler.get().setPossessed(null, true);
                        if (player instanceof EntityPlayerMP) {
                            DissolutionInventoryHelper.transferEquipment(player, possessed);
                            player.inventory.dropAllItems();
                            ((EntityPlayerMP)player).connection.setPlayerLocation(player.posX, player.posY + 1, player.posZ, player.rotationYaw, player.rotationPitch);
                            player.motionY += 1f;
                            player.velocityChanged = true;
                        }
                        return ItemStack.EMPTY;
                    } else {
                        EntityPlayerShell shell = new EntityPlayerShell(worldIn);
                        shell.setPositionAndRotation(entityLiving.posX, entityLiving.posY, entityLiving.posZ, entityLiving.rotationYaw, entityLiving.rotationPitch);
                        shell.setPlayer(entityLiving.getUniqueID());
                        shell.setCustomNameTag(entityLiving.getName());
                        shell.setLeftHanded(entityLiving.getPrimaryHand() == EnumHandSide.LEFT);
                        DissolutionInventoryHelper.transferEquipment(entityLiving, shell);
                        if (player != null) {
                            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                                shell.getInventory().setInventorySlotContents(i, player.inventory.mainInventory.get(i));
                                player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                            }
                        }
                        shell.setHeldItem(entityLiving.getActiveHand(), new ItemStack(Items.GLASS_BOTTLE));
                        worldIn.spawnEntity(shell);
                        handler.get().setCorporealityStatus(SoulStates.SOUL);
                        return ItemStack.EMPTY;
                    }
                } else {
                    entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60 * 20));
                    handler.get().setStrongSoul(true);
                    if (player instanceof EntityPlayerMP) {
                        TextComponentTranslation text = new TextComponentTranslation("dissolution.soul_upgrade");
                        text.getStyle().setItalic(true);
                        player.sendStatusMessage(text, false);
                    }
                }
            }
        }

        return new ItemStack(Items.GLASS_BOTTLE);
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
