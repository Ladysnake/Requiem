package ladysnake.requiem.api.v1.annotation;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Indicates that a String should be used as a translation key
 */
@API(status = EXPERIMENTAL, since = "1.0.0")
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
public @interface Unlocalized {

}
