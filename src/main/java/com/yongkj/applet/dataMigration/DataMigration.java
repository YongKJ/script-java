package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

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
        List<String> fieldNames = getFields(srcManager, databaseName, tableNames.get(0));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "fieldNames", fieldNames));
        SQLUtil.closeAll(srcManager);
        SQLUtil.closeAll(desManager);
    }

    private List<String> getFields(Manager manager, String database, String table) {
        List<String> fields = new ArrayList<>();
        try {
            int columnIndex = 1;
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getColumns(null, database, table, "%");
            LogUtil.loggerLine(Log.of("DataMigration", "getTableNames", "resultSet", resultSet));
            while (resultSet.next()) {
                String field = resultSet.getString("COLUMN_NAME");
                String type = resultSet.getString("TYPE_NAME");
                String remark = resultSet.getString("REMARKS");
                LogUtil.loggerLine(Log.of("DataMigration", "apply", "field", field));
                LogUtil.loggerLine(Log.of("DataMigration", "apply", "type", type));
                LogUtil.loggerLine(Log.of("DataMigration", "apply", "remark", remark));

                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int fieldLength = resultSetMetaData.getPrecision(columnIndex);
                int isNullable = resultSetMetaData.isNullable(columnIndex);
                LogUtil.loggerLine(Log.of("DataMigration", "apply", "fieldLength", fieldLength));
                LogUtil.loggerLine(Log.of("DataMigration", "apply", "isNullable", isNullable));
                System.out.println("--------------------------------------------------------");
                columnIndex++;

                fields.add(field);
            }
            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
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
