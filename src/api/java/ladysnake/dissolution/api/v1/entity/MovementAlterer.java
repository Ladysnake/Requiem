package ladysnake.dissolution.api.v1.entity;

import javax.annotation.CheckForNull;

/**
 * A {@link MovementAlterer} alters the movement of an {@link net.minecraft.entity.Entity}
 * according to a {@link MovementConfig}.
 */
public interface MovementAlterer {

    void setConfig(@CheckForNull MovementConfig config);

    void applyConfig();

    void update();
}
