package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMigration {

    private Manager srcManager;
    private Manager desManager;
    private Database srcDatabase;
    private Database desDatabase;
    private String databaseName;
    private List<String> tableNames;

    private DataMigration() {
        this.srcDatabase = Database.get("src");
        this.desDatabase = Database.get("des");
        this.tableNames = GenUtil.getList("table-names");
        this.srcManager = SQLUtil.getConnection(srcDatabase);
        this.desManager = SQLUtil.getConnection(desDatabase);
        this.databaseName = GenUtil.getValue("databse-name");
    }

    private void apply() {
        List<String> databases = getDatabases(srcManager);
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "databases", databases));

        List<String> tables = getTables(srcManager, databaseName);
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "tableNames", tables));

        List<String> fieldNames = getFields(srcManager, tableNames.get(0));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "fieldNames", fieldNames));

        SQLUtil.closeAll(srcManager);
        SQLUtil.closeAll(desManager);
    }

    private List<String> getFields(Manager manager, String table) {
        List<String> fields = new ArrayList<>();
        try {
            String sql = String.format("select * from %s where 1=2", table);
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);
            ResultSetMetaData sqlResultMeta = sqlResult.getMetaData();

            int count = sqlResultMeta.getColumnCount();
//            Map<String, String> mapRemark = getMapRemark(manager, table);
            Map<String, String> mapRemark = getMapRemarkBySql(manager, table);
            LogUtil.loggerLine(Log.of("DataMigration", "getFields", "mapRemark", mapRemark));
            for (int col = 1; col <= count; col++) {
                String field = sqlResultMeta.getColumnName(col);
                String type = sqlResultMeta.getColumnTypeName(col);
                String remark = mapRemark.get(field);
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "field", field));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "type", type));
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "remark", remark));

                boolean isNotNull = sqlResultMeta.isNullable(col) != ResultSetMetaData.columnNullable;
                LogUtil.loggerLine(Log.of("DataMigration", "getFields", "isNotNull", isNotNull));
                if (Objects.equals(type, "VARCHAR")) {
                    int fieldLength = sqlResultMeta.getColumnDisplaySize(col);
                    LogUtil.loggerLine(Log.of("DataMigration", "getFields", "fieldLength", fieldLength));
                }
                System.out.println("--------------------------------------------------------");

                fields.add(field);
            }
            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
    }


    private Map<String, String> getMapRemarkBySql(Manager manager, String table) {
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
            }
            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapRemark;
    }

    private Map<String, String> getMapRemark(List<String> lstLine) {
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

    private Map<String, String> getMapRemark(Manager manager, String table) {
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

    private List<String> getTables(Manager manager, String database) {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(null, database, "%", new String[]{"TABLE"});
            LogUtil.loggerLine(Log.of("DataMigration", "getTableNames", "resultSet", resultSet));
            while (resultSet.next()) {
                String table = resultSet.getString("TABLE_NAME");
                tables.add(table);
            }
            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    private List<String> getDatabases(Manager manager) {
        List<String> databases = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            LogUtil.loggerLine(Log.of("DataMigration", "getDatabases", "resultSet", resultSet));
            while (resultSet.next()) {
                String database = resultSet.getString("TABLE_CAT");
                databases.add(database);
            }
            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databases;
    }

    public static void run(String[] args) {
        new DataMigration().apply();
    }

}
