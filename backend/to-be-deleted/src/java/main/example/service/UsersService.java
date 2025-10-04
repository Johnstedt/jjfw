package Main.example.service;

import generated.jooq.tables.pojos.Users;
import generated.jooq.tables.records.UsersRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class UsersService extends BaseService<Users, UsersRecord> {

    @Override
    protected Table<UsersRecord> getTable() {
        return Tables.USERS;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.USERS.ID;
    }

    @Override
    protected Class<Users> getPojoClass() {
        return Users.class;
    }
}
