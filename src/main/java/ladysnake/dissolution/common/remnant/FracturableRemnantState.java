package ladysnake.dissolution.common.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionWorld;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.impl.anchor.AnchorFactories;
import ladysnake.dissolution.common.impl.remnant.MutableRemnantState;
import ladysnake.dissolution.common.network.DissolutionNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createEtherealAnimationMessage;

public class FracturableRemnantState extends MutableRemnantState {
    @Nullable
    protected UUID fractureUuid;

    public FracturableRemnantState(RemnantType type, PlayerEntity owner) {
        super(type, owner);
    }

    @Override
    public void fracture() {
        if (!player.world.isClient) {
            PossessionComponent possessionComponent = ((DissolutionPlayer) this.player).getPossessionComponent();
            FractureAnchorManager anchorManager = ((DissolutionWorld) player.world).getAnchorManager();
            if (!this.isSoul()) {
                PlayerShellEntity shellEntity = PlayerShellEntity.fromPlayer(player);
                player.world.spawnEntity(shellEntity);
                FractureAnchor anchor = anchorManager.addAnchor(AnchorFactories.fromEntityUuid(shellEntity.getUuid()));
                anchor.setPosition(shellEntity.x, shellEntity.y, shellEntity.z);
                this.fractureUuid = anchor.getUuid();
                this.setSoul(true);
            } else if (possessionComponent.isPossessing() && isAnchorValid()) {
                possessionComponent.stopPossessing();
            } else {
                return;
            }
            DissolutionNetworking.sendTo((ServerPlayerEntity)this.player, createEtherealAnimationMessage());
        }
    }

    private boolean isAnchorValid() {
        return this.fractureUuid != null && ((DissolutionWorld) player.world).getAnchorManager().getAnchor(this.fractureUuid) != null;
    }
}
