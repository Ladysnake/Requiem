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
package ladysnake.pandemonium.client;

import ladysnake.pandemonium.common.util.ItemUtil;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;
import java.util.List;

public class PossessionTooltipCallback implements ItemTooltipCallback {
    @Override
    public void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, TooltipContext context, List<Text> lines) {
        if (player != null) {
            LivingEntity possessed = PossessionComponent.get(player).getPossessedEntity();
            if (possessed == null) {
                return;
            }
            String translationKey;
            if (possessed instanceof WitchEntity && ItemUtil.isWaterBottle(item)) {
                translationKey = "pandemonium:tooltip.witch_brew_base";
            } else {    // More tooltips can easily be added here
                return;
            }
            lines.add(new TranslatableText(translationKey).styled(style -> style.withColor(Formatting.DARK_GRAY)));
        }
    }
}
