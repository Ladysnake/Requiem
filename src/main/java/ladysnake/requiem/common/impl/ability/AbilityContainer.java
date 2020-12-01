package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.MobAbility;

public abstract class AbilityContainer<A extends MobAbility<?>> {
    protected final A ability;
    private int cooldown;

    protected AbilityContainer(A ability) {
        this.ability = ability;
    }

    public A get() {
        return this.ability;
    }

    public void update() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
        this.ability.update(this.cooldown);
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
