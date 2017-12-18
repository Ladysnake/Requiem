package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.blocks.BlockSepulchre;
import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemSepulture extends Item {

    public ItemSepulture() {
        super();
        this.setMaxStackSize(1);
    }

    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            boolean flag = block.isReplaceable(worldIn, pos);

            if (!flag) {
                pos = pos.up();
            }

            ItemStack itemstack = player.getHeldItem(hand);
            // First we check that the area meets the requirements to place the burial
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos pos1 = pos.add(i, 0, j);
                    IBlockState state = worldIn.getBlockState(pos1);
                    boolean canEdit = player.canPlayerEdit(pos1, facing, itemstack);
                    boolean replaceable = state.getBlock().isReplaceable(worldIn, pos1);
                    boolean solidFloor = worldIn.getBlockState(pos1.down()).isOpaqueCube();
                    if(!canEdit || !replaceable || !solidFloor)
                        return EnumActionResult.FAIL;
                }
            }

            int i = MathHelper.floor((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing enumfacing = EnumFacing.getHorizontal(i);

            IBlockState baseState = ModBlocks.SEPULTURE.getDefaultState()
                    .withProperty(BlockSepulchre.FACING, enumfacing)
                    .withProperty(BlockSepulchre.PART, BlockSepulchre.EnumPartType.CENTER);

            // Need to place the center first to avoid sides popping on update
            IBlockState prevState = worldIn.getBlockState(pos);
            worldIn.setBlockState(pos, baseState, 10);
            worldIn.notifyNeighborsRespectDebug(pos, prevState.getBlock(), false);

            IBlockState state = baseState.withProperty(BlockSepulchre.PART, BlockSepulchre.EnumPartType.SIDE);

            // Then we actually place all the burial blocks
            for (i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // the center is already placed
                    BlockPos pos1 = pos.add(i, 0, j);
                    prevState = worldIn.getBlockState(pos1);
                    worldIn.setBlockState(pos1, state, 10);
                    worldIn.notifyNeighborsRespectDebug(pos1, prevState.getBlock(), false);
                }
            }

            // Finish by playing the sound and stuff
            SoundType soundtype = baseState.getBlock().getSoundType(baseState, worldIn, pos, player);
            worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }
}
