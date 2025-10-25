package jjfw.service;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.*;
import jjfw.annotations.Ignore;

@Service
public abstract class BaseService<T, R extends UpdatableRecord<R>> {

    @Autowired
    protected DSLContext dsl;

    protected abstract Table<R> getTable();
    protected abstract Field<Integer> getIdField();
    protected abstract Class<T> getPojoClass();

    public List<T> findAll() {
        return dsl.selectFrom(getTable()).fetchInto(getPojoClass());
    }

    public Optional<T> findById(Integer id) {
        T result = dsl.selectFrom(getTable())
                .where(getIdField().eq(id))
                .fetchOneInto(getPojoClass());
        return Optional.ofNullable(result);
    }

    public T create(T entity) {
        R rec = dsl.newRecord(getTable());
        // Populate from POJO
        rec.from(entity);
        // Skip ID and ignored fields on create
        for (Field<?> f : rec.fields()) {
            if (f.equals(getIdField())) {
                rec.changed(f, false);
                continue;
            }
            String javaName = snakeToCamel(f.getName());
            if (isIgnored(javaName, Ignore.Operation.CREATE)) {
                rec.changed(f, false);
            }
        }
        rec.store(); // INSERT
        return rec.into(getPojoClass());
    }

    public Optional<T> update(Integer id, T entity) {
        R rec = dsl.fetchOne(getTable(), getIdField().eq(id));
        if (rec == null) return Optional.empty();

        // Preserve the current primary key to avoid it being overwritten by the incoming entity
        Integer currentId = rec.get(getIdField());

        // Populate new values from POJO (may set ID to null/different -> restore afterwards)
        rec.from(entity);

        // Restore ID and ensure it is not treated as changed
        rec.set(getIdField(), currentId);

        // Ensure ID not updated; respect @Ignore for UPDATE
        for (Field<?> f : rec.fields()) {
            if (f.equals(getIdField())) {
                rec.changed(f, false);
                continue;
            }
            String javaName = snakeToCamel(f.getName());
            if (isIgnored(javaName, Ignore.Operation.UPDATE)) {
                rec.changed(f, false);
            }
        }
        rec.store(); // UPDATE
        return Optional.of(rec.into(getPojoClass()));
    }

    public boolean delete(Integer id) {
        int deletedRows = dsl.deleteFrom(getTable())
                .where(getIdField().eq(id))
                .execute();
        return deletedRows > 0;
    }

    // Generic filtering support (equality, range, like, sorting, pagination)
    public List<T> filter(Map<String, String> rawParams) {
        Map<String, String> params = new HashMap<>(rawParams); // defensive copy
        Map<String, Field<?>> fieldMap = Arrays.stream(getTable().fields())
                .collect(Collectors.toMap(f -> f.getName(), f -> f)); // key: snake_case

        int limit = parseIntOrDefault(params.remove("limit"), 100);
        int offset = parseIntOrDefault(params.remove("offset"), 0);
        String sort = params.remove("sort");

        Condition condition = DSL.trueCondition();

        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null || value.isBlank()) continue;
            String keyLower = key.toLowerCase(Locale.ROOT);

            // Range: fieldLow / fieldHigh
            if (keyLower.endsWith("low") || keyLower.endsWith("high")) {
                boolean isLow = keyLower.endsWith("low");
                String base = key.substring(0, key.length() - (isLow ? 3 : 4));
                String snake = toSnakeCase(base);
                Field<?> fld = fieldMap.get(snake);
                if (fld == null) continue;
                Object typed = convertValue(fld, value);
                if (!(typed instanceof Comparable)) continue;
                @SuppressWarnings("unchecked") Field<Comparable> cmp = (Field<Comparable>) fld;
                condition = condition.and(isLow ? cmp.greaterOrEqual((Comparable) typed)
                        : cmp.lessOrEqual((Comparable) typed));
                continue;
            }

