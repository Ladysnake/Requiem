/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.event.requiem.EntityRecordUpdateCallback;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.entity.ReleasedSoulEntity;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FilledSoulVesselItem extends Item {
    public static final String SOUL_FRAGMENT_NBT = "requiem:soul_fragment";

    public static ItemStack forEntityType(EntityType<?> type) {
        ItemStack result = new ItemStack(RequiemItems.FILLED_SOUL_VESSEL);
        result.getOrCreateSubNbt(SOUL_FRAGMENT_NBT).putString("type", EntityType.getId(type).toString());
        return result;
    }

    private final EmptySoulVesselItem emptySoulVessel;

    public FilledSoulVesselItem(Settings settings, EmptySoulVesselItem emptySoulVessel) {
        super(settings);
        this.emptySoulVessel = emptySoulVessel;
    }

    public void registerCallbacks() {
        EntityRecordUpdateCallback.EVENT.register((entity, linkedRecord) ->
            linkedRecord.get(RequiemRecordTypes.RELEASED_SOUL).flatMap(u -> SoulHolderComponent.KEY.maybeGet(entity)).ifPresent(soulHolder -> {
                soulHolder.giveSoulBack();
                linkedRecord.invalidate();
            }));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Optional.ofNullable(stack.getSubNbt(SOUL_FRAGMENT_NBT))
            .flatMap(fragmentData -> EntityType.get(fragmentData.getString("type")))
            .ifPresent(contained -> tooltip.add(new TranslatableText("requiem:tooltip.filled_vessel", contained.getName()).formatted(Formatting.GRAY)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            releaseSoul(user, Optional.ofNullable(stack.getSubNbt(FilledSoulVesselItem.SOUL_FRAGMENT_NBT))
                .filter(data -> data.containsUuid("uuid"))
                .map(data -> data.getUuid("uuid"))
                .orElse(null));
            ItemStack result = new ItemStack(this.emptySoulVessel);
            return TypedActionResult.success(ItemUsage.exchangeStack(stack, user, result));
        }
        user.playSound(RequiemSoundEvents.ITEM_FILLED_VESSEL_USE, 3f, 0.6F + user.getRandom().nextFloat() * 0.4F);
        return TypedActionResult.success(stack);
    }

    public static void releaseSoul(LivingEntity user, @Nullable UUID ownerRecord) {
        ReleasedSoulEntity releasedSoul = new ReleasedSoulEntity(RequiemEntities.RELEASED_SOUL, user.world, ownerRecord);
        releasedSoul.setPosition(user.getX(), user.getBodyY(0.8D), user.getZ());
        releasedSoul.setVelocity(user.getRotationVector().normalize().multiply(0.15));
        releasedSoul.setYaw(user.getYaw());
        releasedSoul.setPitch(user.getPitch());
        user.world.spawnEntity(releasedSoul);
    }
}
