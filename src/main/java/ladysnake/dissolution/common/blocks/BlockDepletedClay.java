package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockDepletedClay extends BlockDepleted {

    public BlockDepletedClay() {
        super(Material.CLAY);
    }

    @Nonnull
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.DEPLETED_CLAY;
    }

    public int quantityDropped(Random random) {
        return 4;
    }

}
