package Main.example.service;

import generated.jooq.tables.pojos.Trees;
import generated.jooq.tables.records.TreesRecord;
import generated.jooq.Tables;
import org.jooq.Table;
import org.jooq.Field;
import org.springframework.stereotype.Service;

@Service
public class TreesService extends BaseService<Trees, TreesRecord> {

    @Override
    protected Table<TreesRecord> getTable() {
        return Tables.TREES;
    }

    @Override
    protected Field<Long> getIdField() {
        return Tables.ORDERS.ORDER_ID; //Tables.TREES.ID;
    }

    @Override
    protected Class<Trees> getPojoClass() {
        return Trees.class;
    }
}
