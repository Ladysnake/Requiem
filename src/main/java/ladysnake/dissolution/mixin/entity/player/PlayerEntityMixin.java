package ladysnake.dissolution.mixin.entity.player;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.DissolutionRegistries;
import ladysnake.dissolution.common.impl.movement.PlayerMovementAlterer;
import ladysnake.dissolution.common.impl.possession.PossessionComponentImpl;
import ladysnake.dissolution.common.impl.remnant.NullRemnantState;
import ladysnake.dissolution.common.remnant.RemnantStates;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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
    private static final String TAG_REMNANT_DATA = "dissolution:remnant_data";

    private RemnantState remnantState = NullRemnantState.NULL_STATE;
    private PossessionComponent possessionComponent = new PossessionComponentImpl((PlayerEntity) (Object) this);
    private MovementAlterer movementAlterer = new PlayerMovementAlterer((PlayerEntity)(Object)this);

    protected PlayerEntityMixin(EntityType<? extends PlayerEntity> type, World world) {
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
            RemnantState state = remnant ? RemnantStates.LARVA.create((PlayerEntity) (Object) this) : NullRemnantState.NULL_STATE;
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
            sendTo(player, createCorporealityMessage(player));
            sendToAllTracking(player, createCorporealityMessage(player));
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

    @Inject(method = "getSizeForStatus", at = @At("HEAD"), cancellable = true)
    private void adjustSize(EntityPose pose, CallbackInfoReturnable<EntitySize> cir) {
        Entity possessedEntity = (Entity) this.getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            cir.setReturnValue(possessedEntity.getSizeForStatus(pose));
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag remnantData = new CompoundTag();
        remnantData.putString("id", DissolutionRegistries.REMNANT_STATES.getId(this.getRemnantState().getType()).toString());
        tag.put(TAG_REMNANT_DATA, this.remnantState.toTag(remnantData));
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag remnantTag = tag.getCompound(TAG_REMNANT_DATA);
        RemnantType remnantType = DissolutionRegistries.REMNANT_STATES.get(new Identifier(remnantTag.getString("id")));
        RemnantState handler = remnantType.create((PlayerEntity) (Object) this);
        handler.fromTag(remnantTag);
        this.setRemnantState(handler);
    }
}
