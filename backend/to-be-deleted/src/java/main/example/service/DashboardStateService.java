package Main.example.service;

import generated.jooq.tables.pojos.DashboardState;
import generated.jooq.tables.records.DashboardStateRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class DashboardStateService extends BaseService<DashboardState, DashboardStateRecord> {

    @Override
    protected Table<DashboardStateRecord> getTable() {
        return Tables.DASHBOARD_STATE;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.DASHBOARD_STATE.ID;
    }

    @Override
    protected Class<DashboardState> getPojoClass() {
        return DashboardState.class;
    }
}
