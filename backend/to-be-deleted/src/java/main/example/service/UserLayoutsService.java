package Main.example.service;

import generated.jooq.tables.pojos.UserLayouts;
import generated.jooq.tables.records.UserLayoutsRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class UserLayoutsService extends BaseService<UserLayouts, UserLayoutsRecord> {

    @Override
    protected Table<UserLayoutsRecord> getTable() {
        return Tables.USER_LAYOUTS;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.USER_LAYOUTS.ID;
    }

    @Override
    protected Class<UserLayouts> getPojoClass() {
        return UserLayouts.class;
    }
}
