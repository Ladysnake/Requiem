package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class BlockPurificationCauldron extends BlockCauldron {

    private static final Map<ItemStack, ItemStack> RECIPES = new HashMap<>();

    @GameRegistry.ItemStackHolder("thaumcraft:brain")
    public static final ItemStack TC_BRAIN = ItemStack.EMPTY;
    private static final boolean INSPIRATIONS_LOADED = Loader.isModLoaded("inspirations");

    @SubscribeEvent
    public static void initRecipes(RegistryEvent.Register<IRecipe> event) {
        RECIPES.put(new ItemStack(Items.ROTTEN_FLESH), new ItemStack(ModItems.HUMAN_FLESH_RAW));
        if (TC_BRAIN != null) {
            RECIPES.put(TC_BRAIN, new ItemStack(ModItems.HUMAN_BRAIN));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != Items.GHAST_TEAR || INSPIRATIONS_LOADED) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() == Blocks.CAULDRON && state.getValue(LEVEL) > 0) {
            event.getWorld().setBlockState(event.getPos(), ModBlocks.PURIFICATION_CAULDRON.getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)));
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = playerIn.getHeldItem(hand);
        // people tend to click cauldrons with fluid items, this prevents water from spilling everywhere
        if (itemstack.isEmpty() || FluidUtil.getFluidHandler(itemstack) != null) {
            return true;
        }
        ItemStack split = itemstack.copy();
        split.setCount(1);
        // Mojang gib itemstack maps please
        for (Map.Entry<ItemStack, ItemStack> entry : RECIPES.entrySet()){
            if (ItemStack.areItemStacksEqual(entry.getKey(), split)) {
                itemstack.shrink(1);
                playerIn.addItemStackToInventory(entry.getValue().copy());
                this.setWaterLevel(worldIn, pos, state, state.getValue(LEVEL) - 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setWaterLevel(World worldIn, @Nonnull BlockPos pos, IBlockState state, int level) {
        int currentLevel = state.getValue(LEVEL);
        // no you cannot simply refill this cauldron with water
        if (currentLevel > level) {
            IBlockState newState;
            if (level > 0) {
                newState = state.withProperty(LEVEL, MathHelper.clamp(level, 0, 3));
            } else {    // if the cauldron is empty, go back to normal cauldron
                newState = Blocks.CAULDRON.getDefaultState();
            }
            worldIn.setBlockState(pos, newState, 0b10);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
    }
}
