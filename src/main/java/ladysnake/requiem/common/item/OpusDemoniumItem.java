package ladysnake.requiem.common.item;

import ladysnake.requiem.client.gui.EditOpusScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.stat.Stats;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class OpusDemoniumItem extends WritableBookItem {

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

    @Override
    public void buildTooltip(ItemStack stack, @Nullable World world, List<TextComponent> lines, TooltipContext ctx) {
        lines.add(new TranslatableTextComponent("requiem.opus_daemonium.curse").applyFormat(RequiemItems.OPUS_DEMONIUM_CURSE.getTooltipColor()));
        lines.add(new TranslatableTextComponent("requiem.opus_daemonium.cure").applyFormat(RequiemItems.OPUS_DEMONIUM_CURE.getTooltipColor()));
    }
}
