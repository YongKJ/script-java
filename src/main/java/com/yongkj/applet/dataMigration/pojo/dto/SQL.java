package com.yongkj.applet.dataMigration.pojo.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SQL {

    private static String TABLE_DELETE_SQL = "DROP TABLE IF EXISTS `%s`";
    private static String FIELD_CREATE_SQL = "ALTER TABLE `%s` ADD COLUMN `%s` %s %s %s %s %s";
    private static String FIELD_MODIFY_SQL = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s %s %s %s %s";
    private static String FIELD_DELETE_SQL = "ALTER TABLE `%s` DROP COLUMN `%s`";
    private static String DATA_SELECT_SQL = "SELECT %s FROM %s %s";
    private static String DATA_INSERT_SQL = "INSERT INTO `%s` (%s) VALUE(%s)";
    private static String DATA_UPDATE_SQL = "UPDATE `%s` SET %s %s";
    private static String DATA_REMOVE_SQL = "DELETE FROM `%s` WHERE %s";

    public static String getTableDeleteSql(String table) {
        return String.format(TABLE_DELETE_SQL, table);
    }

    public static String getFieldCreateSql(String table, String field, String type, String notNull, String defaultValue, String comment, String position, String positionField) {
        comment = comment.length() == 0 ? comment : String.format("COMMENT '%s'", comment);
        defaultValue = defaultValue == null ? "" : Objects.equals(defaultValue, "NULL") ?
                String.format("DEFAULT %s", defaultValue) : String.format("DEFAULT '%s'", defaultValue);
        position = Objects.equals(position, "BEFORE") ? "FIRST" : String.format("%s `%s`", position, positionField);
        return String.format(FIELD_CREATE_SQL, table, field, type, notNull, defaultValue, comment, position);
    }

    public static String getFieldModifySql(String table, String field, String type, String notNull, String defaultValue, String comment, String position, String positionField) {
        comment = comment.length() == 0 ? comment : String.format("COMMENT '%s'", comment);
        defaultValue = defaultValue == null ? "" : Objects.equals(defaultValue, "NULL") ?
                String.format("DEFAULT %s", defaultValue) : String.format("DEFAULT '%s'", defaultValue);
        position = Objects.equals(position, "BEFORE") ? "FIRST" : String.format("%s `%s`", position, positionField);
        return String.format(FIELD_MODIFY_SQL, table, field, type, notNull, defaultValue, comment, position);
    }

    public static String getFieldDeleteSql(String table, String field) {
        return String.format(FIELD_DELETE_SQL, table, field);
    }

    public static String getDataSelectSql(List<String> fields, List<String> tables, String where) {
        String field = getFieldOrTableStr(fields);
        String table = getFieldOrTableStr(tables);
        where = where.length() == 0 ? where : String.format("WHERE %s", where);
        return String.format(DATA_SELECT_SQL, field, table, where);
    }

    public static String getDataInsertSql(String table, List<String> fields, List<String> lstData) {
        String field = getFieldOrTableStr(fields);
        String data = String.join(", ", lstData);
        return String.format(DATA_INSERT_SQL, table, field, data);
    }

    public static String getDataUpdateSql(String table, Map<String, Object> mapData, String where) {
        String data = getMapDataStr(mapData);
        where = where.length() == 0 ? where : String.format("WHERE %s", where);
        return String.format(DATA_UPDATE_SQL, table, data, where);
    }

    public static String getDataRemoveSql(String table, String where) {
        return String.format(DATA_REMOVE_SQL, table, where);
    }

    private static String getMapDataStr(Map<String, Object> mapData) {
        List<String> lstData = new ArrayList<>();
        for (Map.Entry<String, Object> map : mapData.entrySet()) {
            String data = String.format("`%s` = %s", map.getKey(),
                    map.getValue() instanceof String ?
                            String.format("'%s'", map.getValue()) : map.getValue());
            lstData.add(data);
        }
        return String.join(", ", lstData);
    }

    private static String getFieldOrTableStr(List<String> lstData) {
        lstData = lstData.stream().map(f -> f.contains("`") ? String.format("%s", f) : String.format("`%s`", f)).collect(Collectors.toList());
        return String.join(", ", lstData);
    }
}
