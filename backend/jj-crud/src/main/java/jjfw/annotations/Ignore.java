package jjfw.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Marks a generated POJO field to be ignored for certain CRUD operations.
 * By default applies to CREATE and UPDATE (READ still returns the value).
 * Usage examples:
 *   @Ignore // ignore on create + update
 *   @Ignore({Ignore.Operation.CREATE}) // ignore only when creating
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {
    Operation[] value() default { Operation.CREATE, Operation.UPDATE };
    enum Operation { CREATE, UPDATE, READ }
}

