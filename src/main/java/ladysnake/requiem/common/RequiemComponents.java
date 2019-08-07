package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.common.impl.movement.MovementAltererManager;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.util.ObjectPath;
import net.minecraft.world.World;

public final class RequiemComponents {
    public static final ComponentType<SubDataManager> DIALOGUES = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("dialogue_registry"), SubDataManager.class
    );
    public static final ComponentType<SubDataManager> MOVEMENT_ALTERERS = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("movement_alterer"), SubDataManager.class
    );
    public static final ObjectPath<World, DialogueRegistry> DIALOGUE_REGISTRY = DIALOGUES
            .asComponentPath()
            .compose(ComponentProvider::fromWorld)
            .thenCastTo(DialogueRegistry.class);
    public static final ObjectPath<World, MovementAltererManager> MOVEMENT_ALTERER_MANAGER = MOVEMENT_ALTERERS
            .asComponentPath()
            .compose(ComponentProvider::fromWorld)
            .thenCastTo(MovementAltererManager.class);
    public static final ComponentType<PossessionComponent> POSSESSION = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("possession"), PossessionComponent.class
    );
}
