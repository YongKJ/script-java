package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.service.DataIncrementMigrationService;
import com.yongkj.applet.dataMigration.service.FieldIncrementMigrationService;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.util.List;

public class DataMigration {

    private final Database srcDatabase;
    private final Database desDatabase;
    private final DataIncrementMigrationService dataIncrementMigrationService;
    private final FieldIncrementMigrationService fieldIncrementMigrationService;

    private DataMigration() {
        this.srcDatabase = Database.get("src");
        this.desDatabase = Database.get("des");
        this.dataIncrementMigrationService = new DataIncrementMigrationService(
                this.srcDatabase, this.desDatabase
        );
        this.fieldIncrementMigrationService = new FieldIncrementMigrationService(
                this.srcDatabase, this.desDatabase
        );
    }

    private void apply() {
        List<String> srcDatabases = Database.getDatabases(srcDatabase.getManager());
        List<String> desDatabases = Database.getDatabases(desDatabase.getManager());
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases", srcDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases.size()", srcDatabases.size()));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases", desDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases.size()", desDatabases.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        srcDatabases = Database.getDatabasesBySql(srcDatabase.getManager());
        desDatabases = Database.getDatabasesBySql(desDatabase.getManager());
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases", srcDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases.size()", srcDatabases.size()));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases", desDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases.size()", desDatabases.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        if (!srcDatabases.contains(srcDatabase.getName()) || !desDatabases.contains(desDatabase.getName())) {
            System.out.println("数据库不存在！");
            return;
        }

//        fieldIncrementMigrationService.apply();

        String selectSql = dataIncrementMigrationService.dataSelectTest("广州", "city");
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "selectSql", selectSql));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        SQLUtil.closeAll(srcDatabase.getManager());
        SQLUtil.closeAll(desDatabase.getManager());
    }

    public static void run(String[] args) {
        new DataMigration().apply();
    }

}
