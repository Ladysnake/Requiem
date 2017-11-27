package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAcerbacaFruit extends ItemFood {

    private IIncorporealHandler.CorporealityStatus corporealChange;

    public ItemAcerbacaFruit(int amount, float saturation, IIncorporealHandler.CorporealityStatus corporealChange) {
        super(amount, saturation, false);
        this.setAlwaysEdible();
        this.corporealChange = corporealChange;
    }

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, @Nonnull World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);
        return ItemStack.EMPTY;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, @Nonnull EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        split(player, this.corporealChange);
    }

    public static void split(EntityPlayer owner, IIncorporealHandler.CorporealityStatus newStatus) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(owner);
        if (handler.getCorporealityStatus().isIncorporeal() || owner.world.isRemote || !handler.isStrongSoul())
            return;

        EntityPlayerCorpse body = new EntityPlayerCorpse(owner.world);
        body.setPosition(owner.posX, owner.posY, owner.posZ);
        body.setPlayer(owner.getUniqueID());
        body.setInert(true);
        body.setDecompositionCountdown(-1);
        DissolutionInventoryHelper.transferEquipment(owner, body);
        body.setCustomNameTag(owner.getName());
        body.setInventory(new InventoryPlayerCorpse(owner.inventory.mainInventory, body));
        owner.inventory.clear();
        body.setPlayer(owner.getUniqueID());
        owner.world.spawnEntity(body);

        handler.setCorporealityStatus(newStatus);
    }


}
