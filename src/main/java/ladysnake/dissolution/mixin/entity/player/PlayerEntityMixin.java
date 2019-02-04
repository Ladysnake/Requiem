package ladysnake.dissolution.mixin.entity.player;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.common.impl.movement.PlayerMovementAlterer;
import ladysnake.dissolution.common.impl.possession.PossessionComponentImpl;
import ladysnake.dissolution.common.impl.remnant.MutableRemnantState;
import ladysnake.dissolution.common.impl.remnant.NullRemnantState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

@Mixin(value = PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DissolutionPlayer {
    private static final RemnantState NULL_STATE = new NullRemnantState();
    private static final String TAG_REMNANT_DATA = "dissolution:remnant_data";

    private RemnantState remnantState = NULL_STATE;
    private PossessionComponent possessionComponent = new PossessionComponentImpl((PlayerEntity) (Object) this);
    private MovementAlterer movementAlterer = new PlayerMovementAlterer((PlayerEntity)(Object)this);

    protected PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public RemnantState getRemnantState() {
        return this.remnantState;
    }

    @Override
    public PossessionComponent getPossessionComponent() {
        return possessionComponent;
    }

    @Override
    public MovementAlterer getMovementAlterer() {
        return this.movementAlterer;
    }

    @Override
    public void setRemnant(boolean remnant) {
        if (remnant != this.isRemnant()) {
            RemnantState state = remnant ? new MutableRemnantState((PlayerEntity) (Object) this) : NULL_STATE;
            this.setRemnantState(state);
        }
    }

    @Override
    public boolean isRemnant() {
        return !(this.remnantState instanceof NullRemnantState);
    }

    private void setRemnantState(RemnantState handler) {
        this.remnantState.setSoul(false);
        this.remnantState = handler;
        if (!this.world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            sendTo(player, createCorporealityPacket(player));
            sendToAllTracking(player, createCorporealityPacket(player));
        }
    }

    @Inject(method = "updateMovement", at = @At("HEAD"))
    private void updateMovementAlterer(CallbackInfo info) {
        this.movementAlterer.update();
    }

    /**
     * Players' base movement speed is reset each tick to their walking speed.
     * We don't want that when a possession is occurring.
     *
     * @param attr the {@code this} attribute reference
     * @param value the value that is supposed to be assigned
     */
    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;setBaseValue(D)V"))
    private void ignoreSpeedResetDuringPossession(EntityAttributeInstance attr, double value) {
        if (!this.getPossessionComponent().isPossessing()) {
            attr.setBaseValue(value);
        }
    }

    @Inject(method = "getEyeHeight", at = @At("RETURN"), cancellable = true)
    private void adjustEyeHeight(CallbackInfoReturnable<Float> info) {
        if (this.getPossessionComponent().isPossessing()) {
            Entity possessedEntity = (Entity) this.getPossessionComponent().getPossessedEntity();
            if (possessedEntity != null) {
                info.setReturnValue(possessedEntity.getEyeHeight());
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        if (this.isRemnant()) {
            tag.put(TAG_REMNANT_DATA, this.remnantState.writeToTag());
        }
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.containsKey(TAG_REMNANT_DATA)) {
            if (!this.isRemnant()) {
                this.setRemnantState(new MutableRemnantState((PlayerEntity) (Object) this));
            }
            this.remnantState.readFromTag(tag.getCompound(TAG_REMNANT_DATA));
        }
    }
}
