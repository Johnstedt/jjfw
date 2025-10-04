package Main.example.service;

import generated.jooq.tables.pojos.OrderUserMap;
import generated.jooq.tables.records.OrderUserMapRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class OrderUserMapService extends BaseService<OrderUserMap, OrderUserMapRecord> {

    @Override
    protected Table<OrderUserMapRecord> getTable() {
        return Tables.ORDER_USER_MAP;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.ORDER_USER_MAP.USER_ID.as("ID");
    }

    @Override
    protected Class<OrderUserMap> getPojoClass() {
        return OrderUserMap.class;
    }
}
