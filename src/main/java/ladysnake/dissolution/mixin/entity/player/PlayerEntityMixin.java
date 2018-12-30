package ladysnake.dissolution.mixin.entity.player;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.common.impl.DefaultRemnantHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DissolutionPlayer {
    private static final String TAG_REMNANT_DATA = "dissolution:remnant_data";
    private static final TrackedData<Boolean> DISSOLUTION_INCORPOREAL = DefaultRemnantHandler.PLAYER_INCORPOREAL;

    private @Nullable RemnantHandler remnantHandler;

    protected PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public @Nullable RemnantHandler getRemnantHandler() {
        return this.remnantHandler;
    }

    @Override
    public void setRemnantHandler(@Nullable RemnantHandler handler) {
        this.remnantHandler = handler;
    }

    @Inject(at = @At("TAIL"), method = "initDataTracker")
    protected void initDataTracker(CallbackInfo info) {
        ((PlayerEntity)(Object)this).getDataTracker().startTracking(DISSOLUTION_INCORPOREAL, false);
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        if (this.remnantHandler != null) {
            tag.put(TAG_REMNANT_DATA, this.remnantHandler.writeToTag());
        }
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.containsKey(TAG_REMNANT_DATA)) {
            if (this.remnantHandler == null) {
                this.setRemnantHandler(new DefaultRemnantHandler((PlayerEntity)(Object)this));
            }
            this.remnantHandler.readFromTag(tag.getCompound(TAG_REMNANT_DATA));
        }
    }
}
