package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.impl.movement.SerializableMovementConfig;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

public abstract class MutableRemnantState implements RemnantState {
    public static final String INCORPOREAL_TAG = "incorporeal";

    private final RemnantType type;
    protected PlayerEntity player;
    protected boolean incorporeal;

    public MutableRemnantState(RemnantType type, PlayerEntity player) {
        this.type = type;
        this.player = player;
    }

    @Override
    public boolean isIncorporeal() {
        return this.isSoul() && !((DissolutionPlayer) player).getPossessionComponent().isPossessing();
    }

    @Override
    public boolean isSoul() {
        return this.incorporeal;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        if (this.incorporeal != incorporeal) {
            this.incorporeal = incorporeal;
            PlayerAbilities abilities = this.player.abilities;
            SerializableMovementConfig config;
            if (incorporeal) {
                config = SerializableMovementConfig.SOUL;
                abilities.invulnerable = true;
            } else {
                config = null;
                abilities.allowFlying = this.player.isCreative();
                abilities.flying &= abilities.allowFlying;
                abilities.invulnerable = this.player.isCreative();
                ((DissolutionPlayer)this.player).getPossessionComponent().stopPossessing();
            }
            ((DissolutionPlayer)this.player).getMovementAlterer().setConfig(config);
            if (!this.player.world.isClient) {
                // Synchronizes with all players tracking the owner
                sendTo((ServerPlayerEntity) this.player, createCorporealityMessage(this.player));
                sendToAllTracking(this.player, createCorporealityMessage(this.player));
            }
        }
    }

    @Override
    public void onPlayerClone(ServerPlayerEntity clone, boolean dead) {
        ((DissolutionPlayer)clone).setRemnant(true);
        if (dead) {
            clone.setPositionAndAngles(this.player);
            ((DissolutionPlayer) clone).getRemnantState().setSoul(true);
        }
    }

    @Override
    public RemnantType getType() {
        return this.type;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean(INCORPOREAL_TAG, this.isSoul());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.setSoul(tag.getBoolean(INCORPOREAL_TAG));
    }
}
