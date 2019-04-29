/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.player.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
import net.minecraft.util.math.BlockPos;
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

    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.LECTERN) {
            return LecternBlock.putBookIfAbsent(world, pos, state, ctx.getItemStack()) ? ActionResult.SUCCESS : ActionResult.PASS;
        } else {
            return ActionResult.PASS;
        }
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
                RequiemNetworking.sendTo((ServerPlayerEntity) player, RequiemNetworking.createOpusUsePacket(cure, true));
            }
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }
        return super.use(world, player, hand);
    }

    @Environment(EnvType.CLIENT)
    public void buildTooltip(ItemStack stack, @Nullable World world, List<TextComponent> lines, TooltipContext ctx) {
        lines.add(new TranslatableTextComponent(this == RequiemItems.OPUS_DEMONIUM_CURE ? "requiem:opus_daemonium.cure" : "requiem:opus_daemonium.curse")
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
