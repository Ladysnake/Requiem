/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.common.network;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.remnant.RemnantStates;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.ExperienceBarUpdateS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.BiConsumer;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ServerMessageHandling {

    public static void init() {
        register(LEFT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = (Possessable) ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.ATTACK);
            }
        });
        register(RIGHT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = (Possessable) ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.INTERACT);
            }
        });
        ServerSidePacketRegistry.INSTANCE.register(POSSESSION_REQUEST, (context, buf) -> {
            int requestedId = buf.readInt();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                Entity entity = player.world.getEntityById(requestedId);
                if (entity instanceof MobEntity && entity.distanceTo(player) < 20) {
                    ((RequiemPlayer) player).getPossessionComponent().startPossessing((MobEntity) entity);
                }
                sendTo((ServerPlayerEntity) player, createEmptyMessage(POSSESSION_ACK));
            });
        });
        ServerSidePacketRegistry.INSTANCE.register(OPUS_UPDATE, (context, buf) -> {
            String content = buf.readString(32767);
            boolean sign = buf.readBoolean();
            RemnantType type = sign ? RemnantStates.get(new Identifier(buf.readString(32767))) : null;
            Hand hand = buf.readEnumConstant(Hand.class);
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                ItemStack book = player.getStackInHand(hand);
                if (book.getItem() != RequiemItems.OPUS_DEMONIUM) {
                    return;
                }
                int requiredXp = player.isCreative() ? 0 : OpusDemoniumItem.REQUIRED_CONVERSION_XP;
                if (sign && player.experience >= requiredXp) {
                    player.setStackInHand(hand, type.getConversionBook(player));
                    player.world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, player.world.random.nextFloat() * 0.1F + 0.9F);
                    player.experience -= requiredXp;
                    if (player.experience < 0) {
                        player.experience = 0;
                        player.experienceLevelProgress = 0.0F;
                        player.experienceLevel = 0;
                    }
                    ((ServerPlayerEntity)player).networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceLevelProgress, player.experienceLevel, player.experience));
                } else {
                    ListTag pages = new ListTag();
                    pages.add(new StringTag(content));
                    book.setChildTag("pages", pages);
                }
            });
        });
        ServerSidePacketRegistry.INSTANCE.register(DIALOGUE_ACTION, (context, buffer) -> {
            Identifier choice = buffer.readIdentifier();
            context.getTaskQueue().execute(() -> ((RequiemPlayer) context.getPlayer()).getDialogueTracker().handleAction(choice));
        });
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ServerSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
