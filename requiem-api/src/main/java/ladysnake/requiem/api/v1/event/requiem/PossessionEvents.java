/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PossessionEvents {
    public static final Event<InventoryTransferCheck> INVENTORY_TRANSFER_CHECK = EventFactory.createArrayBacked(InventoryTransferCheck.class,
        callbacks -> (possessor, host) -> {
            TriState ret = TriState.DEFAULT;
            for (InventoryTransferCheck callback : callbacks) {
                switch (callback.shouldTransfer(possessor, host)) {
                    case TRUE -> ret = TriState.TRUE;
                    case FALSE -> {
                        return TriState.FALSE;
                    }
                }
            }
            return ret;
        });
    public static final Event<DetectionAttempt> DETECTION_ATTEMPT = EventFactory.createArrayBacked(DetectionAttempt.class,
        callbacks -> (sensed, sensor, reason) -> {
            DetectionAttempt.DetectionResult ret = DetectionAttempt.DetectionResult.DEFAULT;
            for (DetectionAttempt callback : callbacks) {
                DetectionAttempt.DetectionResult result = callback.shouldDetect(sensed, sensor, reason);
                if (result.priority > ret.priority) {
                    ret = result;
                }
            }
            return ret;
        });
    public static final Event<DissociationCleanup> DISSOCIATION_CLEANUP = EventFactory.createArrayBacked(DissociationCleanup.class,
        callbacks -> (possessor, formerHost) -> {
            for (DissociationCleanup callback : callbacks) {
                callback.cleanUpAfterDissociation(possessor, formerHost);
            }
        });
    public static final Event<HostDeath> HOST_DEATH = EventFactory.createArrayBacked(HostDeath.class,
        callbacks -> (player, host, deathCause) -> {
            for (HostDeath callback : callbacks) {
                callback.onHostDeath(player, host, deathCause);
            }
        });
    public static final Event<PostResurrection> POST_RESURRECTION = EventFactory.createArrayBacked(PostResurrection.class,
        callbacks -> (player, secondLife) -> {
            for (PostResurrection callback : callbacks) {
                callback.onResurrected(player, secondLife);
            }
        });

    public interface InventoryTransferCheck {
        TriState shouldTransfer(ServerPlayerEntity possessor, LivingEntity host);
    }

    public interface DetectionAttempt {
        DetectionResult shouldDetect(MobEntity sensed, MobEntity sensor, DetectionReason reason);

        enum DetectionReason {
            BUMP, ATTACKING, ATTACKED
        }

        enum DetectionResult {
            UNDETECTED(3), DEFAULT(0), DETECTED(1), CROWD_DETECTED(2);
            private final int priority;

            DetectionResult(int priority) {
                this.priority = priority;
            }
        }
    }

    public interface DissociationCleanup {
        void cleanUpAfterDissociation(ServerPlayerEntity possessor, LivingEntity formerHost);
    }

    public interface HostDeath {
        void onHostDeath(ServerPlayerEntity possessor, LivingEntity host, DamageSource deathCause);
    }

    public interface PostResurrection {
        void onResurrected(ServerPlayerEntity resurrected, LivingEntity secondLife);
    }
}
