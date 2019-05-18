package ladysnake.requiem.mixin.world;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin {
    @ModifyArg(method = "getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BoundingBox;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;appendEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BoundingBox;Ljava/util/List;Ljava/util/function/Predicate;)V"), index = 3)
    private Predicate<Entity> ignorePossessed(Entity ignored, BoundingBox searchArea, List<Entity> foundEntities, Predicate<Entity> predicate) {
        if (ignored instanceof RequiemPlayer) {
            LivingEntity possessed = ((RequiemPlayer) ignored).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                Predicate<Entity> appendedPredicate = e -> e != possessed;
                return predicate == null ? appendedPredicate : predicate.and(appendedPredicate);
            }
        }
        return predicate;
    }
}
