package ladysnake.pandemonium.mixin.common.entity.player;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RequiemPlayer{

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void update(CallbackInfo info) {
        // TODO move to a player tick event when we have one
        if (!this.world.isClient && this.isSneaking()) {
            PossessionComponent poss = this.asPossessor();
            if (poss.getPossessedEntity() instanceof ShulkerEntity) {
                poss.stopPossessing();
            }
        }
    }

    /**
     * Return a {@code PlayerEntity} instance that corresponds to this player.
     * Calling {@link #from(PlayerEntity)} on the returned value returns {@code this} instance.
     *
     * @return {@code this} as a {@link PlayerEntity}
     * @since 1.0.0
     */
    @Contract(pure = true)
    public abstract PlayerEntity asPlayer();
}
