package Main.example.service;

import generated.jooq.tables.pojos.FixpSession;
import generated.jooq.tables.records.FixpSessionRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class FixpSessionService extends BaseService<FixpSession, FixpSessionRecord> {

    @Override
    protected Table<FixpSessionRecord> getTable() {
        return Tables.FIXP_SESSION;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.FIXP_SESSION.ID;
    }

    @Override
    protected Class<FixpSession> getPojoClass() {
        return FixpSession.class;
    }
}
