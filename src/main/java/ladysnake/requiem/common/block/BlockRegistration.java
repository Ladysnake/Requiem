package ladysnake.requiem.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public record BlockRegistration(String name, @Nullable BlockItemRegistration blockItemRegistration) {
    public record BlockItemRegistration(ItemGroup group, BiFunction<Block, Item.Settings, BlockItem> blockItemFactory) {

    }
}
