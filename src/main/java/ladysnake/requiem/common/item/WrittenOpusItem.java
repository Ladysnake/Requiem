package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.Stats;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

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
            if (currentState != this.remnantType && !((RequiemPlayer) player).getPossessionComponent().isPossessing()) {
                ((RequiemPlayer) player).setRemnantState(remnantType.create(player));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                stack.subtractAmount(1);
                return new TypedActionResult<>(ActionResult.SUCCESS, stack);
            }
        }
        return super.use(world, player, hand);
    }

    @Environment(EnvType.CLIENT)
    public void buildTooltip(ItemStack stack, @Nullable World world, List<TextComponent> lines, TooltipContext ctx) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            String author = tag.getString("author");
            if (!ChatUtil.isEmpty(author)) {
                lines.add((new TranslatableTextComponent("book.byAuthor", author)).applyFormat(TextFormat.GRAY));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public boolean hasEnchantmentGlint(ItemStack stack) {
        return true;
    }
}
