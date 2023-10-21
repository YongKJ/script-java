package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.List;

public class IncrementMigrationService {

    private final Database srcDatabase;
    private final Database desDatabase;
    private final List<String> tableNames;

    public IncrementMigrationService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
        this.tableNames = GenUtil.getList("table-names");
    }

    public void apply() {
        for (String tableName : tableNames) {
            compareAndMigrationTable(tableName);
        }
    }

    private void compareAndMigrationTable(String tableName) {
        if (!srcDatabase.getTableNames().contains(tableName)) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "compareAndMigrationTable", "error", "srcTable 不存在！"));
            return;
        }
        String tableOperate = !srcDatabase.getTableNames().contains(tableName) ? "create" :
                desDatabase.getTableNames().;
        switch (tableOperate) {
            case "add":
            case "update":
        }
    }

    private void compareAndMigrationField(String fieldName) {

    }

}
