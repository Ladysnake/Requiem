package ladysnake.dissolution.api.v1.remnant;

import net.minecraft.entity.player.PlayerEntity;

public interface RemnantType {
    RemnantState create(PlayerEntity player);
}