            // Like: fieldLike
            if (keyLower.endsWith("like")) {
                String base = key.substring(0, key.length() - 4);
                String snake = toSnakeCase(base);
                Field<?> fld = fieldMap.get(snake);
                if (fld == null) continue;
                if (CharSequence.class.isAssignableFrom(fld.getType())) {
                    @SuppressWarnings("unchecked") Field<String> sf = (Field<String>) fld;
                    condition = condition.and(DSL.lower(sf).like("%" + value.toLowerCase(Locale.ROOT) + "%"));
                }
                continue;
            }

            String snake = toSnakeCase(key);
            Field<?> fld = fieldMap.get(snake);
            if (fld == null) continue;
            Object typed = convertValue(fld, value);
            if (typed == null) continue;
            @SuppressWarnings("unchecked") Field<Object> of = (Field<Object>) fld;
            condition = condition.and(of.eq(typed));
        }

        List<SortField<?>> sortFields = parseSort(sort, fieldMap);

        SelectConditionStep<R> base = dsl.selectFrom(getTable()).where(condition);
        SelectLimitStep<R> limitBase = sortFields.isEmpty() ? base : base.orderBy(sortFields);

        // Ensure offset without explicit limit still works: supply large limit if needed
        if (offset > 0 && limit <= 0) {
            limit = 100; // default safeguard
        }

        if (limit > 0) {
            if (offset > 0) {
                return limitBase.limit(limit).offset(offset).fetchInto(getPojoClass());
            }
            return limitBase.limit(limit).fetchInto(getPojoClass());
        }

        if (offset > 0) {
            return limitBase.limit(Integer.MAX_VALUE).offset(offset).fetchInto(getPojoClass());
        }

        return limitBase.fetchInto(getPojoClass());
    }

    // Annotation helpers
    private boolean isIgnored(String javaFieldName, Ignore.Operation op) {
        try {
            java.lang.reflect.Field jf = getPojoClass().getDeclaredField(javaFieldName);
            Ignore ig = jf.getAnnotation(Ignore.class);
            if (ig == null) return false;
            for (Ignore.Operation o : ig.value()) if (o == op) return true;
            return false;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    // camelCase <-> snake_case utilities
    private String snakeToCamel(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') { up = true; continue; }
            if (up) { sb.append(Character.toUpperCase(c)); up = false; }
            else sb.append(c);
        }
        return sb.toString();
    }

    private String toSnakeCase(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private List<SortField<?>> parseSort(String sort, Map<String, Field<?>> fieldMap) {
        if (sort == null || sort.isBlank()) return Collections.emptyList();
        List<SortField<?>> result = new ArrayList<>();
        for (String part : sort.split(",")) {
            String[] kv = part.trim().split(":");
            String name = kv[0].trim();
            String snake = toSnakeCase(name);
            Field<?> fld = fieldMap.get(snake);
            if (fld == null) continue;
            boolean desc = kv.length > 1 && kv[1].equalsIgnoreCase("desc");
            result.add(desc ? fld.desc() : fld.asc());
        }
        return result;
    }

    private int parseIntOrDefault(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException ex) { return def; }
    }

    private Object convertValue(Field<?> fld, String raw) {
        Class<?> type = fld.getType();
        try {
            if (type == String.class) return raw;
            if (type == Integer.class) return Integer.valueOf(raw);
            if (type == Long.class) return Long.valueOf(raw);
            if (type == BigDecimal.class) return new BigDecimal(raw);
            if (type == Boolean.class) return Boolean.valueOf(raw);
            if (type == Double.class) return Double.valueOf(raw);
            if (type == Float.class) return Float.valueOf(raw);
            if (type == LocalDate.class) return LocalDate.parse(raw);
            if (type == LocalDateTime.class) return LocalDateTime.parse(raw);
            if (type == OffsetDateTime.class) return OffsetDateTime.parse(raw);
            return raw;
        } catch (Exception ex) {
            return null;
        }
    }
}
