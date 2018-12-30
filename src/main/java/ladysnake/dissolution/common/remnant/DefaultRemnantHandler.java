package ladysnake.dissolution.common.remnant;

import ladysnake.dissolution.api.remnant.RemnantCapability;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;

public class DefaultRemnantHandler implements RemnantCapability {
    public static final String INCORPOREAL_TAG = "incorporeal";
    public static final TrackedData<Boolean> PLAYER_INCORPOREAL = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static void init() {
        PlayerInteractionEvent.ATTACK_BLOCK.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantCapability.get(player).filter(RemnantCapability::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            } else {
                return ActionResult.PASS;
            }
        });
    }

    protected PlayerEntity owner;
    protected boolean wasAllowedFlight;

    public DefaultRemnantHandler(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean isIncorporeal() {
        return this.owner.getDataTracker().get(PLAYER_INCORPOREAL);
    }

    @Override
    public void setIncorporeal(boolean incorporeal) {
        this.owner.getDataTracker().set(PLAYER_INCORPOREAL, incorporeal);
        if (incorporeal) {
            this.wasAllowedFlight = this.owner.abilities.allowFlying;
            this.owner.abilities.allowFlying = true;
        } else {
            this.owner.abilities.allowFlying = this.wasAllowedFlight;
        }
    }

    @Override
    public CompoundTag writeToTag() {
        CompoundTag serialized = new CompoundTag();
        serialized.putBoolean(INCORPOREAL_TAG, this.isIncorporeal());
        return serialized;
    }

    @Override
    public void readFromTag(CompoundTag tag) {
        this.setIncorporeal(tag.getBoolean(INCORPOREAL_TAG));
    }
}
