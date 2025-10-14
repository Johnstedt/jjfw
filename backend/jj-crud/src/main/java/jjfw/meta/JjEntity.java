package jjfw.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation applied (via code generation) to generated jOOQ POJOs so that generic
 * CRUD infrastructure can discover table + id metadata at runtime without manual
 * per-entity service/controller classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JjEntity {
    String table();
    String id() default "ID"; // logical primary key column name
    boolean exposed() default true; // allow hiding from generic controllers
    boolean softDelete() default false; // future: interpret delete as soft-delete
    String softDeleteField() default ""; // name of timestamp/flag field for soft delete
}

