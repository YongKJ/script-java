package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dict.SQLOperate;
import com.yongkj.applet.dataMigration.pojo.dto.SQLValue;
import com.yongkj.applet.dataMigration.pojo.po.Table;

import java.util.*;
import java.util.function.Consumer;

public class Wrappers {

    private final List<String> fields;
    private final List<String> tableNames;
    private final List<SQLValue> sqlValues;

    private Wrappers() {
        this.fields = new ArrayList<>();
        this.sqlValues = new ArrayList<>();
        this.tableNames = new ArrayList<>();
    }

    private Wrappers(String tableName) {
        this.fields = new ArrayList<>();
        this.sqlValues = new ArrayList<>();
        this.tableNames = new ArrayList<>();
        this.tableNames.add(tableName);
    }

    private Wrappers(String[] tableNames) {
        this.fields = new ArrayList<>();
        this.sqlValues = new ArrayList<>();
        this.tableNames = Arrays.asList(tableNames);
    }

    public static Wrappers lambdaQuery() {
        return new Wrappers();
    }

    public static Wrappers lambdaQuery(Table table) {
        return new Wrappers(table.getName());
    }

    public static Wrappers lambdaQuery(String tableName) {
        return new Wrappers(tableName);
    }

    public static Wrappers lambdaQuery(String... tableNames) {
        return new Wrappers(tableNames);
    }

    public String getSqlSegment() {
        ArrayDeque<String> lstSqlSegment = new ArrayDeque<>();
        for (SQLValue sqlValue : sqlValues) {
            switch (sqlValue.getOperate()) {
                case and:
                case or:
                    lstSqlSegment.removeLast();
                    lstSqlSegment.addLast(sqlValue.getSqlSegment());
                    break;
                case limit:
                case andWrapper:
                case orWrapper:
                case groupBy:
                case orderByAsc:
                case orderByDesc:
                    lstSqlSegment.removeLast();
                    lstSqlSegment.addLast(sqlValue.getSqlSegment());
                    lstSqlSegment.addLast(SQLOperate.and.getValue());
                    break;
                default:
                    lstSqlSegment.addLast(sqlValue.getSqlSegment());
                    lstSqlSegment.addLast(SQLOperate.and.getValue());
            }
        }
        if (!lstSqlSegment.isEmpty()) {
            lstSqlSegment.removeLast();
        }
        return String.join(" ", lstSqlSegment);
    }

    public Wrappers select(String... field) {
        fields.addAll(Arrays.asList(field));
        return this;
    }

    public Wrappers and() {
        sqlValues.add(SQLValue.of(SQLOperate.and));
        return this;
    }

    public Wrappers and(Consumer<Wrappers> consumer) {
        Wrappers query = Wrappers.lambdaQuery();
        consumer.accept(query);
        sqlValues.add(SQLValue.of(
                SQLOperate.andWrapper,
                query.getSqlSegment()
        ));
        return this;
    }

    public Wrappers or() {
        sqlValues.add(SQLValue.of(SQLOperate.or));
        return this;
    }

    public Wrappers or(Consumer<Wrappers> consumer) {
        Wrappers query = Wrappers.lambdaQuery();
        consumer.accept(query);
        sqlValues.add(SQLValue.of(
                SQLOperate.orWrapper,
                query.getSqlSegment()
        ));
        return this;
    }

    public Wrappers eq(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.eq,
                value
        ));
        return this;
    }

    public Wrappers ne(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.ne,
                value
        ));
        return this;
    }

    public Wrappers gt(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.gt,
                value
        ));
        return this;
    }

    public Wrappers ge(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.ge,
                value
        ));
        return this;
    }

    public Wrappers lt(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.lt,
                value
        ));
        return this;
    }

    public Wrappers le(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.le,
                value
        ));
        return this;
    }

    public <T> Wrappers in(String field, T... values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.in,
                Arrays.asList(values)
        ));
        return this;
    }

    public <T> Wrappers in(String field, List<T> values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.in,
                values
        ));
        return this;
    }

    public <T> Wrappers notIn(String field, T... values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notIn,
                Arrays.asList(values)
        ));
        return this;
    }

    public <T> Wrappers notIn(String field, List<T> values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notIn,
                values
        ));
        return this;
    }

    public Wrappers isNull(String field) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.isNull,
                null
        ));
        return this;
    }

    public Wrappers isNotNUll(String field) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.isNotNUll,
                null
        ));
        return this;
    }

    public Wrappers like(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.like,
                value
        ));
        return this;
    }

    public Wrappers notLike(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notLike,
                value
        ));
        return this;
    }

    public Wrappers likeLeft(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.likeLeft,
                value
        ));
        return this;
    }

    public Wrappers likeRight(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.likeRight,
                value
        ));
        return this;
    }

    public Wrappers limit(Integer value) {
        sqlValues.add(SQLValue.of(
                "",
                SQLOperate.limit,
                Collections.singletonList(value)
        ));
        return this;
    }

    public Wrappers limit(Integer startValue, Integer endValue) {
        sqlValues.add(SQLValue.of(
                "",
                SQLOperate.limit,
                Arrays.asList(startValue, endValue)
        ));
        return this;
    }

    public Wrappers between(String field, Object startValue, Object endValue) {
        Map<Integer, Object> mapData = new HashMap<>();
        mapData.put(1, startValue);
        mapData.put(2, endValue);
        return between(field, mapData);
    }

    private Wrappers between(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.between,
                value
        ));
        return this;
    }

    public Wrappers notBetween(String field, Object startValue, Object endValue) {
        Map<Integer, Object> mapData = new HashMap<>();
        mapData.put(1, startValue);
        mapData.put(2, endValue);
        return notBetween(field, mapData);
    }

    private Wrappers notBetween(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notBetween,
                value
        ));
        return this;
    }

    public Wrappers groupBy(String... fields) {
        sqlValues.add(SQLValue.of(
                "",
                SQLOperate.groupBy,
                Arrays.asList(fields)
        ));
        return this;
    }

    public Wrappers orderByAsc(String... fields) {
        sqlValues.add(SQLValue.of(
                "",
                SQLOperate.orderByAsc,
                Arrays.asList(fields)
        ));
        return this;
    }

    public Wrappers orderByDesc(String... fields) {
        sqlValues.add(SQLValue.of(
                "",
                SQLOperate.orderByDesc,
                Arrays.asList(fields)
        ));
        return this;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public String getTableName() {
        return tableNames.size() == 0 ? "" : tableNames.get(0);
    }

    public List<String> getFields() {
        return fields;
    }
}
