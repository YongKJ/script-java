package com.yongkj.applet.dataMigration.pojo.po;

import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

public class Field {

    private String table;
    private String name;
    private String type;
    private int length;
    private String notNull;
    private boolean isNotNull;
    private String defaultValue;
    private String comment;
    private String afterField;
    private String beforeField;
    private String createSql;
    private String modifySql;
    private String deleteSql;

    private Field() {
        this.table = "";
        this.name = "";
        this.type = "";
        this.length = 0;
        this.notNull = "";
        this.isNotNull = false;
        this.defaultValue = "";
        this.comment = "";
        this.afterField = "";
        this.beforeField = "";
        this.createSql = "";
        this.modifySql = "";
        this.deleteSql = "";
    }

    public static Map<String, Field> getFields(Manager manager, String tableName, Map<String, String> mapComment) {
        List<Field> fields = new ArrayList<>();
        try {
            String sql = String.format("SELECT * FROM `%s` WHERE 1=2", tableName);
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);
            ResultSetMetaData sqlResultMeta = sqlResult.getMetaData();

            int count = sqlResultMeta.getColumnCount();
            for (int col = 1; col <= count; col++) {
                String fieldName = sqlResultMeta.getColumnName(col);
                String type = sqlResultMeta.getColumnTypeName(col);
                int length = sqlResultMeta.getColumnDisplaySize(col);
                String comment = mapComment.get(fieldName) == null ? "" : String.format("COMMENT '%s'", mapComment.get(fieldName));
                boolean isNotNull = sqlResultMeta.isNullable(col) != ResultSetMetaData.columnNullable;
                String notNull = Objects.equals(type, "JSON") || !isNotNull ? "" : "NOT NULL";

                Field field = new Field();
                field.setTable(tableName);
                field.setName(fieldName);
                field.setType(type);
                field.setNotNull(isNotNull);
                field.setNotNull(notNull);
                field.setDefaultValue(
                        Objects.equals(type, "VARCHAR") ? "''" :
                                Objects.equals(type, "JSON") ? "NULL" : "'0'");
                if (Objects.equals(type, "VARCHAR") && length > 0) {
                    field.setLength(length);
                    field.setType(type + "(" + length + ")");
                }
                field.setComment(comment);
                fields.add(field);
            }

            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getMapField(fields);
    }

    public static Map<String, Field> getMapField(List<Field> lstField) {
        Map<String, Field> mapField = new HashMap<>();
        for (int i = 0; i < lstField.size(); i++) {
            String beforeField = i == 0 ? "" : lstField.get(i - 1).name;
            String afterField = i == lstField.size() - 1 ? "" : lstField.get(i + 1).name;
            Field field = lstField.get(i);
            field.setBeforeField(beforeField);
            field.setAfterField(afterField);
            mapField.put(field.name, getSqlField(field));
        }
        return mapField;
    }

    private static Field getSqlField(Field field) {
        field.setCreateSql(getCreateSQl(field));
        return field;
    }

    private static String getCreateSQl(Field field) {
        return SQL.getFieldCreateSql(
                field.getTable(), field.getName(), field.getType(),
                field.getNotNull(), field.getDefaultValue(), field.getComment(),
                field.getBeforeField().length() == 0 ? "BEFORE" : "AFTER",
                field.getBeforeField().length() == 0 ? field.getAfterField() : field.getBeforeField()
        );
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getNotNull() {
        return notNull;
    }

    public void setNotNull(String notNull) {
        this.notNull = notNull;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean notNull) {
        isNotNull = notNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAfterField() {
        return afterField;
    }

    public void setAfterField(String afterField) {
        this.afterField = afterField;
    }

    public String getBeforeField() {
        return beforeField;
    }

    public void setBeforeField(String beforeField) {
        this.beforeField = beforeField;
    }

    public String getCreateSql() {
        return createSql;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    public String getModifySql() {
        return modifySql;
    }

    public void setModifySql(String modifySql) {
        this.modifySql = modifySql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }
}
