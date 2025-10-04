package Main.example.service;

import generated.jooq.tables.pojos.Orders;
import generated.jooq.tables.records.OrdersRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class OrdersService extends BaseService<Orders, OrdersRecord> {

    @Override
    protected Table<OrdersRecord> getTable() {
        return Tables.ORDERS;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.ORDERS.ORDER_ID;
    }

    @Override
    protected Class<Orders> getPojoClass() {
        return Orders.class;
    }
}
