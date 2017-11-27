package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.handlers.CustomDissolutionTeleporter;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemDebug extends Item implements ISoulInteractable {

    protected int debugWanted = 0;

    public ItemDebug() {
        super();
        this.setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                debugWanted = (debugWanted + 1) % 8;
                playerIn.sendStatusMessage(new TextComponentString("debug: " + debugWanted), true);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        switch (debugWanted) {
            case 0:
                CapabilityIncorporealHandler.getHandler(playerIn).setCorporealityStatus(
                        IIncorporealHandler.CorporealityStatus.values()
                                [(CapabilityIncorporealHandler.getHandler(playerIn).getCorporealityStatus().ordinal() + 1) % 3]);
                break;
            case 1:
                if (!worldIn.isRemote) worldIn.getWorldInfo().setAllowCommands(true);
                break;
            case 2:
                if (!playerIn.world.isRemote)
                    CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) playerIn, playerIn.dimension == -1 ? 0 : -1);
                break;
            case 3:
                if (!playerIn.world.isRemote) {
                    worldIn.loadedEntityList.stream().filter(e -> e instanceof EntityFleetingSoul).forEach(Entity::onKillCommand);
                    EntityFleetingSoul cam = new EntityFleetingSoul(playerIn.world, playerIn.posX + 2, playerIn.posY, playerIn.posZ);
                    worldIn.spawnEntity(cam);
                }
                break;
            case 4:
                if (!playerIn.world.isRemote) {
                    playerIn.sendStatusMessage(new TextComponentString("Printing fire information"), true);
                    List<Entity> fires = playerIn.world.getEntities(Entity.class, e -> e != null && e.getDistance(playerIn) < 20);
                    fires.forEach(System.out::println);
                }
                break;
            case 5: {
                AbstractMinion minion =
                        playerIn.world.findNearestEntityWithinAABB(AbstractMinion.class, new AxisAlignedBB(playerIn.getPosition()), null);
                if (minion != null) minion.onEntityPossessed(playerIn);
                break;
            }
            case 6: {
                if (!worldIn.isRemote) {
                    @SuppressWarnings("MethodCallSideOnly") RayTraceResult result = playerIn.rayTrace(6, 0);
                    TileEntity te = worldIn.getTileEntity(result.getBlockPos());
                    if (te != null && te.hasCapability(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, result.sideHit)) {
                        IDistillateHandler essentiaInv = te.getCapability(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, result.sideHit);
                        essentiaInv.forEach(System.out::println);
                        System.out.println(String.format("%s/%s (%s/%s)", essentiaInv.readContent(DistillateTypes.UNTYPED), essentiaInv.getMaxSize(), essentiaInv.getChannels(), essentiaInv.getMaxChannels()));
                        playerIn.sendStatusMessage(new TextComponentTranslation("suction: %s, type: %s", essentiaInv.getSuction(DistillateTypes.UNTYPED)), false);
                    }
                    if (te instanceof TileEntityModularMachine)
                        playerIn.sendStatusMessage(new TextComponentTranslation("modules: %s", ((TileEntityModularMachine) te).getInstalledModules()), false);
                }
                break;
            }
            case 7:
                ItemStack eye = new ItemStack(ModItems.EYE_OF_THE_UNDEAD);
                ModItems.EYE_OF_THE_UNDEAD.setShell(eye, new ItemStack(ModItems.GOLD_SHELL));
                playerIn.addItemStackToInventory(eye);
                break;
            default:
                break;
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
