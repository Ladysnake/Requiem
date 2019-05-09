package ladysnake.requiem.common.advancement.criterion;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public abstract class CriterionBase<T extends CriterionConditions, H extends CriterionBase.Handler<T>> implements Criterion<T> {
    private final Map<PlayerAdvancementTracker, H> handlers = new HashMap<>();
    private final Identifier id;
    private final Function<PlayerAdvancementTracker, H> handlerFactory;

    public CriterionBase(Identifier id, Function<PlayerAdvancementTracker, H> handlerFactory) {
        this.id = id;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Nullable
    protected H getHandler(PlayerAdvancementTracker tracker) {
        return this.handlers.get(tracker);
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker tracker, ConditionsContainer<T> container) {
        H handler = this.handlers.get(tracker);
        if (handler == null) {
            handler = handlerFactory.apply(tracker);
            this.handlers.put(tracker, handler);
        }

        handler.addCondition(container);
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker tracker, ConditionsContainer<T> container) {
        H handler = this.handlers.get(tracker);
        if (handler != null) {
            handler.removeCondition(container);
            if (handler.isEmpty()) {
                this.handlers.remove(tracker);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker tracker) {
        this.handlers.remove(tracker);
    }
    
    public static class Handler<T extends CriterionConditions> {
        private final PlayerAdvancementTracker tracker;
        protected final Set<ConditionsContainer<T>> conditions = new HashSet<>();

        public Handler(PlayerAdvancementTracker tracker) {
            this.tracker = tracker;
        }

        public void addCondition(ConditionsContainer<T> conditionContainer) {
            this.conditions.add(conditionContainer);
        }

        public void removeCondition(Criterion.ConditionsContainer<T> conditionContainer) {
            this.conditions.remove(conditionContainer);
        }

        public boolean isEmpty() {
            return this.conditions.isEmpty();
        }

        protected void apply(@Nullable List<ConditionsContainer<T>> conditionsContainers) {
            if (conditionsContainers != null) {
                for (ConditionsContainer<T> container : conditionsContainers) {
                    container.apply(this.tracker);
                }
            }
        }
    }
}
