package com.yongkj.applet.dataMigration.pojo.po;

import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.util.GenUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<String> keys;
    private List<String> indexes;

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
        this.keys = new ArrayList<>();
        this.indexes = new ArrayList<>();
    }


    public static Map<String, Field> getMapField(List<Field> fields, String tableSql, String table) {
        return packKeyOrIndex(getMapField(fields), tableSql, table);
    }

    public static List<Field> getFields(Manager manager, String tableName, Map<String, String> mapComment) {
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
                String comment = mapComment.get(fieldName) == null ? "" : mapComment.get(fieldName);
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
                    field.setType(String.format("%s(%s)", type, length));
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

    private static Map<String, Field> packKeyOrIndex(Map<String, Field> mapField, String tableSql, String tableName) {
        if (tableSql == null || tableSql.length() == 0) return mapField;
        List<String> lstLine = GenUtil.getStrLines(tableSql);
        for (String line : lstLine) {
            if (!line.contains("KEY") && !line.contains("INDEX")) continue;
            String sql = getKeyOrIndexSql(line, tableName);
            List<String> fieldNames = getFieldNames(line);
            for (String fieldName : fieldNames) {
                if (!mapField.containsKey(fieldName)) continue;
                (lstLine.contains("KEY") ?
                        mapField.get(fieldName).getKeys() :
                        mapField.get(fieldName).getIndexes()).add(sql);
            }
        }
        return mapField;
    }

    private static String getKeyOrIndexSql(String line, String tableName) {
        if (line.endsWith(",")) line = line.substring(0, line.length() - 1);
        return String.format("ALTER TABLE %s ADD %s", tableName, line);
    }

    private static List<String> getFieldNames(String line) {
        String regStr = ".*\\((.*)\\).*";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return new ArrayList<>();
        String fieldNamesStr = matcher.group(1);
        regStr = "`(\\S+)`";
        pattern = Pattern.compile(regStr);
        matcher = pattern.matcher(fieldNamesStr);
        if (!matcher.find()) return new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            fieldNames.add(matcher.group(i));
        }
        return fieldNames;
    }

    private static Map<String, Field> getMapField(List<Field> lstField) {
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
        field.setModifySql(getModifySQl(field));
        field.setDeleteSql(getDeleteSQl(field));
        return field;
    }

    private static String getCreateSQl(Field field) {
        return SQL.getFieldCreateSql(
                field.getTable(), field.getName(), field.getType(),
                field.getNotNull(), field.getDefaultValue(), field.getComment(),
                field.getBeforeField().length() == 0 ? "BEFORE" : "AFTER",
                field.getBeforeField().length() == 0 ? field.getAfterField() : field.getBeforeField());
    }

    private static String getModifySQl(Field field) {
        return SQL.getFieldModifySql(
                field.getTable(), field.getName(), field.getType(),
                field.getNotNull(), field.getDefaultValue(), field.getComment());
    }

    private static String getDeleteSQl(Field field) {
        return SQL.getFieldDeleteSql(field.getTable(), field.getName());
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

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }
}
