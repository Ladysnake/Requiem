package ladysnake.dissolution.common.item;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.common.impl.DefaultRemnantHandler;
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
        RemnantHandler cap = ((DissolutionPlayer)player).getRemnantHandler();
        Dissolution.LOGGER.info("Player was {}", cap != null && cap.isIncorporeal() ? "incorporeal" : "corporeal");
        if (!world.isClient) {
            if (cap == null) {
                Dissolution.LOGGER.info("Turned {} into a remnant", player);
                ((DissolutionPlayer)player).setRemnantHandler(cap = new DefaultRemnantHandler(player));
            }
            cap.setIncorporeal(!cap.isIncorporeal());
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }
}
