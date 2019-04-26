package ladysnake.requiem.common.item;

import ladysnake.requiem.client.gui.EditOpusScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ingame.EditBookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class OpusDemoniumItem extends WritableBookItem {
    public static final String CURSE_SENTENCE = "Make me eternal";
    public static final String CURE_SENTENCE = "Make me a mortal";

    public OpusDemoniumItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) {
            MinecraftClient.getInstance().openScreen(new EditOpusScreen(player, stack, hand));
        }
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }
}
