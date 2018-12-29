package ladysnake.dissolution.mixin;

import ladysnake.dissolution.api.Remnant;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Remnant {
    private static final String TAG_INCORPOREAL = "dissolution:incorporeal";

    private boolean incorporeal;

    protected PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean isIncorporeal() {
        return incorporeal;
    }

    @Override
    public void setIncorporeal(boolean incorporeal) {
        this.incorporeal = incorporeal;
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToTag")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        tag.putBoolean(TAG_INCORPOREAL, this.incorporeal);
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromTag")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        this.incorporeal = tag.getBoolean(TAG_INCORPOREAL);
    }
}
