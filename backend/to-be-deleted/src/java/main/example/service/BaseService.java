package Main.example.service;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Record;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public abstract class BaseService<T, R extends Record> {

    @Autowired
    protected DSLContext dsl;

    protected abstract Table<R> getTable();
    protected abstract Field<Long> getIdField();
    protected abstract Class<T> getPojoClass();

    public List<T> findAll() {
        return dsl.selectFrom(getTable())
                 .fetchInto(getPojoClass());
    }

    public Optional<T> findById(Long id) {
        T result = dsl.selectFrom(getTable())
                     .where(getIdField().eq(id))
                     .fetchOneInto(getPojoClass());
        return Optional.ofNullable(result);
    }

    public T create(T entity) {
        R record = dsl.newRecord(getTable(), entity);

        dsl.insertInto(getTable())
                .set(record)
                .execute();
        return record.into(getPojoClass());
    }

    public Optional<T> update(Long id, T entity) {
        R record = dsl.selectFrom(getTable())
                     .where(getIdField().eq(id))
                     .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        record.from(entity);
        dsl.update(getTable())
                .set(record)
                .where(getIdField().eq(id))
                .execute();

        return Optional.of(record.into(getPojoClass()));
    }

    public boolean delete(Long id) {
        int deletedRows = dsl.deleteFrom(getTable())
                            .where(getIdField().eq(id))
                            .execute();
        return deletedRows > 0;
    }
}
