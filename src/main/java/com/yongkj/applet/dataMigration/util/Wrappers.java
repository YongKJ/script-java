package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dict.SQLOperate;
import com.yongkj.applet.dataMigration.pojo.dto.SQLValue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Wrappers {

    private final String tableName;
    private final List<String> fields;
    private final List<SQLValue> sqlValues;

    private Wrappers() {
        this.tableName = "";
        this.fields = new ArrayList<>();
        this.sqlValues = new ArrayList<>();
    }

    private Wrappers(String tableName) {
        this.tableName = tableName;
        this.fields = new ArrayList<>();
        this.sqlValues = new ArrayList<>();
    }

    public static Wrappers lambdaQuery() {
        return new Wrappers();
    }

    public static Wrappers lambdaQuery(String tableName) {
        return new Wrappers(tableName);
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
        if (lstSqlSegment.size() > 0) {
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
        Wrappers query = Wrappers.lambdaQuery(tableName);
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
        Wrappers query = Wrappers.lambdaQuery(tableName);
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

    public Wrappers between(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.between,
                value
        ));
        return this;
    }

    public Wrappers notBetween(String field, Object value) {
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

    public String getTableName() {
        return tableName;
    }

    public List<String> getFields() {
        return fields;
    }
}
