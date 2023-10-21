package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.List;
import java.util.Objects;

public class IncrementMigrationService {

    private final boolean tableDelete;
    private final Database srcDatabase;
    private final Database desDatabase;
    private final List<String> tableNames;

    public IncrementMigrationService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
        this.tableNames = GenUtil.getList("table-names");
        this.tableDelete = Objects.equals(GenUtil.getValue("table-delete"), "true");
    }

    public void apply() {
        for (String tableName : tableNames) {
            compareAndMigrationTable(tableName);
        }
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
        switch (tableOperate) {
            case "add":
            case "update":
            case "delete":
            default:
        }
    }

    private void compareAndMigrationField(String fieldName) {

    }

}
