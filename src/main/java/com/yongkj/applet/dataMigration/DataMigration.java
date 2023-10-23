package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.service.DataIncrementMigrationService;
import com.yongkj.applet.dataMigration.service.FieldIncrementMigrationService;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        fieldIncrementMigrationService.apply();
        dataIncrementMigrationService.apply();
        SQLUtil.closeAll(srcDatabase.getManager());
        SQLUtil.closeAll(desDatabase.getManager());
    }

    public void test() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.dataSelectTest("广州", "city");
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "selectData", selectData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    public void test1() {
        String line = "  `sort` int NOT NULL DEFAULT '0' COMMENT '排序 越大越靠前',";
        String regStr = "\\s+`(\\S+)`[\\s\\S]+DEFAULT\\s'(.*?)'[\\s\\S]+";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String field = matcher.group(1);
            String defaultValue = matcher.group(2);
            LogUtil.loggerLine(Log.of("DataMigration", "test", "field", field));
            LogUtil.loggerLine(Log.of("DataMigration", "test", "defaultValue", defaultValue));
            System.out.println("------------------------------------------------------------------------------------------------------------");
        }
    }

    public void test2() {
        String line = "  `score` decimal(3,2) NOT NULL COMMENT '评分',";
        String regStr = String.format("\\s+`%s`\\s\\S+?\\((.*?)\\)[\\s\\S]+", "score");
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String decimalType = matcher.group(1);
            LogUtil.loggerLine(Log.of("DataMigration", "test1", "decimalType", decimalType));
            System.out.println("------------------------------------------------------------------------------------------------------------");
        }
    }

    public static void run(String[] args) {
        new DataMigration().apply();
//        new DataMigration().test1();
    }

}
