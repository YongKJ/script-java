package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dict.SQLOperate;
import com.yongkj.applet.dataMigration.pojo.dto.SQLValue;

import java.util.*;
import java.util.function.Consumer;

public class Wrapper {

    private final String tableName;
    private final List<SQLValue> sqlValues;

    private Wrapper(String tableName) {
        this.tableName = tableName;
        this.sqlValues = new ArrayList<>();
    }

    public static Wrapper query(String tableName) {
        return new Wrapper(tableName);
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
                    lstSqlSegment.removeLast();
                    lstSqlSegment.addLast(sqlValue.getSqlSegment());
                    lstSqlSegment.addLast("and");
                    break;
                default:
                    lstSqlSegment.addLast(sqlValue.getSqlSegment());
                    lstSqlSegment.addLast("and");
            }
        }
        lstSqlSegment.removeLast();
        return String.join(" ", lstSqlSegment);
    }

    public Wrapper and() {
        sqlValues.add(SQLValue.of(SQLOperate.and));
        return this;
    }

    public Wrapper and(Consumer<Wrapper> consumer) {
        Wrapper query = Wrapper.query(tableName);
        consumer.accept(query);
        sqlValues.add(SQLValue.of(
                SQLOperate.andWrapper,
                query.getSqlSegment()
        ));
        return this;
    }

    public Wrapper or() {
        sqlValues.add(SQLValue.of(SQLOperate.or));
        return this;
    }

    public Wrapper or(Consumer<Wrapper> consumer) {
        Wrapper query = Wrapper.query(tableName);
        consumer.accept(query);
        sqlValues.add(SQLValue.of(
                SQLOperate.orWrapper,
                query.getSqlSegment()
        ));
        return this;
    }

    public Wrapper eq(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.eq,
                value
        ));
        return this;
    }

    public Wrapper ne(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.ne,
                value
        ));
        return this;
    }

    public Wrapper gt(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.gt,
                value
        ));
        return this;
    }

    public Wrapper ge(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.ge,
                value
        ));
        return this;
    }

    public Wrapper lt(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.lt,
                value
        ));
        return this;
    }

    public Wrapper le(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.le,
                value
        ));
        return this;
    }

    public <T> Wrapper in(String field, T... values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.in,
                Arrays.asList(values)
        ));
        return this;
    }

    public <T> Wrapper notIn(String field, T... values) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.le,
                Arrays.asList(values)
        ));
        return this;
    }

    public Wrapper isNull(String field) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.isNull,
                null
        ));
        return this;
    }

    public Wrapper isNotNUll(String field) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.isNotNUll,
                null
        ));
        return this;
    }

    public Wrapper like(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.like,
                value
        ));
        return this;
    }

    public Wrapper notLike(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notLike,
                value
        ));
        return this;
    }

    public Wrapper likeLeft(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.likeLeft,
                value
        ));
        return this;
    }

    public Wrapper likeRight(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.likeRight,
                value
        ));
        return this;
    }

    public Wrapper between(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.between,
                value
        ));
        return this;
    }

    public Wrapper notBetween(String field, Object value) {
        sqlValues.add(SQLValue.of(
                field,
                SQLOperate.notBetween,
                value
        ));
        return this;
    }

    public String getTableName() {
        return tableName;
    }
}
