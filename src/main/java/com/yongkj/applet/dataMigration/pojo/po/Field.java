package com.yongkj.applet.dataMigration.pojo.po;

import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Field {

    private String table;
    private String name;
    private String type;
    private int length;
    private boolean isNotNull;
    private Object defaultValue;
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
        this.isNotNull = false;
        this.defaultValue = "";
        this.comment = "";
        this.afterField = "";
        this.beforeField = "";
        this.createSql = "";
        this.modifySql = "";
        this.deleteSql = "";
    }

    public static List<Field> getFields(Manager manager, String tableName, Map<String, String> mapComment) {
        LogUtil.loggerLine(Log.of("DataMigration", "getFields", "mapComment", mapComment));
        List<Field> fields = new ArrayList<>();
        try {
            String sql = String.format("select * from %s where 1=2", tableName);
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);
            ResultSetMetaData sqlResultMeta = sqlResult.getMetaData();

            int count = sqlResultMeta.getColumnCount();
            for (int col = 1; col <= count; col++) {
                String fieldName = sqlResultMeta.getColumnName(col);
                String type = sqlResultMeta.getColumnTypeName(col);
                int length = sqlResultMeta.getColumnDisplaySize(col);
                String comment = mapComment.get(fieldName);
                boolean isNotNull = sqlResultMeta.isNullable(col) != ResultSetMetaData.columnNullable;

                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "fieldName", fieldName));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "type", type));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "comment", comment));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "isNotNull", isNotNull));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "length", length));
                System.out.println("--------------------------------------------------------");

                Field field = new Field();
                field.setTable(tableName);
                field.setName(fieldName);
                field.setType(type);
                field.setNotNull(isNotNull);
                if (Objects.equals(type, "VARCHAR")) {
                    field.setLength(length);
                    field.setDefaultValue("");
                } else {
                    field.setDefaultValue(0);
                }
                field.setComment(comment);
                fields.add(field);
            }

            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
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

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean notNull) {
        isNotNull = notNull;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
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
