package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class IncrementMigrationService {

    private final boolean tableDelete;
    private final boolean fieldDelete;
    private final Database srcDatabase;
    private final Database desDatabase;
    private final List<String> tableNames;

    public IncrementMigrationService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
        this.tableNames = GenUtil.getList("table-names");
        this.tableDelete = Objects.equals(GenUtil.getValue("table-delete"), "true");
        this.fieldDelete = Objects.equals(GenUtil.getValue("field-delete"), "true");
    }

    public void apply() {
        for (String tableName : tableNames) {
            compareAndMigrationTable(tableName);
        }
        SQLUtil.closeAll(srcDatabase.getManager());
        SQLUtil.closeAll(desDatabase.getManager());
    }

    private void compareAndMigrationTable(String tableName) {
        if (!tableDelete && !srcDatabase.getTableNames().contains(tableName)) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "compareAndMigrationTable", "error", "srcTable no exist!"));
            return;
        }
        String tableOperate = !desDatabase.getTableNames().contains(tableName) ? "create" : "update";
        if (tableDelete && !srcDatabase.getTableNames().contains(tableName)) {
            tableOperate = "delete";
        }
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        switch (tableOperate) {
            case "create":
                createDesTable(srcTable);
                break;
            case "update":
                updateDesTable(srcTable, desTable);
                break;
            case "delete":
                deleteDesTable(desTable);
                break;
            default:
        }
    }

    private void updateDesTable(Table srcTable, Table desTable) {
        if (srcTable == null || desTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "error", "srcTable or desTable not exist!"));
            return;
        }
    }

    private void deleteDesTable(Table desTable) {
        if (desTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "error", "desTable not exist!"));
            return;
        }
        try {
            Statement statement = desDatabase.getManager().getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(desTable.getDeleteSql());
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "success", "deleteDesTable success!"));

            desDatabase.getManager().setResultSet(sqlResult);
            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDesTable(Table srcTable) {
        if (srcTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "error", "srcTable no exist!"));
            return;
        }
        try {
            Statement statement = desDatabase.getManager().getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(srcTable.getCreateSql());
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "success", "createDesTable success!"));

            desDatabase.getManager().setResultSet(sqlResult);
            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareAndMigrationField(Table srcTale, Table desTable, String fieldName) {

    }

}
