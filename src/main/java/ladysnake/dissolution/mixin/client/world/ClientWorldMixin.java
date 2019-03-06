package ladysnake.dissolution.mixin.client.world;

import ladysnake.dissolution.api.v1.DissolutionWorld;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import ladysnake.dissolution.client.ClientAnchorManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiFunction;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements DissolutionWorld {
    private final FractureAnchorManager anchorManager = new ClientAnchorManager(this);

    protected ClientWorldMixin(LevelProperties props, DimensionType dim, BiFunction<World, Dimension, ChunkManager> biFunction_1, Profiler profiler_1, boolean boolean_1) {
        super(props, dim, biFunction_1, profiler_1, boolean_1);
    }

    @Override
    public FractureAnchorManager getAnchorManager() {
        return this.anchorManager;
    }
}
