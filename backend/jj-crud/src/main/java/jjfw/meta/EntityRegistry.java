package jjfw.meta;

import org.jooq.Table;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import jjfw.service.GenericCrudService;
import jjfw.service.BaseService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Discovers @JjEntity annotated POJOs and builds metadata + generic services.
 */
@Component
public class EntityRegistry {

    private final Map<Class<?>, EntityMeta<?, ? extends UpdatableRecord<?>>> metaByPojo = new ConcurrentHashMap<>();
    private final Map<Class<?>, BaseService<?, ? extends UpdatableRecord<?>>> serviceByPojo = new ConcurrentHashMap<>();

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private volatile boolean initialized = false;

    private synchronized void initIfNeeded() {
        if (initialized) return;
        scanAndRegister("generated.jooq.tables.pojos");
        initialized = true;
    }

    private void scanAndRegister(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JjEntity.class));
        scanner.findCandidateComponents(basePackage).forEach(def -> {
            try {
                Class<?> pojoClazz = Class.forName(def.getBeanClassName());
                JjEntity ann = pojoClazz.getAnnotation(JjEntity.class);
                if (ann == null) return;
                String tableName = ann.table();
                String idName = ann.id();
                Table<? extends Record> rawTable = resolveTable(tableName);
                if (rawTable == null) return;
                @SuppressWarnings("unchecked")
                Table<? extends UpdatableRecord<?>> table = (Table<? extends UpdatableRecord<?>>) rawTable;
                Field<Integer> idField = resolveIdField(rawTable, idName);
                if (idField == null) return;
                EntityMeta<?, ? extends UpdatableRecord<?>> meta = new EntityMeta(table, idField, pojoClazz);
                metaByPojo.put(pojoClazz, meta);
            } catch (Exception ignored) { }
        });
    }

    private Table<? extends Record> resolveTable(String tableName) {
        try {
            Class<?> tablesClass = Class.forName("generated.jooq.Tables");
            for (var field : tablesClass.getFields()) {
                if (Table.class.isAssignableFrom(field.getType())) {
                    Table<?> t = (Table<?>) field.get(null);
                    if (t.getName().equalsIgnoreCase(tableName)) {
                        return t;
                    }
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private Field<Integer> resolveIdField(Table<? extends Record> table, String idName) {
        for (Field<?> f : table.fields()) {
            if (f.getName().equalsIgnoreCase(idName) && Integer.class.equals(f.getType())) {
                @SuppressWarnings("unchecked")
                Field<Integer> cast = (Field<Integer>) f;
                return cast;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> GenericCrudService<T, ? extends UpdatableRecord<?>> genericService(Class<T> pojoClass) {
        initIfNeeded();
        return (GenericCrudService<T, ? extends UpdatableRecord<?>>) serviceByPojo.computeIfAbsent(pojoClass, pc -> {
            EntityMeta<T, ? extends UpdatableRecord<?>> meta = (EntityMeta<T, ? extends UpdatableRecord<?>>) metaByPojo.get(pc);
            if (meta == null) return null;
            GenericCrudService<T, ? extends UpdatableRecord<?>> svc = new GenericCrudService(meta);
            beanFactory.autowireBean(svc); // inject DSLContext
            return svc;
        });
    }

    public Collection<EntityMeta<?, ? extends UpdatableRecord<?>>> allEntities() {
        initIfNeeded();
        return Collections.unmodifiableCollection(metaByPojo.values());
    }
}
