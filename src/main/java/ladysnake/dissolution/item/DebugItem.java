package ladysnake.dissolution.item;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.Remnant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DebugItem extends Item {
    public DebugItem(Settings item$Settings_1) {
        super(item$Settings_1);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Remnant remnant = (Remnant) player;
        Dissolution.LOGGER.info("Player is {}", remnant.isIncorporeal() ? "incorporeal" : "corporeal");
        remnant.setIncorporeal(!remnant.isIncorporeal());
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }
}
