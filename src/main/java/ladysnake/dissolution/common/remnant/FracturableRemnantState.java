package ladysnake.dissolution.common.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.impl.remnant.MutableRemnantState;
import ladysnake.dissolution.common.network.DissolutionNetworking;
import ladysnake.dissolution.common.util.InventoryHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createEtherealAnimationMessage;

public class FracturableRemnantState extends MutableRemnantState {
    public FracturableRemnantState(RemnantType type, PlayerEntity owner) {
        super(type, owner);
    }

    @Override
    public void fracture() {
        if (!player.world.isClient) {
            PossessionComponent possessionComponent = ((DissolutionPlayer) this.player).getPossessionComponent();
            if (!this.isSoul()) {
                PlayerShellEntity shellEntity = new PlayerShellEntity(player);
                InventoryHelper.transferEquipment(player, shellEntity);
                shellEntity.transferInventory(player.inventory, shellEntity.getInventory(), shellEntity.getInventory().getInvSize());
                player.world.spawnEntity(shellEntity);
                this.setSoul(true);
            } else if (possessionComponent.isPossessing()) {
                possessionComponent.stopPossessing();
            } else {
                return;
            }
            DissolutionNetworking.sendTo((ServerPlayerEntity)this.player, createEtherealAnimationMessage());
        }
    }
}
