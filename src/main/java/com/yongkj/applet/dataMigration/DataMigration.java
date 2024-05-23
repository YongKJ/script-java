package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.service.*;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMigration {

    private final Database srcDatabase;
    private final Database desDatabase;
    private final Database devDatabase;
    private final Database preDatabase;
    private final Database testDatabase;
    private final Database prodDatabase;
    private final Map<String, Database> mapDatabase;
    private final TokenManagementService tokenManagementService;
    private final AgreementsUpdateService agreementsUpdateService;
    private final ShopCancelLogoutService shopCancelLogoutService;
    private final ShopWorkerDataExportService shopWorkerDataExportService;
    private final SmallAssignmentUpdateService smallAssignmentUpdateService;
    private final AdminMenuDataMigrationService adminMenuDataMigrationService;
    private final DataIncrementMigrationService dataIncrementMigrationService;
    private final FieldIncrementMigrationService fieldIncrementMigrationService;

    private DataMigration() {
        this.mapDatabase = Database.initDMapDatabase();
        this.devDatabase = Database.get("dev", mapDatabase);
        this.preDatabase = Database.get("pre", mapDatabase);
        this.testDatabase = Database.get("test", mapDatabase);
        this.prodDatabase = Database.get("prod", mapDatabase);
        this.srcDatabase = Database.get("src", this);
        this.desDatabase = Database.get("des", this);
        this.tokenManagementService = new TokenManagementService(this);
        this.adminMenuDataMigrationService = new AdminMenuDataMigrationService(this);
        this.agreementsUpdateService = new AgreementsUpdateService(this);
        this.shopCancelLogoutService = new ShopCancelLogoutService(this);
        this.shopWorkerDataExportService = new ShopWorkerDataExportService(this);
        this.smallAssignmentUpdateService = new SmallAssignmentUpdateService(this);
        this.dataIncrementMigrationService = new DataIncrementMigrationService(this);
        this.fieldIncrementMigrationService = new FieldIncrementMigrationService(this);
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

        tokenManagementService.apply();
        agreementsUpdateService.apply();
        shopCancelLogoutService.apply();
        shopWorkerDataExportService.apply();
        smallAssignmentUpdateService.apply();
        adminMenuDataMigrationService.apply();
        dataIncrementMigrationService.apply();
        fieldIncrementMigrationService.apply();
        JDBCUtil.closeAll(srcDatabase.getManager());
        JDBCUtil.closeAll(desDatabase.getManager());
        JDBCUtil.closeAll(devDatabase.getManager());
        JDBCUtil.closeAll(preDatabase.getManager());
        JDBCUtil.closeAll(testDatabase.getManager());
        JDBCUtil.closeAll(prodDatabase.getManager());
    }

    public Map<String, Database> getMapDatabase() {
        return mapDatabase;
    }

    public Database getDevDatabase() {
        return devDatabase;
    }

    public Database getPreDatabase() {
        return preDatabase;
    }

    public Database getTestDatabase() {
        return testDatabase;
    }

    public Database getProdDatabase() {
        return prodDatabase;
    }

    public Database getSrcDatabase() {
        return srcDatabase;
    }

    public Database getDesDatabase() {
        return desDatabase;
    }

    public void test() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.dataSelectTest("广州", "city");
        LogUtil.loggerLine(Log.of("DataMigration", "test", "selectData", selectData));
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

    public void test3() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.setDataSelectTest();
        LogUtil.loggerLine(Log.of("DataMigration", "test3", "selectData", selectData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    public void test4() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.setDataSelectTestOne();
        LogUtil.loggerLine(Log.of("DataMigration", "test4", "selectData", selectData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    public void test5() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.setDataSelectTestTwo();
        LogUtil.loggerLine(Log.of("DataMigration", "test5", "selectData", selectData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    public void test6() {
        List<Map<String, Object>> selectData = dataIncrementMigrationService.setDataSelectTestThree();
        LogUtil.loggerLine(Log.of("DataMigration", "test6", "selectData", selectData));
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    public static void run(String[] args) {
        new DataMigration().apply();
//        new DataMigration().test();
//        new DataMigration().test1();
//        new DataMigration().test4();
//        new DataMigration().test5();
//        new DataMigration().test6();
    }

}
