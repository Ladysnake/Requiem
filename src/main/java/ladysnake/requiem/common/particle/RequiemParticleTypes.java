package ladysnake.requiem.common.particle;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;

public class RequiemParticleTypes {
    public static final DefaultParticleType GHOST = FabricParticleTypes.simple(true);

    public static void init() {
        Registry.register(Registry.PARTICLE_TYPE, Requiem.id("ghost"), GHOST);
    }
}
