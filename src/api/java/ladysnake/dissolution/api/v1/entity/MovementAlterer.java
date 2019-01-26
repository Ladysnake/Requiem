package ladysnake.dissolution.api.v1.entity;

import javax.annotation.Nullable;

/**
 * A {@link MovementAlterer} alters the movement of an {@link net.minecraft.entity.Entity}
 * according to a {@link MovementConfig}.
 */
public interface MovementAlterer {
    @Nullable
    MovementConfig getConfig();

    void setConfig(MovementConfig config);

    void onMotionStateChanged();

    void update();
}
