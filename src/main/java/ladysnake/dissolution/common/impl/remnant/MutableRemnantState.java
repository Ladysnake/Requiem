package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.PlayerEvent;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.common.impl.SerializableMovementConfig;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.apiguardian.api.API;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;
import static org.apiguardian.api.API.Status.INTERNAL;

public class MutableRemnantState implements RemnantState {
    public static final String INCORPOREAL_TAG = "incorporeal";

    @API(status = INTERNAL)
    public static void init() {
        // Prevent incorporeal players from picking up anything
        PlayerEvent.PICKUP_ITEM.register((player, pickedUp) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isSoul).isPresent()) {
                Entity possessed = (Entity) ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
                if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.FAILURE;
                }
            }
            return ActionResult.PASS;
        });
        // Prevent incorporeal players from breaking anything
        PlayerInteractionEvent.ATTACK_BLOCK.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            } else {
                return ActionResult.PASS;
            }
        });
        // Prevent incorporeal players from hitting anything
        PlayerInteractionEvent.ATTACK_ENTITY.register((playerEntity, world, hand, entity) -> {
            if (RemnantState.getIfRemnant(playerEntity).filter(RemnantState::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
    }

    protected PlayerEntity owner;
    protected boolean incorporeal;

    public MutableRemnantState(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean isIncorporeal() {
        return this.isSoul() && !((DissolutionPlayer)owner).getPossessionComponent().isPossessing();
    }

    @Override
    public boolean isSoul() {
        return this.incorporeal;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        if (this.incorporeal != incorporeal) {
            this.incorporeal = incorporeal;
            PlayerAbilities abilities = this.owner.abilities;
            SerializableMovementConfig config;
            if (incorporeal) {
                config = SerializableMovementConfig.SOUL;
                abilities.invulnerable = true;
            } else {
                config = null;
                abilities.allowFlying = false;
                abilities.invulnerable = this.owner.isCreative();
                ((DissolutionPlayer)this.owner).getPossessionComponent().stopPossessing();
            }
            ((DissolutionPlayer)this.owner).getMovementAlterer().setConfig(config);
            if (!this.owner.world.isClient) {
                // Synchronizes with all players tracking the owner
                sendTo((ServerPlayerEntity) this.owner, createCorporealityPacket(this.owner));
                sendToAllTracking(this.owner, createCorporealityPacket(this.owner));
            }
        }
    }

    @Override
    public CompoundTag writeToTag() {
        CompoundTag serialized = new CompoundTag();
        serialized.putBoolean(INCORPOREAL_TAG, this.isSoul());
        return serialized;
    }

    @Override
    public void readFromTag(CompoundTag tag) {
        this.setSoul(tag.getBoolean(INCORPOREAL_TAG));
    }
}
