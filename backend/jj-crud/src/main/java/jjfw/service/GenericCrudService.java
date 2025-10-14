package jjfw.service;

import jjfw.meta.EntityMeta;
import org.jooq.Field;
import org.jooq.UpdatableRecord;
import org.jooq.Table;

/**
 * Generic CRUD service backed by EntityMeta discovered at runtime.
 */
public class GenericCrudService<T, R extends UpdatableRecord<R>> extends BaseService<T, R> {

    private final EntityMeta<T, R> meta;

    public GenericCrudService(EntityMeta<T, R> meta) {
        this.meta = meta;
    }

    @Override
    protected Table<R> getTable() {
        return meta.table();
    }

    @Override
    protected Field<Integer> getIdField() {
        return meta.idField();
    }

    @Override
    protected Class<T> getPojoClass() {
        return meta.pojoClass();
    }
}
