package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.event.PlayerEvent;
import ladysnake.dissolution.api.remnant.RemnantHandler;
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

public class DefaultRemnantHandler implements RemnantHandler {
    public static final String INCORPOREAL_TAG = "incorporeal";

    @API(status = INTERNAL)
    public static void init() {
        // Prevent incorporeal players from picking up anything
        PlayerEvent.PICKUP_ITEM.register((player, pickedUp) -> {
            if (RemnantHandler.get(player).filter(RemnantHandler::isSoul).isPresent()) {
                Entity possessed = (Entity) ((DissolutionPlayer)player).getPossessionManager().getPossessedEntity();
                if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.FAILURE;
                }
            }
            return ActionResult.PASS;
        });
        // Prevent incorporeal players from breaking anything
        PlayerInteractionEvent.ATTACK_BLOCK.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantHandler.get(player).filter(RemnantHandler::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            } else {
                return ActionResult.PASS;
            }
        });
        // Prevent incorporeal players from hitting anything
        PlayerInteractionEvent.ATTACK_ENTITY.register((playerEntity, world, hand, entity) -> {
            if (RemnantHandler.get(playerEntity).filter(RemnantHandler::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
    }

    @API(status = INTERNAL)
    public static RemnantHandler getOrMakeRemnant(PlayerEntity player) {
        RemnantHandler handler = ((DissolutionPlayer)player).getRemnantHandler();
        if (handler == null) {
            handler = new DefaultRemnantHandler(player);
            ((DissolutionPlayer)player).setRemnantHandler(handler);
        }
        return handler;
    }

    protected PlayerEntity owner;
    protected boolean incorporeal;

    public DefaultRemnantHandler(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean isIncorporeal() {
        return this.isSoul() && !((DissolutionPlayer)owner).getPossessionManager().isPossessing();
    }

    @Override
    public boolean isSoul() {
        return this.incorporeal;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        this.incorporeal = incorporeal;
        PlayerAbilities abilities = this.owner.abilities;
        if (incorporeal) {
            abilities.allowFlying = true;
            abilities.invulnerable = true;
        } else {
            abilities.allowFlying = this.owner.isCreative();
            abilities.flying = abilities.flying && abilities.allowFlying;
            abilities.invulnerable = this.owner.isCreative();
            ((DissolutionPlayer)this.owner).getPossessionManager().stopPossessing();
        }
        if (!this.owner.world.isClient) {
            // Synchronizes with all players tracking the owner
            sendTo((ServerPlayerEntity) this.owner, createCorporealityPacket(this.owner));
            sendToAllTracking(this.owner, createCorporealityPacket(this.owner));
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
