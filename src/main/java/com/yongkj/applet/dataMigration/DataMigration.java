package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.service.*;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMigration {

    private String bizDate;
    private final Database srcDatabase;
    private final Database desDatabase;
    private final Database devDatabase;
    private final Database preDatabase;
    private final Database testDatabase;
    private final Database prodDatabase;
    private final Database preDatabaseHologres;
    private final Database prodDatabaseHologres;
    private final Database preDatabaseMaxCompute;
    private final Database prodDatabaseMaxCompute;
    private final Map<String, Database> mapDatabase;
    private final TokenManagementService tokenManagementService;
    private final CategoryDataSyncService categoryDataSyncService;
    private final AgreementsUpdateService agreementsUpdateService;
    private final ShopCancelLogoutService shopCancelLogoutService;
    private final MaxComputeCloudInitService maxComputeCloudInitService;
    private final SyncMenuPermissionsService syncMenuPermissionsService;
    private final MaxComputeAssignmentService maxComputeAssignmentService;
    private final ShopWorkerDataExportService shopWorkerDataExportService;
    private final SmallAssignmentUpdateService smallAssignmentUpdateService;
    private final MaxComputeHistoryInitService maxComputeHistoryInitService;
    private final AdminMenuDataMigrationService adminMenuDataMigrationService;
    private final DataIncrementMigrationService dataIncrementMigrationService;
    private final FieldIncrementMigrationService fieldIncrementMigrationService;
    private final MaxComputeStatisticsInitService maxComputeStatisticsInitService;

    private DataMigration() {
        Map<String, Object> mapMaxComputeHistoryInit = GenUtil.getMap("max-compute-history-init");
        boolean historyInit = GenUtil.objToBoolean(mapMaxComputeHistoryInit.get("enable"));

        Map<String, Object> mapMaxComputeCloudInit = GenUtil.getMap("max-compute-cloud-init");
        boolean cloudInit = GenUtil.objToBoolean(mapMaxComputeCloudInit.get("enable"));

        if (historyInit || cloudInit) {
            this.preDatabaseHologres = null;
            this.prodDatabaseHologres = null;
            this.mapDatabase = Database.initMaxComputeMapDatabase();
            this.preDatabaseMaxCompute = Database.get("pre_warehouse_max_compute", mapDatabase);
            this.prodDatabaseMaxCompute = Database.get("prod_warehouse_max_compute", mapDatabase);

            this.devDatabase = null;
            this.testDatabase = null;
            this.srcDatabase = null;
            this.desDatabase = null;
            Map<String, Database> mapDatabase = Database.initMapDatabase();
            this.preDatabase = Database.get("pre", mapDatabase);
            this.prodDatabase = Database.get("prod", mapDatabase);
        } else {
            Map<String, Object> mapMaxCompute = GenUtil.getMap("max-compute-assignment");
            boolean maxCompute = GenUtil.objToBoolean(mapMaxCompute.get("enable"));

            if (!maxCompute) {
                Map<String, Object> mapMaxComputeStatisticsInit = GenUtil.getMap("max-compute-statistics-init");
                maxCompute = GenUtil.objToBoolean(mapMaxComputeStatisticsInit.get("enable"));
            }

            if (maxCompute) {
                this.devDatabase = null;
                this.preDatabase = null;
                this.testDatabase = null;
                this.prodDatabase = null;
                this.srcDatabase = null;
                this.desDatabase = null;
                this.mapDatabase = Database.initMaxComputeMapDatabase();
                this.preDatabaseHologres = Database.get("pre_warehouse", mapDatabase);
                this.prodDatabaseHologres = Database.get("prod_warehouse", mapDatabase);
                this.preDatabaseMaxCompute = Database.get("pre_warehouse_max_compute", mapDatabase);
                this.prodDatabaseMaxCompute = Database.get("prod_warehouse_max_compute", mapDatabase);
            } else {
                this.preDatabaseHologres = null;
                this.prodDatabaseHologres = null;
                this.preDatabaseMaxCompute = null;
                this.prodDatabaseMaxCompute = null;
                this.mapDatabase = Database.initMapDatabase();
                this.devDatabase = Database.get("dev", mapDatabase);
                this.preDatabase = Database.get("pre", mapDatabase);
                this.testDatabase = Database.get("test", mapDatabase);
                this.prodDatabase = Database.get("prod", mapDatabase);
                this.srcDatabase = Database.get("src", this);
                this.desDatabase = Database.get("des", this);
            }
        }

        this.tokenManagementService = new TokenManagementService(this);
        this.adminMenuDataMigrationService = new AdminMenuDataMigrationService(this);
        this.categoryDataSyncService = new CategoryDataSyncService(this);
        this.agreementsUpdateService = new AgreementsUpdateService(this);
        this.shopCancelLogoutService = new ShopCancelLogoutService(this);
        this.maxComputeCloudInitService = new MaxComputeCloudInitService(this);
        this.syncMenuPermissionsService = new SyncMenuPermissionsService(this);
        this.maxComputeAssignmentService = new MaxComputeAssignmentService(this);
        this.shopWorkerDataExportService = new ShopWorkerDataExportService(this);
        this.maxComputeHistoryInitService = new MaxComputeHistoryInitService(this);
        this.smallAssignmentUpdateService = new SmallAssignmentUpdateService(this);
        this.dataIncrementMigrationService = new DataIncrementMigrationService(this);
        this.fieldIncrementMigrationService = new FieldIncrementMigrationService(this);
        this.maxComputeStatisticsInitService = new MaxComputeStatisticsInitService(this);
    }

    private void apply() {
        List<String> srcDatabases = srcDatabase == null ? new ArrayList<>() : Database.getDatabases(srcDatabase.getManager());
        List<String> desDatabases = desDatabase == null ? new ArrayList<>() : Database.getDatabases(desDatabase.getManager());
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases", srcDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases.size()", srcDatabases.size()));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases", desDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases.size()", desDatabases.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        srcDatabases = srcDatabase == null ? new ArrayList<>() : Database.getDatabasesBySql(srcDatabase.getManager());
        desDatabases = desDatabase == null ? new ArrayList<>() : Database.getDatabasesBySql(desDatabase.getManager());
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases", srcDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases.size()", srcDatabases.size()));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases", desDatabases));
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "desDatabases.size()", desDatabases.size()));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        if (srcDatabase != null && !srcDatabases.contains(srcDatabase.getName()) ||
                desDatabase != null && !desDatabases.contains(desDatabase.getName())) {
            System.out.println("数据库不存在！");
            return;
        }

        tokenManagementService.apply();
        categoryDataSyncService.apply();
        agreementsUpdateService.apply();
        shopCancelLogoutService.apply();
        maxComputeCloudInitService.apply();
        syncMenuPermissionsService.apply();
        maxComputeAssignmentService.apply();
        shopWorkerDataExportService.apply();
        smallAssignmentUpdateService.apply();
        maxComputeHistoryInitService.apply();
        adminMenuDataMigrationService.apply();
        dataIncrementMigrationService.apply();
        fieldIncrementMigrationService.apply();
        maxComputeStatisticsInitService.apply();
        for (Map.Entry<String, Database> map : mapDatabase.entrySet()) {
            JDBCUtil.closeAll(map.getValue().getManager());
        }
    }

    private String applyCloud(String bizDate) {
        this.bizDate = bizDate;
        return maxComputeCloudInitService.apply();
    }

    public String getBizDate() {
        return bizDate;
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

    public Database getPreDatabaseHologres() {
        return preDatabaseHologres;
    }

    public Database getProdDatabaseHologres() {
        return prodDatabaseHologres;
    }

    public Database getPreDatabaseMaxCompute() {
        return preDatabaseMaxCompute;
    }

    public Database getProdDatabaseMaxCompute() {
        return prodDatabaseMaxCompute;
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

    public static String run(String[] args) {
        if (args.length == 0) {
            new DataMigration().apply();
            return null;
        } else {
            return new DataMigration().applyCloud(args[0]);
        }
//        new DataMigration().test();
//        new DataMigration().test1();
//        new DataMigration().test4();
//        new DataMigration().test5();
//        new DataMigration().test6();
    }

}
