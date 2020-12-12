package ladysnake.pandemonium.common;

import com.mojang.authlib.GameProfile;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.ladysnake.impersonate.Impersonate;
import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.pandemonium.common.impl.anchor.AnchorFactories;
import ladysnake.pandemonium.common.remnant.PlayerBodyTracker;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import nerdhub.cardinal.components.api.util.container.AbstractComponentContainer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class PlayerSplitter {
    public static void split(ServerPlayerEntity whole) {
        FractureAnchorManager anchorManager = FractureAnchorManager.get(whole.world);
        PlayerShellEntity shell = new PlayerShellEntity(PandemoniumEntities.PLAYER_SHELL, whole.world);
        shell.storePlayerData(whole, computeCopyNbt(whole));
        ServerPlayerEntity soul = performRespawn(whole);
        soul.world.spawnEntity(shell);
        FractureAnchor anchor = anchorManager.addAnchor(AnchorFactories.fromEntityUuid(shell.getUuid()));
        anchor.setPosition(shell.getX(), shell.getY(), shell.getZ());
        PlayerBodyTracker.get(soul).setAnchor(anchor);
    }

    public static void merge(PlayerShellEntity shell, ServerPlayerEntity soul) {
        soul.inventory.dropAll();
        shell.restorePlayerData(soul);
        shell.remove();
        RemnantComponent.get(soul).setSoul(false);

        if (!Objects.equals(shell.getPlayerUuid(), soul.getUuid())) {
            GameProfile gameProfile = shell.getGameProfile();
            Impersonate.IMPERSONATION.get(soul).impersonate(Pandemonium.BODY_IMPERSONATION, gameProfile == null ? new GameProfile(shell.getPlayerUuid(), null) : gameProfile);
        }
    }

    public static ServerPlayerEntity performRespawn(ServerPlayerEntity player) {
        //Setup location for dummy
        GameRules.BooleanRule keepInventory = player.world.getGameRules().get(GameRules.KEEP_INVENTORY);
        boolean keepInv = keepInventory.get();
        RegistryKey<World> dimension = player.getSpawnPointDimension();
        BlockPos blockPos = player.getSpawnPointPosition();
        boolean spawnPointSet = player.isSpawnPointSet();
        float angle = player.getSpawnAngle();
        player.setSpawnPoint(World.OVERWORLD, null, 0, false, false);

        try {
            keepInventory.set(false, player.getServer());

            //Setup location for dummy
            ServerPlayerEntity clone = player.getServerWorld().getServer().getPlayerManager().respawnPlayer(player, false);
            clone.setSpawnPoint(dimension, blockPos, angle, spawnPointSet, false);
            player.networkHandler.player = clone;
            return clone;
        } finally {
            player.setSpawnPoint(dimension, blockPos, angle, spawnPointSet, false);
            keepInventory.set(keepInv, player.getServer());
        }
    }

    @NotNull
    public static CompoundTag computeCopyNbt(Entity template) {
        //Player keeps everything that goes through death
        CompoundTag templateNbt = template.toTag(new CompoundTag());
        deduplicateVanillaData(templateNbt);
        deduplicateComponents(templateNbt, ComponentProvider.fromEntity(template).getComponentContainer().keys());
        return templateNbt;
    }

    private static void deduplicateComponents(CompoundTag leftoverData, Set<ComponentKey<?>> keys) {
        CompoundTag leftoverComponents = leftoverData.getCompound(AbstractComponentContainer.NBT_KEY);

        for (ComponentKey<?> key : keys) {
            String keyId = key.getId().toString();
            if (EntityComponents.getRespawnCopyStrategy(key) == RespawnCopyStrategy.ALWAYS_COPY) {
                // avoid duplicating soulbound data
                leftoverComponents.remove(keyId);
            }
        }
    }

    private static void deduplicateVanillaData(CompoundTag leftoverData) {
        leftoverData.remove("UUID");
        leftoverData.remove("SpawnX");
        leftoverData.remove("SpawnY");
        leftoverData.remove("SpawnZ");
        leftoverData.remove("SpawnForced");
        leftoverData.remove("SpawnAngle");
        leftoverData.remove("SpawnDimension");
        leftoverData.remove("EnderItems");
        leftoverData.remove("abilities");
        leftoverData.remove("playerGameType");
        leftoverData.remove("previousPlayerGameType");
        leftoverData.remove("seenCredits");
    }
}
