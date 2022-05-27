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
package ladysnake.requiem.mixin.common.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import ladysnake.requiem.api.v1.event.minecraft.DynamicRegistryRegistrationCallback;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.util.data.DynamicRegistryRegistrationHelperImpl;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DynamicRegistryManager.class)
public interface DynamicRegistryManagerMixin {
    @Shadow
    private static void register(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> infosBuilder, RegistryKey<? extends Registry<?>> registryRef, Codec<?> entryCodec) {
        throw new IllegalStateException("Mixin not transformed");
    }

    @Shadow
    private static void register(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> infosBuilder, RegistryKey<? extends Registry<?>> registryRef, Codec<?> entryCodec, Codec<?> networkEntryCodec) {}

    @Dynamic("Lambda for INFOS initialization through Util#make")
    @Inject(method = "method_30531", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void buildDynamicRegistries(CallbackInfoReturnable<ImmutableMap<RegistryKey<? extends Registry<?>>, ?>> cir, ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> builder) {
        RequiemRegistries.init();
        DynamicRegistryRegistrationCallback.EVENT.invoker().registerDynamicRegistries(new DynamicRegistryRegistrationHelperImpl(
            (registryKey, codec) -> register(builder, registryKey, codec),
            (registryKey, codec, codec2) -> register(builder, registryKey, codec, codec2))
        );
    }
}
