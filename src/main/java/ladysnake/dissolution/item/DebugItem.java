package ladysnake.dissolution.item;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantCapability;
import ladysnake.dissolution.remnant.DefaultRemnantHandler;
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
        RemnantCapability cap = ((DissolutionPlayer)player).getRemnantCapability();
        if (cap == null) {
            Dissolution.LOGGER.info("Turned {} into a remnant", player);
            ((DissolutionPlayer)player).setRemnantCapability(cap = new DefaultRemnantHandler(player));
        }
        Dissolution.LOGGER.info("Player is {}", cap.isIncorporeal() ? "incorporeal" : "corporeal");
        cap.setIncorporeal(!cap.isIncorporeal());
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }
}
