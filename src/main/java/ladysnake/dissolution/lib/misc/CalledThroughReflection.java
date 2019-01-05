package ladysnake.dissolution.lib.misc;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * A simple annotation to tell static analysis tools (and other people) to shut up about "unused" code
 */
@API(status = MAINTAINED, since = "2.6.2")
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface CalledThroughReflection {
}
