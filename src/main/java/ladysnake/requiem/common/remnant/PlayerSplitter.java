/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.remnant;

import com.mojang.authlib.GameProfile;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.internal.base.AbstractComponentContainer;
import io.github.ladysnake.impersonate.Impersonator;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.event.requiem.PlayerShellEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.event.requiem.SoulboundStackCheckCallback;
import ladysnake.requiem.api.v1.remnant.PlayerSplitResult;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.record.EntityPositionClerk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PlayerSplitter {
    public static final Identifier BODY_IMPERSONATION = RequiemCore.id("body_impersonation");

    /**
     * @see RemnantComponent#splitPlayer(boolean)
     */
    public static PlayerSplitResult doSplit(ServerPlayerEntity whole) {
        PlayerShellEntity shell = createShell(whole);
        Entity mount = whole.getVehicle();
        int experience = whole.totalExperience;
        EntityPositionClerk.transferRecord(whole, shell);
        ServerPlayerEntity soul = performRespawn(whole);
        // Respawning in some conditions can place us away from the shell
        soul.setSneaking(whole.isSneaking());
        soul.networkHandler.requestTeleport(shell.getX(), shell.getY(), shell.getZ(), shell.getYaw(), shell.getPitch());
        soul.addExperience(experience);
        transferSoulboundItems(shell, soul);
        soul.world.spawnEntity(shell);
        if (mount != null) shell.startRiding(mount);
        PlayerBodyTracker.get(soul).setAnchor(EntityPositionClerk.get(shell).getOrCreateRecord());
        PlayerShellEvents.PLAYER_SPLIT.invoker().onPlayerSplit(whole, soul, shell);
        return new PlayerSplitResult(soul, shell);
    }

    private static void transferSoulboundItems(PlayerShellEntity shell, ServerPlayerEntity soul) {
        // Inverted because we are doing things backwards (moving items back to player)
        boolean transferAll = !PossessionEvents.INVENTORY_TRANSFER_CHECK.invoker().shouldTransfer(soul, shell).get();

        for (int i = 0; i < soul.getInventory().size(); i++) {
            if (transferAll || SoulboundStackCheckCallback.EVENT.invoker().isSoulbound(shell.getInventory(), shell.getInventory().getStack(i), i)) {
                soul.getInventory().setStack(i, shell.getInventory().removeStack(i));
            }
        }
    }

    public static PlayerShellEntity createShell(ServerPlayerEntity whole) {
        PlayerShellEntity shell = new PlayerShellEntity(RequiemEntities.PLAYER_SHELL, whole.getWorld());
        shell.changeGameMode(whole.interactionManager.getGameMode());  // use same gamemode for deserialization
        shell.storePlayerData(whole, computeCopyNbt(whole));
        shell.headYaw = whole.headYaw;
        shell.bodyYaw = whole.bodyYaw;
        GameMode shellGm = whole.interactionManager.isSurvivalLike() ? whole.interactionManager.getGameMode() : GameMode.SURVIVAL;
        shell.changeGameMode(shellGm);
        // changeGameMode has no effect when the gamemode stays the same, so we make sure abilities work as intended
        shellGm.setAbilities(shell.getAbilities());
        RemnantComponent.get(shell).become(RemnantTypes.MORTAL, true);
        InventoryLimiter.instance().disable(shell);
        for (StatusEffectInstance effect : List.copyOf(shell.getStatusEffects())) {
            if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
                shell.removeStatusEffect(effect.getEffectType());
            }
        }
        PlayerShellEvents.DATA_TRANSFER.invoker().transferData(whole, shell, false);
        return shell;
    }

    public static void doMerge(PlayerShellEntity shell, ServerPlayerEntity soul) {
        Entity mount = shell.getVehicle();
        shell.stopRiding();
        mergeInventories(shell, soul);
        soul.getInventory().dropAll();  // ensure modded inventories do not get voided, hopefully
        // Note: the teleport request must be before deserialization, as it only encodes the required relative movement
        soul.networkHandler.requestTeleport(shell.getX(), shell.getY(), shell.getZ(), shell.getYaw(), shell.getPitch(), EnumSet.allOf(PlayerPositionLookS2CPacket.Flag.class));
        // override common data that may have been altered during this shell's existence
        performNbtCopy(computeCopyNbt(shell), soul);
        soul.addExperience(shell.totalExperience);
        EntityPositionClerk.transferRecord(shell, soul);
        PlayerShellEvents.DATA_TRANSFER.invoker().transferData(shell, soul, true);

        soul.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(soul.getId(), soul.getDataTracker().m_inhpjhpj()));

        shell.remove(Entity.RemovalReason.DISCARDED);
        if (mount != null) soul.startRiding(mount);

        GameProfile shellProfile = shell.getDisplayProfile();
        if (shellProfile != null && !Objects.equals(shellProfile.getId(), soul.getUuid())) {
            Impersonator.get(soul).impersonate(BODY_IMPERSONATION, shellProfile);
        }

        Objects.requireNonNull(soul.getServer()).getPlayerManager().sendPlayerStatus(soul);
        PlayerShellEvents.PLAYER_MERGED.invoker().onPlayerMerge(soul, shell, shell.getGameProfile());
    }

    private static void mergeInventories(PlayerShellEntity shell, ServerPlayerEntity soul) {
        PlayerInventory shellInventory = shell.getInventory();
        PlayerInventory soulInventory = soul.getInventory();

        for (int i = 0; i < soulInventory.size(); i++) {
            ItemStack stack = soulInventory.removeStack(i);

            if (!stack.isEmpty()) {
                if (shellInventory.getStack(i).isEmpty()) {
                    shellInventory.setStack(i, stack);
                } else {
                    soul.dropStack(stack);
                }
            }
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
//            ((SwitchablePlayerEntity) player).cca$markAsSwitchingCharacter();

            ServerPlayerEntity clone = player.getWorld().getServer().getPlayerManager().respawnPlayer(player, false);
            clone.setSpawnPoint(dimension, blockPos, angle, spawnPointSet, false);
            player.networkHandler.player = clone;
            return clone;
        } finally {
            player.setSpawnPoint(dimension, blockPos, angle, spawnPointSet, false);
            keepInventory.set(keepInv, player.getServer());
        }
    }

    private static NbtCompound computeCopyNbt(Entity template) {
        //Player keeps everything that goes through death
        NbtCompound templateNbt = template.writeNbt(new NbtCompound());
        deduplicateVanillaData(templateNbt);
        deduplicateComponents(templateNbt, template.asComponentProvider().getComponentContainer().keys());
        return templateNbt;
    }

    private static void deduplicateComponents(NbtCompound leftoverData, Set<ComponentKey<?>> keys) {
        NbtCompound leftoverComponents = leftoverData.getCompound(AbstractComponentContainer.NBT_KEY);

        for (ComponentKey<?> key : keys) {
            String keyId = key.getId().toString();
            if (RespawnCopyStrategy.get(key) == RespawnCopyStrategy.ALWAYS_COPY) {
                // avoid duplicating soulbound data
                leftoverComponents.remove(keyId);
            }
        }
    }

    private static void deduplicateVanillaData(NbtCompound leftoverData) {
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
        leftoverData.remove("XpP");
        leftoverData.remove("XpLevel");
        leftoverData.remove("XpTotal");
    }

    public static void performNbtCopy(NbtCompound from, Entity to) {
        // Save the complete representation of the player
        NbtCompound serialized = new NbtCompound();
        // We write every attribute of the destination entity to the tag, then we override.
        // That way, attributes that do not exist in the base entity are kept intact during the copy.
        to.writeNbt(serialized);
        serialized.copyFrom(from);
        to.readNbt(serialized);
    }
}
