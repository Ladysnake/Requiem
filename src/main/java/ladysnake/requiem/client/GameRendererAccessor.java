package ladysnake.requiem.client;

import net.minecraft.entity.Entity;

public interface GameRendererAccessor {
    boolean requiem_isEligibleForTargeting(Entity tested);
}
