package ladysnake.dissolution.mixin.server.world;

import ladysnake.dissolution.api.v1.DissolutionWorld;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import ladysnake.dissolution.common.impl.anchor.CommonAnchorManager;
import ladysnake.dissolution.common.impl.anchor.FractureAnchorPersistentState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements DissolutionWorld {
    private static final String PERSISTENT_STATE_KEY = "dissolution_anchor_provider";

    private final FractureAnchorManager anchorTracker = new CommonAnchorManager(this);

    protected ServerWorldMixin(LevelProperties props, DimensionType dim, BiFunction<World, Dimension, ChunkManager> biFunction, Profiler profiler, boolean bool) {
        super(props, dim, biFunction, profiler, bool);
    }

    /**
     * Registers a persistent state for this world to save anchors on a world basis
     */
    @Inject(
            at = @At("RETURN"),
            method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/WorldSaveHandler;Lnet/minecraft/world/level/LevelProperties;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/server/WorldGenerationProgressListener;)V"
    )
    private void constructor(MinecraftServer server, Executor executor, WorldSaveHandler oldWorldSaveHandler, LevelProperties levelProperties, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        PersistentStateManager persistentStateManager = ((ServerWorld) (Object) this).getPersistentStateManager();
        persistentStateManager.getOrCreate(() -> new FractureAnchorPersistentState(PERSISTENT_STATE_KEY, anchorTracker), PERSISTENT_STATE_KEY);
    }

    @Override
    public FractureAnchorManager getAnchorManager() {
        return this.anchorTracker;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void updateAnchorTracker(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        Profiler profiler = this.getProfiler();
        profiler.push("dissolution_ethereal_anchors");
        this.getAnchorManager().updateAnchors(this.properties.getTime());
        profiler.pop();
    }

}
