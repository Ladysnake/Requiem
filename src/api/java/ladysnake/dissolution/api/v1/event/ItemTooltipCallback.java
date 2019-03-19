package ladysnake.dissolution.api.v1.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;

import javax.annotation.Nullable;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface ItemTooltipCallback {
    void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, TooltipContext context, List<TextComponent> lines);

    Event<ItemTooltipCallback> EVENT = EventFactory.createArrayBacked(ItemTooltipCallback.class,
            (listeners) -> (item, player, context, lines) -> {
                for (ItemTooltipCallback callback : listeners) {
                    callback.onTooltipBuilt(item, player, context, lines);
                }
            });
}
