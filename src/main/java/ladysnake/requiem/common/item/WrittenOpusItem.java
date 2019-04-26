package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WrittenOpusItem extends Item {
    private final RemnantType remnantType;

    public WrittenOpusItem(RemnantType remnantType, Settings settings) {
        super(settings);
        this.remnantType = remnantType;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient && stack.getItem() == this) {
            RemnantType currentState = ((RequiemPlayer)player).getRemnantState().getType();
            if (currentState != this.remnantType) {
                ((RequiemPlayer) player).setRemnantState(remnantType.create(player));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                stack.subtractAmount(1);
                return new TypedActionResult<>(ActionResult.SUCCESS, stack);
            }
        }
        return super.use(world, player, hand);
    }
}
