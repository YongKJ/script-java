package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaxComputeAssignmentService extends BaseService {

    private final boolean enable;

    public MaxComputeAssignmentService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeAssignment = GenUtil.getMap("max-compute-assignment");
        this.enable = GenUtil.objToBoolean(maxComputeAssignment.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "apply", "this.dataphinChunDevDatabase.getMapTable().size()", this.dataphinChunDevDatabase.getMapTable().size()));

//        getAllData();
//        createTestData();
//        modifyTable();
//        countTableData();
//        testSql();
//        saveData();
        updateData();
    }

    private void updateData() {
        Table reportTable = dataphinChunDevDatabase.getMapTable().get("ods_ocean_engine_advertising_report");
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\table-data.json");
        List<Map<String, Object>> lstData = GenUtil.fromJsonString(content, List.class);
        for (Map<String, Object> mapData : lstData) {
            String updateSql = getUpdateSQl(mapData,
                    Wrappers.lambdaQuery(reportTable)
                            .eq("id", mapData.get("id")));

            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "updateData", "updateSql", updateSql));
            System.out.println("===========================================================================================================");

            desDataUpdate(dataphinChunDevDatabase, updateSql);
        }
    }

    private void saveData() {
        Table reportTable = dataphinChunDevDatabase.getMapTable().get("ods_ocean_engine_advertising_report");
        Map<String, Map<String, Object>> mapReportData = getMapData(srcDataList(dataphinChunDevDatabase, reportTable));
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : mapReportData.entrySet()) {
            Double statCost = (Double) map.getValue().get("stat_cost") * 100;
            Double cpmPlatform = (Double) map.getValue().get("cpm_platform") * 100;
            Double showCost = (Double) map.getValue().get("show_cost") * 100;
            Double cpcPlatform = (Double) map.getValue().get("cpc_platform") * 100;
            Double clickCost = (Double) map.getValue().get("click_cost") * 100;
            Double attributionCustomerEffectiveCost = (Double) map.getValue().get("attribution_customer_effective_cost") * 100;

            map.getValue().put("stat_cost", statCost);
            map.getValue().put("cpm_platform", cpmPlatform);
            map.getValue().put("show_cost", showCost);
            map.getValue().put("cpc_platform", cpcPlatform);
            map.getValue().put("click_cost", clickCost);
            map.getValue().put("attribution_customer_effective_cost", attributionCustomerEffectiveCost);

            lstData.add(map.getValue());
        }

        if (!lstData.isEmpty()) {
            FileUtil.write(
                    "C:\\Users\\Admin\\Desktop\\table-data.json",
                    GenUtil.toJsonString(lstData)
            );
        }
    }

    private void countTableData() {
        Table testTable = dataphinChunDevDatabase.getMapTable().get("ods_ocean_engine_advertising_report");
        List<Map<String, Object>> lstData = srcSetDataList(dataphinChunDevDatabase,
                Wrappers.lambdaQuery(testTable)
//                        .eq("event", 8)
//                        .in("promotion_id", "1826230234437730306")
                        .select("COUNT(*) `count`"));
//        List<Map<String, Object>> lstData = srcSetDataList(dataphinChunDevDatabase,
//                Wrappers.lambdaQuery(testTable)
//                        .eq("event", 8)
//                        .in("promotion_id", "1826230234437730306", "1826584778565984258")
//                        .groupBy("promotion_id", "event")
//                        .select("promotion_id", "event", "COUNT(*)"));

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "countTableData", "lstData", lstData));
    }

    private void modifyTable() {
//        String sql = "ALTER TABLE `test_odps` ADD COLUMN `data` STRING";
        String sql = "ALTER TABLE `ods_event_data` ADD COLUMN `item_id` BIGINT";
//        String sql = "ALTER TABLE `test_odps` MODIFY COLUMN `data` INT";
//        String sql = "ALTER TABLE `test_odps` DROP COLUMN `data`";
        boolean result = JDBCUtil.getResult(dataphinChunDevDatabase, sql);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "modifyTable", "result", result));
    }

    private void createTestData() {
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "test-odps.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "ad-store-tt.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "device-record.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "ocean-engine-advertising-report.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "tencent-advertising-report.sql");
        String sqlPath = FileUtil.getAbsPath(false,
                "src", "main", "resources", "sql", "max-compute", "little-advertising-report.sql");
        String sqlStr = FileUtil.read(sqlPath);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "sqlStr", sqlStr));
        System.out.println("==========================================================================================================");

        String[] lstSql = sqlStr.split(";");
        for (String createSql : lstSql) {
            if (!StringUtils.hasText(createSql)) {
                continue;
            }
            boolean result = JDBCUtil.getResult(dataphinChunDevDatabase, createSql);

            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "createSql", createSql));
            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "result", result));
            System.out.println("----------------------------------------------------------------------------------------------------------");
        }
    }

    private void getAllData() {
//        Table testTable = dataphinChunDevDatabase.getMapTable().get("ods_test_odps");
//        Table testTable = dataphinChunDevDatabase.getMapTable().get("ods_ad_store_tt");
//        Table testTable = dataphinChunDevDatabase.getMapTable().get("ods_test_odps");
        Table testTable = dataphinChunDevDatabase.getMapTable().get("ods_ocean_engine_advertising_report");
        List<Map<String, Object>> lstData = srcDataList(dataphinChunDevDatabase,
                Wrappers.lambdaQuery(testTable)
                        .eq("cdp_promotion_id", 7350259605072920617L)
                        .between("stat_time_day", strToTimestamp("2024-08-01"), strToTimestamp("2024-08-28"))
//                        .in("stat_time_day", strToTimestamp("2024-02-28"), strToTimestamp("2024-02-29"))
                        .orderByDesc("stat_time_day")
        );

//        lstData = lstData.stream().sorted(
//                        Comparator.comparing(
//                                po -> (Long) ((Map<String, Object>) po).get("stat_time_day")).reversed())
//                .collect(Collectors.toList());

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "getAllData", "lstData.size()", lstData.size()));
    }

    private void testSql() {
//        String sql = "show full columns from ods_tencent_advertising_report";
//        String sql = "desc extended ods_tencent_advertising_report";
        String sql = "show create table ods_tencent_advertising_report";
        List<Map<String, Object>> lstData = JDBCUtil.getResultSet(dataphinChunDevDatabase, sql);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "testSql", "lstData.size()", lstData.size()));
    }

    private String localDateToStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    private LocalDate strToLocalDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateStr, formatter);
    }

    private Long strToTimestamp(String dateStr) {
        LocalDate date = strToLocalDate(dateStr);
        ZonedDateTime zonedDateTime = date.atStartOfDay().atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().getEpochSecond();
    }

    public String timestampToStr(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return localDateToStr(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    }
}
