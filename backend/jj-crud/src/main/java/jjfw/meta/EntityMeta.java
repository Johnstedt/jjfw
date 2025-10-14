package jjfw.meta;

import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.Field;

public record EntityMeta<T, R extends UpdatableRecord<R>>(Table<R> table, Field<Integer> idField, Class<T> pojoClass) {}
