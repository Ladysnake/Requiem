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
package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.ItemTooltipCallback;
import ladysnake.requiem.common.util.ItemUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;

import javax.annotation.Nullable;
import java.util.List;

public class PossessionTooltipCallback implements ItemTooltipCallback {
    @Override
    public void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, TooltipContext context, List<TextComponent> lines) {
        addPossessionTooltip:
        if (player != null) {
            LivingEntity possessed = (LivingEntity) ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
            String translationKey;
            if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof BowItem) {
                translationKey = "requiem:tooltip.skeletal_efficiency";
            } else if (possessed instanceof WitchEntity && ItemUtil.isWaterBottle(item)) {
                translationKey = "requiem:tooltip.witch_brew_base";
            } else {
                break addPossessionTooltip;
            }
            lines.add(TextFormatter.style(
                    new TranslatableTextComponent(translationKey),
                    new Style().setColor(TextFormat.DARK_GRAY)
            ));
        }
    }
}
