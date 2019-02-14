package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.ItemPickupCallback;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.common.impl.movement.SerializableMovementConfig;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
        ItemPickupCallback.EVENT.register((player, pickedUp) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isSoul).isPresent()) {
                Entity possessed = (Entity) ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
                if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.FAILURE;
                }
            }
            return ActionResult.PASS;
        });
        // Prevent incorporeal players from breaking anything
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            } else {
                return ActionResult.PASS;
            }
        });
        // Prevent incorporeal players from hitting anything
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
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
                abilities.allowFlying = this.owner.isCreative();
                abilities.invulnerable = this.owner.isCreative();
                ((DissolutionPlayer)this.owner).getPossessionComponent().stopPossessing();
            }
            ((DissolutionPlayer)this.owner).getMovementAlterer().setConfig(config);
            if (!this.owner.world.isClient) {
                // Synchronizes with all players tracking the owner
                sendTo((ServerPlayerEntity) this.owner, createCorporealityMessage(this.owner));
                sendToAllTracking(this.owner, createCorporealityMessage(this.owner));
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
