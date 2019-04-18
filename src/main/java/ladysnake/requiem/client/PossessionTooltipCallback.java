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
