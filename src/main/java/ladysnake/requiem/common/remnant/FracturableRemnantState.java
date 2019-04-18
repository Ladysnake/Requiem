package ladysnake.requiem.common.remnant;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.RequiemWorld;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.FractureAnchor;
import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.impl.anchor.AnchorFactories;
import ladysnake.requiem.common.impl.anchor.EntityFractureAnchor;
import ladysnake.requiem.common.impl.remnant.MutableRemnantState;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class FracturableRemnantState extends MutableRemnantState {
    @Nullable
    protected UUID anchorUuid;
    private float previousAnchorHealth = -1;

    public FracturableRemnantState(RemnantType type, PlayerEntity owner) {
        super(type, owner);
    }

    @Override
    public void fracture() {
        if (!player.world.isClient) {
            PossessionComponent possessionComponent = ((RequiemPlayer) this.player).getPossessionComponent();
            FractureAnchorManager anchorManager = ((RequiemWorld) player.world).getAnchorManager();
            if (!this.isSoul()) {
                PlayerShellEntity shellEntity = PlayerShellEntity.fromPlayer(player);
                player.world.spawnEntity(shellEntity);
                FractureAnchor anchor = anchorManager.addAnchor(AnchorFactories.fromEntityUuid(shellEntity.getUuid()));
                anchor.setPosition(shellEntity.x, shellEntity.y, shellEntity.z);
                this.anchorUuid = anchor.getUuid();
                this.setSoul(true);
            } else if (possessionComponent.isPossessing() && this.getAnchor() != null) {
                possessionComponent.stopPossessing();
            } else {
                return;
            }
            RequiemNetworking.sendTo((ServerPlayerEntity)this.player, createEtherealAnimationMessage());
        }
    }

    @Override
    public void update() {
        FractureAnchor anchor = this.getAnchor();
        if (this.player instanceof ServerPlayerEntity) {
            if (anchor instanceof EntityFractureAnchor) {
                Entity anchorEntity = ((EntityFractureAnchor) anchor).getEntity();
                if (anchorEntity instanceof LivingEntity) {
                    float health = ((LivingEntity) anchorEntity).getHealth();
                    if (health < this.previousAnchorHealth) {
                        sendTo((ServerPlayerEntity) this.player, createAnchorDamageMessage(false));
                    }
                    this.previousAnchorHealth = health;
                }
            } else if (this.previousAnchorHealth > 0) {
                sendTo((ServerPlayerEntity) this.player, createAnchorDamageMessage(true));
                this.previousAnchorHealth = -1;
            }
        }
    }

    @Nullable
    private FractureAnchor getAnchor() {
        return this.anchorUuid != null
                ? ((RequiemWorld) player.world).getAnchorManager().getAnchor(this.anchorUuid)
                : null;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (this.anchorUuid != null) {
            tag.putUuid("AnchorUuid", this.anchorUuid);
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.hasUuid("AnchorUuid")) {
            this.anchorUuid = tag.getUuid("AnchorUuid");
        }
    }
}
