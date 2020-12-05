package ladysnake.requiem.compat;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.hyper_pigeon.eldritch_mobs.EldritchMobsMod;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class EldritchMobsCompat implements PossessionStartCallback {
    public static void init() {
        PossessionStartCallback.EVENT.register(Requiem.id("eldritch_mobs"), new EldritchMobsCompat());
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    @Override
    public Result onPossessionAttempted(MobEntity target, PlayerEntity possessor, boolean simulate) {
        ComponentProvider t = ComponentProvider.fromEntity(target);
        if (EldritchMobsMod.isEldritch(t) || EldritchMobsMod.isElite(t) || EldritchMobsMod.isUltra(t)) {
            return Result.DENY;
        }
        return Result.PASS;
    }
}
