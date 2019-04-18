package ladysnake.requiem.api.v1.entity;

import net.minecraft.util.math.Vec3d;

import javax.annotation.CheckForNull;

/**
 * A {@link MovementAlterer} alters the movement of an {@link net.minecraft.entity.Entity}
 * according to a {@link MovementConfig}.
 */
public interface MovementAlterer {

    void setConfig(@CheckForNull MovementConfig config);

    void applyConfig();

    void update();

    /**
     * Gets the acceleration that this entity has underwater.
     *
     * @param baseAcceleration the default acceleration computed in {@link net.minecraft.entity.LivingEntity#travel(Vec3d)}
     * @return the modified acceleration
     */
    float getSwimmingAcceleration(float baseAcceleration);
}
