package ladysnake.requiem.mixin.possession.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link Possessable} on living entities
 */
@Mixin(LivingEntity.class)
abstract class PossessableLivingEntityMixin extends Entity implements Possessable {
    @Shadow
    public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute entityAttribute_1);

    @Shadow public abstract float getHealth();

    @Nullable
    private PlayerEntity possessor;

    public PossessableLivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public Optional<UUID> getPossessorUuid() {
        return Optional.ofNullable(this.possessor).map(PlayerEntity::getUuid);
    }

    @Override
    public boolean isBeingPossessed() {
        return this.possessor != null;
    }

    @Nullable
    @Override
    public PlayerEntity getPossessor() {
        if (this.possessor != null && this.possessor.removed) {
            ((RequiemPlayer)this.possessor).getPossessionComponent().stopPossessing();
            // Make doubly sure
            this.setPossessor(null);
        }
        return possessor;
    }

    @Override
    public boolean canBePossessedBy(PlayerEntity player) {
        return !this.removed && this.getHealth() > 0 && !this.isBeingPossessed();
    }

    @Override
    public MobAbilityController getMobAbilityController() {
        return MobAbilityController.DUMMY;
    }

    @Override
    public void setPossessor(@CheckForNull PlayerEntity possessor) {
        if (this.possessor != null && ((RequiemPlayer) this.possessor).getPossessionComponent().getPossessedEntity() == this) {
            throw new IllegalStateException("Players must stop possessing an entity before it can change possessor!");
        }
        this.possessor = possessor;
        this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(VanillaRequiemPlugin.INHERENT_MOB_SLOWNESS_UUID);
        if (possessor != null) {
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addModifier(VanillaRequiemPlugin.INHERENT_MOB_SLOWNESS);
        }
    }
}
