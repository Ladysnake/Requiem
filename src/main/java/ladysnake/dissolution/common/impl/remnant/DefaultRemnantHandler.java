package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
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
        PlayerInteractionEvent.ATTACK_BLOCK.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantHandler.get(player).filter(RemnantHandler::isIncorporeal).isPresent()) {
                return ActionResult.FAILURE;
            } else {
                return ActionResult.PASS;
            }
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
    protected boolean wasAllowedFlight;

    public DefaultRemnantHandler(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean isIncorporeal() {
        return this.incorporeal;
    }

    @Override
    public void setIncorporeal(boolean incorporeal) {
        this.incorporeal = incorporeal;
        if (incorporeal) {
            this.wasAllowedFlight = this.owner.abilities.allowFlying;
            this.owner.abilities.allowFlying = true;
        } else {
            this.owner.abilities.allowFlying = this.wasAllowedFlight;
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
        serialized.putBoolean(INCORPOREAL_TAG, this.isIncorporeal());
        return serialized;
    }

    @Override
    public void readFromTag(CompoundTag tag) {
        this.setIncorporeal(tag.getBoolean(INCORPOREAL_TAG));
    }
}
