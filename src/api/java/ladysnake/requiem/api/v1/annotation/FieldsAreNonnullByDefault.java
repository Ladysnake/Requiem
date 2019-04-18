package ladysnake.requiem.api.v1.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be applied to a package, class or field to indicate that
 * the fields in that element are nonnull by default unless there is:
 * <ul>
 * <li>An explicit nullness annotation
 * <li>a default parameter annotation applied to a more tightly nested
 * element.
 * </ul>
 *
 */
@Documented
@Nonnull
@TypeQualifierDefault(ElementType.FIELD) // Note: This is a copy of javax.annotation.ParametersAreNonnullByDefault with target changed to FIELD
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsAreNonnullByDefault {}