package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
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
    private final TextFormat color;

    public WrittenOpusItem(RemnantType remnantType, TextFormat color, Settings settings) {
        super(settings);
        this.remnantType = remnantType;
        this.color = color;
    }

    public RemnantType getRemnantType() {
        return remnantType;
    }

    public TextFormat getTooltipColor() {
        return color;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient && stack.getItem() == this) {
            RemnantType currentState = ((RequiemPlayer) player).getRemnantState().getType();
            if (currentState != this.remnantType && !((RequiemPlayer) player).getPossessionComponent().isPossessing()) {
                ((RequiemPlayer) player).setRemnantState(remnantType.create(player));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                stack.subtractAmount(1);
                boolean cure = this == RequiemItems.OPUS_DEMONIUM_CURE;
                world.playSound(null, player.x, player.y, player.z, SoundEvents.ITEM_TOTEM_USE, player.getSoundCategory(), 1.0F, 0.1F);
                world.playSound(null, player.x, player.y, player.z, cure ? SoundEvents.BLOCK_BEACON_DEACTIVATE : SoundEvents.BLOCK_BEACON_ACTIVATE, player.getSoundCategory(), 1.4F, 0.1F);
                RequiemNetworking.sendTo((ServerPlayerEntity) player, RequiemNetworking.createOpusUsePacket(cure));
            }
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }
        return super.use(world, player, hand);
    }

    @Environment(EnvType.CLIENT)
    public void buildTooltip(ItemStack stack, @Nullable World world, List<TextComponent> lines, TooltipContext ctx) {
        lines.add(new TranslatableTextComponent(this == RequiemItems.OPUS_DEMONIUM_CURE ? "requiem.opus_daemonium.cure" : "requiem.opus_daemonium.curse")
                .applyFormat(this.getTooltipColor()));
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
