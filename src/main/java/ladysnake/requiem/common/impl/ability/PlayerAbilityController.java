package ladysnake.requiem.common.impl.ability;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.internal.DummyMobAbilityController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerAbilityController implements MobAbilityController {
    private static final MobAbilityConfig<PlayerEntity> SOUL_CONFIG = MobAbilityConfig.<PlayerEntity>builder()
        // TODO soul abilities
        .build();

    private final MobAbilityController soulAbilities;
    private MobAbilityController delegate = DummyMobAbilityController.INSTANCE;

    public PlayerAbilityController(PlayerEntity player) {
        soulAbilities = new ImmutableMobAbilityController<>(SOUL_CONFIG, player);
    }

    public static PlayerAbilityController get(PlayerEntity player) {
        return (PlayerAbilityController) MobAbilityController.get(player);
    }

    public void useSoulAbilities() {
        this.delegate = this.soulAbilities;
    }

    public void useDefaultAbilities() {
        this.delegate = DummyMobAbilityController.INSTANCE;
    }

    public void usePossessedAbilities(MobEntity possessed) {
        this.delegate = MobAbilityController.get(possessed);
    }

    public void setDelegate(MobAbilityController delegate) {
        Preconditions.checkNotNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public double getRange(AbilityType type) {
        return delegate.getRange(type);
    }

    @Override
    public boolean canTarget(AbilityType type, Entity target) {
        return delegate.canTarget(type, target);
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        return delegate.useDirect(type, target);
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        return delegate.useIndirect(type);
    }

    @Override
    public float getCooldownProgress(AbilityType type) {
        return delegate.getCooldownProgress(type);
    }

    @Override
    public void tick() {
        delegate.tick();
    }
}
