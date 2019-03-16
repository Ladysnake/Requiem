package ladysnake.dissolution.common.impl.possession.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.annotation.CalledThroughReflection;
import ladysnake.dissolution.api.v1.entity.ability.*;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.DissolutionRegistries;
import ladysnake.dissolution.common.entity.ai.InertGoal;
import ladysnake.dissolution.common.entity.ai.attribute.AttributeHelper;
import ladysnake.dissolution.common.entity.ai.attribute.CooldownStrengthAttribute;
import ladysnake.dissolution.mixin.entity.LivingEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PossessableEntityImpl extends PossessableEntityBase implements Possessable, MobAbilityController {
    public static final UUID INHERENT_MOB_SLOWNESS_UUID = UUID.fromString("a2ebbb6b-fd10-4a30-a0c7-dadb9700732e");
    /**
     * Mobs do not use 100% of their movement speed attribute, so we compensate with this modifier when they are possessed
     */
    public static final EntityAttributeModifier INHERENT_MOB_SLOWNESS = new EntityAttributeModifier(
            INHERENT_MOB_SLOWNESS_UUID,
            "Inherent Mob Slowness",
            -0.66,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
    ).setSerialize(false);
    private @Nullable UUID possessorUuid;
    private IndirectAbility<? super PossessableEntityImpl> indirectAttack;
    private IndirectAbility<? super PossessableEntityImpl> indirectInteraction;
    private DirectAbility<? super PossessableEntityImpl> directAttack;
    private DirectAbility<? super PossessableEntityImpl> directInteraction;

    @CalledThroughReflection
    @SuppressWarnings("unchecked")
    public PossessableEntityImpl(MobEntityWithAi cloned) {
        this((EntityType<? extends MobEntityWithAi>) cloned.getType(), cloned.world);
    }

    @CalledThroughReflection
    public PossessableEntityImpl(World world) {
        super(world);
    }

    public PossessableEntityImpl(EntityType<? extends MobEntityWithAi> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    /* * * * * * * * * * * * * *
      Interfaces implementation
     * * * * * * * * * * * * * */

    private void configure(MobAbilityConfig<? super PossessableEntityImpl> config) {
        this.directAttack = config.getDirectAbility(this, AbilityType.ATTACK);
        this.directInteraction = config.getDirectAbility(this, AbilityType.INTERACT);
        this.indirectAttack = config.getIndirectAbility(this, AbilityType.ATTACK);
        this.indirectInteraction = config.getIndirectAbility(this, AbilityType.INTERACT);
    }

    @Override
    public Optional<UUID> getPossessorUuid() {
        return Optional.ofNullable(possessorUuid);
    }

    @Override
    public Optional<PlayerEntity> getPossessor() {
        // method_18470 == getPlayerByUuid
        return getPossessorUuid().map(world::method_18470);
    }

    @Nullable
    @Override
    public PlayerEntity getPossessorEntity() {
        return getPossessor().orElse(null);
    }

    @Override
    public boolean canBePossessedBy(PlayerEntity player) {
        return !this.isBeingPossessed();
    }

    @Override
    public MobAbilityController getMobAbilityController() {
        return this;
    }

    @Override
    public void setPossessor(@CheckForNull PlayerEntity possessor) {
        if (this.getPossessor().map(p -> ((DissolutionPlayer)p).getPossessionComponent().getPossessedEntity()).filter(this::equals).isPresent()) {
            throw new IllegalStateException("Players must stop possessing an entity before it can change possessor!");
        }
        if (possessor != null) {
            this.possessorUuid = possessor.getUuid();
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addModifier(INHERENT_MOB_SLOWNESS);
        } else {
            this.possessorUuid = null;
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(INHERENT_MOB_SLOWNESS_UUID);
        }
    }

    @Override
    public void onPossessorFalls(float fallDistance, double double_1, boolean boolean_1, BlockState blockState_1, BlockPos blockPos_1) {
        this.fallDistance = fallDistance;
        this.method_5623(double_1, boolean_1, blockState_1, blockPos_1);
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        if (type == AbilityType.ATTACK) {
            return this.getPossessor().map(p -> directAttack.trigger(p, target)).orElse(false);
        } else if (type == AbilityType.INTERACT) {
            return this.getPossessor().map(p -> directInteraction.trigger(p, target)).orElse(false);
        }
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        if (type == AbilityType.ATTACK) {
            return this.getPossessor().map(indirectAttack::trigger).orElse(false);
        } else if (type == AbilityType.INTERACT) {
            return this.getPossessor().map(indirectInteraction::trigger).orElse(false);
        }
        return false;
    }

    @Override
    public void updateAbilities() {
        if (!this.world.isClient) {
            this.directAttack.update();
            this.indirectAttack.update();
            this.directInteraction.update();
            this.indirectInteraction.update();
        }
    }

    /* * * * * * * * * * *
        Entity overrides
    * * * * * * * * * * */

    @Override
    protected void initAttributes() {
        super.initAttributes();
        if (this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) {
            AttributeHelper.substituteAttributeInstance(this.getAttributeContainer(), new CooldownStrengthAttribute(this));
        }
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(99, new InertGoal(this));
        this.configure(DissolutionRegistries.ABILITIES.getConfig(this));
    }

    @Override
    public void update() {
        // Make possessed monsters despawn gracefully
        this.getPossessor().ifPresent(player -> {
            if (!this.world.isClient) {
                if (this instanceof Monster && this.world.getDifficulty() == Difficulty.PEACEFUL) {
                    player.addChatMessage(new TranslatableTextComponent("dissolution.message.peaceful_despawn"), true);
                }
            }
            // Set the player's hit timer for damage animation and stuff
            player.field_6008 = this.field_6008;
            player.setAbsorptionAmount(this.getAbsorptionAmount());
        });
        super.update();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource_1) {
        // Prevent possessed entities from hitting themselves
        return super.isInvulnerableTo(damageSource_1) ||
                this.getPossessor().filter(p -> p.isCreative() || p == damageSource_1.getAttacker()).isPresent();
    }

    /**
     * Called by living entities each tick to process the move logic
     */
    @Override
    public void travel(Vec3d direction) {
        // Always set the entity at the possessor's position
        Optional<PlayerEntity> optionalPlayer = this.getPossessor();
        if (optionalPlayer.isPresent()) {
            PlayerEntity player = optionalPlayer.get();
            this.setRotation(player.yaw, player.pitch);
            this.headYaw = this.field_6283 = this.prevYaw = this.yaw;
            this.method_5796(player.isSwimming());
            // Prevent this entity from taking fall damage unless triggered by the possessor
            this.fallDistance = 0;

            super.travel(direction);
            this.setPosition(player.x, player.y, player.z);
            // update limb movement
            this.field_6249 = player.field_6249;
            this.field_6225 = player.field_6225;
        } else {
            super.travel(direction);
        }
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        // Allows this entity to move client-side, in conjunction with #method_5956
        return this.getPossessor().map(p -> (Entity)p)
                .orElseGet(super::getPrimaryPassenger);
    }

    @Override
    public boolean method_5956() {
        // Allows this entity to move client side
        return super.method_5956() || this.isBeingPossessed();
    }

    @Override
    public void pushAwayFrom(Entity entityIn) {
        // Prevent infinite propulsion through self collision
        if (!getPossessorUuid().filter(entityIn.getUuid()::equals).isPresent()) {
            super.pushAwayFrom(entityIn);
        }
    }

    @Override
    protected void pushAway(Entity entityIn) {
        // Prevent infinite propulsion through self collision
        if (!getPossessorUuid().filter(entityIn.getUuid()::equals).isPresent()) {
            super.pushAwayFrom(entityIn);
        }
    }

    @Override
    public boolean canUsePortals() {
        // This entity's dimension should always be changed by the player
        return !isBeingPossessed() && super.canUsePortals();
    }

    @Override
    public void onDeath(DamageSource damageSource_1) {
        super.onDeath(damageSource_1);
        // Drop player inventory on death
        this.getPossessor().ifPresent(possessor -> {
            ((DissolutionPlayer)possessor).getPossessionComponent().stopPossessing();
            if (!world.isClient && !possessor.isCreative() && !world.getGameRules().getBoolean("keepInventory")) {
                possessor.inventory.dropAll();
            }
        });

    }

    /**
     * Updates the logic for the held item when the hand is active
     */
    @Override
    public void method_6076() {
        if (!getPossessorUuid().isPresent()) {
            super.method_6076();
        }
    }

    /* * * * * * * * * * *
      Plenty of delegation
     * * * * * * * * * * * */

    /**
     * Knockback
     */
    @Override
    public void method_6005(Entity entity_1, float float_1, double double_1, double double_2) {
        Optional<PlayerEntity> possessing = getPossessor();
        if (possessing.isPresent()) {
            PlayerEntity player = possessing.get();
            player.method_6005(entity_1, float_1, double_1, double_2);
        } else {
            super.method_6005(entity_1, float_1, double_1, double_2);
        }
    }

    @Override
    protected void scheduleVelocityUpdate() {
        super.scheduleVelocityUpdate();
        if (!world.isClient && this.velocityModified) {
            this.getPossessor().ifPresent(player -> player.velocityModified = true);
        }
    }

    /**
     * Teleport
     *
     * @param enderTp <code>true</code> for ender particles and sound effect
     * @return <code>true</code> if the teleportation is successful, otherwise <code>false</code>
     */
    @Override
    public boolean method_6082(double x, double y, double z, boolean enderTp) {
        return getPossessor()
                .map(p -> p.method_6082(x, y, z, enderTp))
                .orElseGet(() -> super.method_6082(x, y, z, enderTp));
    }

    @Override
    public boolean startRiding(Entity entity_1) {
        Optional<PlayerEntity> possessing = getPossessor();
        return possessing
                .map(playerEntity -> playerEntity.startRiding(entity_1))
                .orElseGet(() -> super.startRiding(entity_1));
    }

    @Override
    public boolean isFallFlying() {
        return super.isFallFlying() || getPossessor().filter(PlayerEntity::isFallFlying).isPresent();
    }

    /**
     * @return Whether this entity is using a shield or equivalent
     */
    @Override
    public boolean method_6039() {
        Optional<PlayerEntity> possessing = getPossessor();
        return possessing
                .map(LivingEntity::method_6039)
                .orElseGet(super::method_6039);
    }

    @Override
    protected void damageShield(float float_1) {
        Optional<PlayerEntity> possessing = getPossessor();
        if (possessing.filter(p -> !p.world.isClient).isPresent()) {
            ((LivingEntityAccessor)possessing.get()).invokeDamageShield(float_1);
            this.world.summonParticle(possessing.get(), (byte)29);
        } else {
            super.damageShield(float_1);
        }
    }

    @Override
    public Iterable<ItemStack> getItemsHand() {
        return getPossessor()
                .map(PlayerEntity::getItemsHand)
                .orElseGet(super::getItemsHand);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot var1) {
        return getPossessor()
                .map(playerEntity -> playerEntity.getEquippedStack(var1))
                .orElseGet(() -> super.getEquippedStack(var1));
    }

    @Override
    public ItemStack getActiveItem() {
        return getPossessor()
                .map(LivingEntity::getActiveItem)
                .orElseGet(super::getActiveItem);
    }

    @Override
    public void setEquippedStack(EquipmentSlot var1, ItemStack var2) {
        Optional<PlayerEntity> possessing = getPossessor();
        if (possessing.filter(p -> !p.world.isClient).isPresent()) {
            possessing.get().setEquippedStack(var1, var2);
        } else {
            super.setEquippedStack(var1, var2);
        }
    }

    @Override
    public boolean isEquippedStackValid(EquipmentSlot equipmentSlot_1) {
        return getPossessor()
                .map(playerEntity -> playerEntity.isEquippedStackValid(equipmentSlot_1))
                .orElseGet(() -> super.isEquippedStackValid(equipmentSlot_1));
    }

    /**
     * @return true if this entity's main hand is active
     */
    @Override
    public boolean isUsingItem() {
        return getPossessor()
                .map(LivingEntity::isUsingItem)
                .orElseGet(super::isUsingItem);
    }
}
