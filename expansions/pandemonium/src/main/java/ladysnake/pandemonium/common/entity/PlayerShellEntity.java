/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.common.entity;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import ladysnake.pandemonium.common.PlayerSplitter;
import ladysnake.pandemonium.common.entity.fakeplayer.FakePlayerEntity;
import ladysnake.pandemonium.common.network.PandemoniumNetworking;
import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckForNull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.apiguardian.api.API.Status.MAINTAINED;

// TODO add inventory access
public class PlayerShellEntity extends FakePlayerEntity {

    @CheckEnv(Env.SERVER)
    @API(status = MAINTAINED)
    public PlayerShellEntity(EntityType<? extends PlayerShellEntity> type, ServerWorld world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createPlayerShellAttributes() {
        return createPlayerAttributes();
    }

    public void storePlayerData(ServerPlayerEntity player, CompoundTag respawnNbt) {
        // Save the complete representation of the player
        PlayerSplitter.performNbtCopy(respawnNbt, this);

        this.getDataTracker().set(PLAYER_MODEL_PARTS, player.getDataTracker().get(PLAYER_MODEL_PARTS));

        this.setOwnerProfile(Optional.ofNullable(Impersonator.get(player).getImpersonatedProfile()).orElse(player.getGameProfile()));
        this.setCustomName(new LiteralText(player.getEntityName()));
    }

    @Override
    public Text getName() {
        GameProfile impersonatedProfile = this.getOwnerProfile();
        if (impersonatedProfile != null) {
            return new LiteralText(impersonatedProfile.getName());
        }
        return super.getName();
    }

    /**
     * Applies the given player interaction to this Entity.
     */
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() != Items.NAME_TAG) {
            if (!this.world.isClient && !player.isSpectator()) {
                EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
                if (stack.isEmpty()) {
                    EquipmentSlot clickedSlot = this.getClickedSlot(vec);
                    if (this.hasStackEquipped(clickedSlot)) {
                        this.swapItem(player, clickedSlot, stack, hand);
                    } else {
                        return ActionResult.PASS;
                    }
                } else {
                    this.swapItem(player, slot, stack, hand);
                }
                return ActionResult.SUCCESS;
            } else {
                return stack.isEmpty() && !this.hasStackEquipped(this.getClickedSlot(vec))
                        ? ActionResult.PASS
                        : ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    /**
     * Vanilla code from the armor stand
     *
     * @param rayTrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EquipmentSlot getClickedSlot(Vec3d rayTrace) {
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        boolean flag = this.isBaby();
        double d0 = (rayTrace.y) * (flag ? 2.0D : 1.0D);

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasStackEquipped(EquipmentSlot.FEET)) {
            slot = EquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.hasStackEquipped(EquipmentSlot.CHEST)) {
            slot = EquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasStackEquipped(EquipmentSlot.LEGS)) {
            slot = EquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            slot = EquipmentSlot.HEAD;
        }

        return slot;
    }

    protected void swapItem(PlayerEntity player, EquipmentSlot targetedSlot, ItemStack playerItemStack,
                            Hand hand) {
        ItemStack equippedStack = this.getEquippedStack(targetedSlot);
        if (player.abilities.creativeMode && equippedStack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack copy = playerItemStack.copy();
            copy.setCount(1);
            this.equipStack(targetedSlot, copy);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1) {
            if (equippedStack.isEmpty()) {
                ItemStack copy = playerItemStack.copy();
                copy.setCount(1);
                this.equipStack(targetedSlot, copy);
                playerItemStack.decrement(1);
            }
        } else {
            this.equipStack(targetedSlot, playerItemStack);
            player.setStackInHand(hand, equippedStack);
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        UUID playerUuid = this.getOwnerUuid();

        if (playerUuid != null) {
            AttritionFocus.KEY.get(this.world.getScoreboard()).addAttrition(playerUuid, 1);
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return PandemoniumNetworking.createPlayerShellSpawnPacket(this);
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);

        if (tag.contains("PlayerProfile")) {
            this.setOwnerProfile(NbtHelper.toGameProfile(tag.getCompound("PlayerProfile")));
        }

        this.getDataTracker().set(PLAYER_MODEL_PARTS, tag.getByte("PlayerModelParts"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        GameProfile gameProfile = this.getOwnerProfile();
        if (gameProfile != null) {
            compound.put("PlayerProfile", NbtHelper.fromGameProfile(new CompoundTag(), gameProfile));
        }
        compound.putByte("PlayerModelParts", this.getDataTracker().get(PLAYER_MODEL_PARTS));
    }

    /* Static Methods */

}
