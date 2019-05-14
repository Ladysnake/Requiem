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

import ladysnake.requiem.client.gui.EditOpusScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class OpusDemoniumItem extends WritableBookItem {
    public static final int REQUIRED_CONVERSION_XP = 5;

    public OpusDemoniumItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) {
            openScreen(player, hand, stack);
        }
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(PlayerEntity player, Hand hand, ItemStack stack) {
        MinecraftClient.getInstance().openScreen(new EditOpusScreen(player, stack, hand));
    }

    @Override
    public void buildTooltip(ItemStack stack, @Nullable World world, List<Component> lines, TooltipContext ctx) {
        addTooltipLine(lines, RequiemItems.OPUS_DEMONIUM_CURSE);
        addTooltipLine(lines, RequiemItems.OPUS_DEMONIUM_CURE);
    }

    private void addTooltipLine(List<Component> lines, WrittenOpusItem version) {
        lines.add(new TranslatableComponent(version.getRemnantType().getConversionBookSentence()).applyFormat(version.getTooltipColor()));
    }
}
