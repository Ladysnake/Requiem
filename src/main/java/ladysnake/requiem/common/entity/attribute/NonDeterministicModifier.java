package ladysnake.requiem.common.entity.attribute;

import java.util.OptionalDouble;
import java.util.function.DoubleFunction;

public interface NonDeterministicModifier extends DoubleFunction<OptionalDouble> {
    @Override
    OptionalDouble apply(double value);

    default NonDeterministicModifier andThen(NonDeterministicModifier after) {
        return value -> after.apply(this.apply(value).orElse(value));
    }
}
