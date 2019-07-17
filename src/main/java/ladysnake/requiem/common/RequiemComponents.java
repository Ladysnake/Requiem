package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.impl.movement.MovementAltererManager;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;

public final class RequiemComponents {
    public static final ComponentType<DialogueRegistry> DIALOGUES = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("dialogue_registry"), DialogueRegistry.class
    );
    public static final ComponentType<MovementAltererManager> MOVEMENT_ALTERERS = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("movement_alterer"), MovementAltererManager.class
    );
    public static final ComponentType<PossessionComponent> POSSESSION = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("possession"), PossessionComponent.class
    );
}
