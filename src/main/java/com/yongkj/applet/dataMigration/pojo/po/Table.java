package com.yongkj.applet.dataMigration.pojo.po;

import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Table {

    private String name;
    private String createSql;
    private String deleteSql;
    private String selectDataSql;
    private String insertDataSql;
    private String updateDataSql;
    private String removeDataSql;
    private boolean isMaxCompute;
    private List<String> fieldNames;
    private Map<String, Field> mapField;

    private Table() {
        this.name = "";
        this.createSql = "";
        this.deleteSql = "";
        this.selectDataSql = "";
        this.insertDataSql = "";
        this.updateDataSql = "";
        this.removeDataSql = "";
        this.mapField = new HashMap<>();
        this.fieldNames = new ArrayList<>();
    }

    public static Map<String, Table> getTables(Manager manager, boolean isMaxCompute) {
        Map<String, Table> mapTable = new HashMap<>();
        List<String> lstTableName = getTableNamesBySql(manager);
        LogUtil.loggerLine(Log.of("DataMigration", "getTables", "lstTableName", lstTableName));
        LogUtil.loggerLine(Log.of("DataMigration", "getTables", "lstTableName.size()", lstTableName.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        List<String> filterTableName = new ArrayList<>();
        if (isMaxCompute) {
            Map<String, Object> mapData = GenUtil.getMap("max-compute-config");
            filterTableName = (List<String>) mapData.get("tables");
        }
        for (String tableName : lstTableName) {
            if (isMaxCompute && !filterTableName.isEmpty() && !filterTableName.contains(tableName)) {
                continue;
            }
            Map<String, String> mapRemark = new HashMap<>();
            if (!isMaxCompute) {
                mapRemark = getMapRemarkBySql(manager, tableName);
            }
            List<Field> lstField = Field.getFields(manager, tableName, mapRemark);
            String createSql = mapRemark.get("createSql") == null ? "" : mapRemark.get("createSql");
            List<String> fieldNames = lstField.stream().map(Field::getName).collect(Collectors.toList());

            Table table = new Table();
            table.setName(tableName);
            table.setCreateSql(createSql);
            table.setFieldNames(fieldNames);
            table.setMaxCompute(isMaxCompute);
            table.setMapField(Field.getMapField(lstField, createSql, tableName));
            mapTable.put(tableName, getSqlTable(table));
        }
        JDBCUtil.close(manager);
        return mapTable;
    }

    public static Table getSqlTable(Table table) {
        table.setDeleteSql(SQL.getTableDeleteSql(table.getName()));
        table.setSelectDataSql(getDataSelectSql(table));
        table.setInsertDataSql(getDataInsertSql(table));
        table.setUpdateDataSql(getDataUpdateSql(table));
        table.setRemoveDataSql(getDataRemoveSql(table));
        return table;
    }

    private static String getDataRemoveSql(Table table) {
        return SQL.getDataRemoveSql(
                table.name,
                ""
        );
    }

    private static String getDataUpdateSql(Table table) {
        return SQL.getDataUpdateSql(
                table.name,
                new HashMap<>(),
                ""
        );
    }

    private static String getDataInsertSql(Table table) {
        return SQL.getDataInsertSql(
                table.name,
                table.fieldNames,
                new ArrayList<>()
        );
    }

    private static String getDataSelectSql(Table table) {
        return SQL.getDataSelectSql(
                table.fieldNames,
                Collections.singletonList(table.name),
                ""
        );
    }

    private static Map<String, String> getMapRemarkBySql(Manager manager, String table) {
        Map<String, String> mapRemark = new HashMap<>();
        try {
            String sql = String.format("SHOW CREATE TABLE `%s`", table);
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);

            if (sqlResult != null) {
                while (sqlResult.next()) {
                    String createTableSql = sqlResult.getString("Create Table");

                    List<String> lstLine = GenUtil.getStrLines(createTableSql);
                    mapRemark = getMapRemark(lstLine);
                    mapRemark.put("createSql", createTableSql);
                }
            }

            manager.setStatement(statement);
            manager.setResultSet(sqlResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapRemark;
    }

    private static Map<String, String> getMapRemark(List<String> lstLine) {
        String regStr = "\\s+`(\\S+)`[\\s\\S]+COMMENT\\s'(.*)'[\\s\\S]+";
        Map<String, String> mapRemark = new HashMap<>();
        Pattern pattern = Pattern.compile(regStr);
        for (String line : lstLine) {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) continue;
            String field = matcher.group(1);
            String comment = matcher.group(2);
            mapRemark.put(field, comment);
        }
        return mapRemark;
    }

    public static List<String> getTableNamesBySql(Manager manager) {
        List<String> tables = new ArrayList<>();
        try {
            String sql = "SHOW TABLES";
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);

            while (sqlResult.next()) {
                tables.add(sqlResult.getString(1));
            }

            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    private static Map<String, String> getMapRemark(Manager manager, String table) {
        Map<String, String> mapRemark = new HashMap<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getColumns(null, "%", table, "%");
            ResultSetMetaData resultSetMeta = resultSet.getMetaData();

            int columnIndex = 1;
            int columnCount = resultSetMeta.getColumnCount();
            while (resultSet.next() && columnIndex <= columnCount) {
                String remark = resultSet.getString("REMARKS");
                String field = resultSet.getString("COLUMN_NAME");

                if (!mapRemark.containsKey(field)) {
                    mapRemark.put(field, remark);
                }
                columnIndex++;
            }

            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapRemark;
    }

    private static List<String> getTableNames(Manager manager, String database) {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(null, database, "%", new String[]{"TABLE"});

            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }

            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateSql() {
        return createSql;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    public String getSelectDataSql() {
        return selectDataSql;
    }

    public void setSelectDataSql(String selectDataSql) {
        this.selectDataSql = selectDataSql;
    }

    public String getInsertDataSql() {
        return insertDataSql;
    }

    public void setInsertDataSql(String insertDataSql) {
        this.insertDataSql = insertDataSql;
    }

    public String getUpdateDataSql() {
        return updateDataSql;
    }

    public void setUpdateDataSql(String updateDataSql) {
        this.updateDataSql = updateDataSql;
    }

    public String getRemoveDataSql() {
        return removeDataSql;
    }

    public void setRemoveDataSql(String removeDataSql) {
        this.removeDataSql = removeDataSql;
    }

    public Map<String, Field> getMapField() {
        return mapField;
    }

    public void setMapField(Map<String, Field> mapField) {
        this.mapField = mapField;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public boolean isMaxCompute() {
        return isMaxCompute;
    }

    public void setMaxCompute(boolean maxCompute) {
        isMaxCompute = maxCompute;
    }
}
