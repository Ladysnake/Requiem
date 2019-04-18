package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.*;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ImmutableMobAbilityController<T extends MobEntity & Possessable> implements MobAbilityController {
    private final IndirectAbility<? super T> indirectAttack;
    private final IndirectAbility<? super T> indirectInteraction;
    private final DirectAbility<? super T> directAttack;
    private final DirectAbility<? super T> directInteraction;
    private final T owner;

    public ImmutableMobAbilityController(MobAbilityConfig<? super T> config, T owner) {
        this.owner = owner;
        this.directAttack = config.getDirectAbility(owner, AbilityType.ATTACK);
        this.directInteraction = config.getDirectAbility(owner, AbilityType.INTERACT);
        this.indirectAttack = config.getIndirectAbility(owner, AbilityType.ATTACK);
        this.indirectInteraction = config.getIndirectAbility(owner, AbilityType.INTERACT);
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        PlayerEntity p = this.owner.getPossessor();
        if (type == AbilityType.ATTACK) {
            return p != null && directAttack.trigger(p, target);
        } else if (type == AbilityType.INTERACT) {
            return p != null && directInteraction.trigger(p, target);
        }
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        PlayerEntity p = this.owner.getPossessor();
        if (type == AbilityType.ATTACK) {
            return p != null && indirectAttack.trigger(p);
        } else if (type == AbilityType.INTERACT) {
            return p != null && indirectInteraction.trigger(p);
        }
        return false;
    }

    @Override
    public void updateAbilities() {
        if (!this.owner.world.isClient) {
            this.directAttack.update();
            this.indirectAttack.update();
            this.directInteraction.update();
            this.indirectInteraction.update();
        }
    }
}
