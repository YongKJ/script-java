package com.yongkj.applet.dataMigration.pojo.po;

import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Table {

    private String name;
    private String createSql;
    private String deleteSql;
    private String insertDataSql;
    private String updateDataSql;
    private String removeDataSql;
    private List<Field> lstField;

    private Table() {
        this.name = "";
        this.createSql = "";
        this.deleteSql = "";
        this.insertDataSql = "";
        this.updateDataSql = "";
        this.removeDataSql = "";
        this.lstField = new ArrayList<>();
    }

    public static List<Table> getTables(Manager manager, String database) {
        List<Table> tables = new ArrayList<>();
        List<String> lstTableName = getTableNames(manager, database);
        for (String tableName : lstTableName) {
            Map<String, String> mapRemark = getMapRemarkBySql(manager, tableName);
            String createSqs = mapRemark.get("createSql");
            Table table = new Table();
            table.setName(tableName);
            table.setCreateSql(createSqs);
            table.setLstField(Field.getFields(manager, tableName, mapRemark));
        }
        SQLUtil.close(manager);
        return tables;
    }

    private static Map<String, String> getMapRemarkBySql(Manager manager, String table) {
        Map<String, String> mapRemark = new HashMap<>();
        try {
            String sql = String.format("show create table %s", table);
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);

            while (sqlResult.next()) {
                String createTableSql = sqlResult.getString("Create Table");
                LogUtil.loggerLine(Log.of("DataMigration", "getMapRemarkBySql", "createTableSql", createTableSql));

                String lineBreak = createTableSql.contains("\r\n") ? "\r\n" : "\n";
                List<String> lstLine = Arrays.asList(createTableSql.split(lineBreak));
                mapRemark = getMapRemark(lstLine);
                mapRemark.put("createSql", createTableSql);
            }

            manager.setStatement(statement);
            manager.setResultSet(sqlResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapRemark;
    }

    private static Map<String, String> getMapRemark(List<String> lstLine) {
        String regStr = "\\s+`(\\S+)`[\\s\\S]+COMMENT\\s'(\\S+)'[\\s\\S]+";
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
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "field", field));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "remark", remark));

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
            LogUtil.loggerLine(Log.of("DataMigration", "getTableNames", "resultSet", resultSet));

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

    public List<Field> getLstField() {
        return lstField;
    }

    public void setLstField(List<Field> lstField) {
        this.lstField = lstField;
    }
}
