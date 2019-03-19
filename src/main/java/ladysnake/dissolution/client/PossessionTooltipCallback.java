package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.ItemTooltipCallback;
import ladysnake.dissolution.common.item.ItemUtil;
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
            LivingEntity possessed = (LivingEntity) ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
            String translationKey;
            if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof BowItem) {
                translationKey = "dissolution:tooltip.skeletal_efficiency";
            } else if (possessed instanceof WitchEntity && ItemUtil.isWaterBottle(item)) {
                translationKey = "dissolution:tooltip.witch_brew_base";
            } else {
                break addPossessionTooltip;
            }
            lines.add(TextFormatter.addStyle(
                    new TranslatableTextComponent(translationKey),
                    new Style().setColor(TextFormat.DARK_GRAY)
            ));
        }
    }
}
