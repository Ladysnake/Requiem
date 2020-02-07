package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Npc;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class HorologistEntity extends PassiveEntity implements Npc {
    public HorologistEntity(EntityType<? extends PassiveEntity> type, World world) {
        super(type, world);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 32.0F));
    }

    @Override
    public boolean interactMob(PlayerEntity player, Hand hand) {
        if (world.isClient) {
            RequiemPlayer.from(player).getDialogueTracker().startDialogue(Requiem.id("remnant_choice"));
            return true;
        }
        return super.interactMob(player, hand);
    }
}
