package ladysnake.requiem.common.impl.ability;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.internal.DummyMobAbilityController;
import ladysnake.requiem.common.entity.ability.DelegatingDirectAbility;
import ladysnake.requiem.common.entity.ability.SoulPossessAbility;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

public class PlayerAbilityController implements MobAbilityController, AutoSyncedComponent {
    private static final MobAbilityConfig<PlayerEntity> SOUL_CONFIG = MobAbilityConfig.<PlayerEntity>builder()
        .directAttack(player -> new DelegatingDirectAbility<>(player, MobEntity.class, AbilityType.INTERACT))
        .directInteract(SoulPossessAbility::new)
        .build();

    private final MobAbilityController soulAbilities;
    private final PlayerEntity player;
    private final EnumMap<AbilityType, WeakReference<Entity>> targets = new EnumMap<>(AbilityType.class);
    private AbilityType[] sortedAbilities = AbilityType.values();

    private MobAbilityController delegate = DummyMobAbilityController.INSTANCE;

    public PlayerAbilityController(PlayerEntity player) {
        soulAbilities = new ImmutableMobAbilityController<>(SOUL_CONFIG, player);
        this.player = player;
    }

    public static PlayerAbilityController get(PlayerEntity player) {
        return (PlayerAbilityController) MobAbilityController.get(player);
    }

    public void resetAbilities(boolean incorporeal) {
        this.setDelegate(incorporeal ? this.soulAbilities : DummyMobAbilityController.INSTANCE);
    }

    public void usePossessedAbilities(MobEntity possessed) {
        this.setDelegate(MobAbilityController.get(possessed));
    }

    @CheckEnv(Env.CLIENT)
    public AbilityType[] getSortedAbilities() {
        return sortedAbilities;
    }

    @CheckEnv(Env.CLIENT)
    public void tryTarget(AbilityType type, Entity target) {
        if (this.canTarget(type, target)) {
            targets.put(type, new WeakReference<>(target));
        }
    }

    @CheckEnv(Env.CLIENT)
    public void clearTargets() {
        this.targets.clear();
    }

    @CheckEnv(Env.CLIENT)
    public @Nullable Entity getTargetedEntity(AbilityType type) {
        WeakReference<Entity> ref = targets.get(type);
        return ref == null ? null : ref.get();
    }

    @CheckEnv(Env.CLIENT)
    public boolean useDirectAbility(AbilityType type) {
        Entity targetedEntity = this.getTargetedEntity(type);

        if (targetedEntity != null) {
            if (this.useDirect(type, targetedEntity)) {
                RequiemNetworking.sendAbilityUseMessage(type, targetedEntity);
                return true;
            }
        }

        return false;
    }

    private void setDelegate(MobAbilityController delegate) {
        Preconditions.checkNotNull(delegate);
        this.delegate = delegate;

        this.targets.clear();
        this.sortAbilities();
    }

    private void sortAbilities() {
        AbilityType[] a = this.sortedAbilities.clone();
        Arrays.sort(a, Comparator.comparingDouble(this::getRange));
        this.sortedAbilities = a;
    }

    @Override
    public Identifier getIconTexture(AbilityType type) {
        return this.delegate.getIconTexture(type);
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

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        this.delegate.writeSyncPacket(buf, recipient);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.delegate.applySyncPacket(buf);
    }
}
