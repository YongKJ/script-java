package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MaxComputeAssignmentService extends BaseService {

    private final boolean enable;

    public MaxComputeAssignmentService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeAssignment = GenUtil.getMap("max-compute-assignment");
        this.enable = GenUtil.objToBoolean(maxComputeAssignment.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "apply", "this.preDatabaseMaxCompute.getMapTable().size()", this.preDatabaseMaxCompute.getMapTable().size()));

//        exportTencentAdUser();
//        exportMiniRedBookAdUser();
//        exportBrowsingLink();
//        tencentSceneTest();
//        insertData();
//        getAllData();
//        createTestData();
//        modifyTable();
//        countTableData();
//        testSql();
//        saveData();
//        updateData();
//        syncTableData();
        exportScene();
    }

    private void exportScene() {
        Table table = prodDatabaseHologres.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(table)
                        .isNotNUll("type")
                        .isNotNUll("wx_open_id")
                        .isNotNUll("scene_value")
                        .select("type", "wx_open_id", "scene_value", "options"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        SXSSFSheet localSheet = workbook.createSheet("线下推广场景值");
        SXSSFSheet tencentSheet = workbook.createSheet("广点通场景值");
        SXSSFSheet redBookSheet = workbook.createSheet("小红书场景值");

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("微信小程序标识"),
                Collections.singletonList("场景值"),
                Collections.singletonList("场景值含义")
        );
        PoiExcelUtil.writeHeader(localSheet, lstHeader, lstCellStyle, 1);
        PoiExcelUtil.writeHeader(tencentSheet, lstHeader, lstCellStyle, 1);
        PoiExcelUtil.writeHeader(redBookSheet, lstHeader, lstCellStyle, 1);

        packSheetData(localSheet, lstCellStyle, "local", lstData);
        packSheetData(tencentSheet, lstCellStyle, "tencent", lstData);
        packSheetData(redBookSheet, lstCellStyle, "redBook", lstData);

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\微信小程序场景值-" + System.currentTimeMillis() + ".xlsx");
    }

    private void packSheetData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, String type, List<Map<String, Object>> lstData) {
        int rowIndex = 1;
        Map<String, List<Map<String, Object>>> mapSceneValue = getMapSceneValue(type, lstData);
        List<Map<String, String>> lstScene = CsvUtil.toMap("/csv/小程序场景值.csv");
        for (Map.Entry<String, List<Map<String, Object>>> map : mapSceneValue.entrySet()) {
            for (Map<String, Object> mapData : map.getValue()) {
                String wxOpenId = mapData.get("wx_open_id").toString();
                String sceneValue = mapData.get("scene_value").toString();
                Map<String, String> mapScene = lstScene.stream()
                        .filter(po -> Objects.equals(po.get("scene"), sceneValue)).findFirst().orElse(new HashMap<>());
                String sceneDesc = mapScene.get("desc");
                if (!StringUtils.hasText(sceneDesc)) {
                    continue;
                }

                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, wxOpenId);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 2, sceneValue);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 3, sceneDesc);
                rowIndex++;
                break;
            }
        }
    }

    private Map<String, List<Map<String, Object>>> getMapSceneValue(String type, List<Map<String, Object>> lstData) {
        Map<String, String> mapWxOpenId = getWxOpenId(type, lstData);
        List<Map<String, Object>> lstSceneData = getSceneData(mapWxOpenId, lstData);
        Map<String, List<Map<String, Object>>> mapSceneValue = new HashMap<>();
        for (Map<String, Object> mapData : lstSceneData) {
            String sceneValue = (String) mapData.get("scene_value");
            if (!mapSceneValue.containsKey(sceneValue)) {
                mapSceneValue.put(sceneValue, new ArrayList<>());
            }
            mapSceneValue.get(sceneValue).add(mapData);
        }
        return mapSceneValue;
    }

    private List<Map<String, Object>> getSceneData(Map<String, String> mapWxOpenId, List<Map<String, Object>> lstData) {
        return lstData.stream().filter(po ->
                StringUtils.hasText((String) po.get("scene_value")) &&
                StringUtils.hasText((String) po.get("wx_open_id")) &&
                mapWxOpenId.containsKey((String) po.get("wx_open_id"))
        ).collect(Collectors.toList());
    }

    private Map<String, String> getWxOpenId(String adType, List<Map<String, Object>> lstData) {
        switch (adType) {
            case "tencent":
                return getTencentWxOpenId(lstData);
            case "redBook":
                return getRedBookWxOpenId(lstData);
            case "local":
                return getLocalWxOpenId(lstData);
        }
        return new HashMap<>();
    }

    private Map<String, String> getLocalWxOpenId(List<Map<String, Object>> lstData) {
        Map<String, String> mapTencentWxOpenId = getTencentWxOpenId(lstData);
        Map<String, String> mapRedBookWxOpenId = getRedBookWxOpenId(lstData);
        Map<String, String> mapExcludeWxOpenId = mapTencentWxOpenId.values().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
        mapExcludeWxOpenId.putAll(mapRedBookWxOpenId.values().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity())));

        List<Map<String, Object>> lstLocalData = lstData.stream().filter(po -> Objects.equals(po.get("type"), "1") &&
                !mapExcludeWxOpenId.containsKey((String) po.get("wx_open_id"))).collect(Collectors.toList());
        return lstLocalData.stream()
                .map(po -> (String) po.get("wx_open_id"))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    private Map<String, String> getTencentWxOpenId(List<Map<String, Object>> lstData) {
        List<Map<String, Object>> lstTencentData = lstData.stream()
                .filter(po -> Objects.equals(po.get("type"), "3")).collect(Collectors.toList());
        return lstTencentData.stream()
                .map(po -> (String) po.get("wx_open_id"))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    private Map<String, String> getRedBookWxOpenId(List<Map<String, Object>> lstData) {
        List<Map<String, Object>> lstRedBookData = lstData.stream()
                .filter(po -> Objects.equals(po.get("type"), "1") &&
                        po.get("options") != null && ((String) po.get("options")).contains("click")).collect(Collectors.toList());
        return lstRedBookData.stream()
                .map(po -> (String) po.get("wx_open_id"))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    private void exportMiniRedBookAdUser() {
        Table table = prodDatabaseHologres.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstMiniRedBookData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(table)
                        .eq("type", 1)
                        .eq("event", 6)
                        .like("options", "%click%"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("小红书");
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("wx_open_id"),
                Collections.singletonList("启动事件参数")
        );
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);

        int rowIndex = 1;
        for (Map<String, Object> mapData : lstMiniRedBookData) {
            String wxOpenId = (String) mapData.get("wx_open_id");
            String startOptions = (String) mapData.get("options");

            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, wxOpenId);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, 2, startOptions);
        }

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\小红书小程序推广启动参数-" + System.currentTimeMillis() + ".xlsx");
    }

    private void exportTencentAdUser() {
        Table table = prodDatabaseHologres.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstTencentData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(table)
                        .eq("type", 3)
                        .select("wx_open_id"));

        List<String> lstTencentWxOpenId = lstTencentData.stream()
                .map(po -> (String) po.get("wx_open_id"))
                .filter(StringUtils::hasText)
                .distinct().collect(Collectors.toList());

        List<Map<String, Object>> lstStartData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(table)
                        .eq("type", 1)
                        .eq("event", 6)
                        .in("wx_open_id", lstTencentWxOpenId));

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "exportTencentAdUser", "lstStartData", lstStartData));

        List<Map<String, Object>> lstRegisterData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(table)
                        .eq("type", 1)
                        .eq("event", 7)
                        .in("wx_open_id", lstTencentWxOpenId));

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "exportTencentAdUser", "lstRegisterData", lstRegisterData));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("广点通");
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("wx_open_id"),
                Collections.singletonList("启动事件参数")
        );
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);

        int rowIndex = 1;
        for (Map<String, Object> mapData : lstRegisterData) {
            String wxOpenId = (String) mapData.get("wx_open_id");
            Map<String, Object> mapStartData = lstStartData.stream()
                    .filter(po -> Objects.equals(po.get("wx_open_id"), wxOpenId)).findFirst().orElse(null);
            if (mapStartData == null) {
                continue;
            }

            String startOptions = (String) mapStartData.get("options");
            if (!StringUtils.hasText(startOptions) || startOptions.contains("marketing")) {
                continue;
            }

            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, wxOpenId);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, 2, startOptions);
        }

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\广点通小程序推广启动参数-" + System.currentTimeMillis() + ".xlsx");
    }

    private void exportBrowsingLink() {
        Map<String, Integer> mapPrePageLink = getMapPageLink(preDatabaseHologres);
        Map<String, Integer> mapProdPageLink = getMapPageLink(prodDatabaseHologres);

        Map<String, Integer> mapPageLink = new ConcurrentSkipListMap<>();
        mapPageLink.putAll(mapPrePageLink);
        mapPageLink.putAll(mapProdPageLink);

        for (Map.Entry<String, Integer> map : mapPageLink.entrySet()) {
            Integer preValue = Optional.ofNullable(mapPrePageLink.get(map.getKey())).orElse(0);
            Integer prodValue = Optional.ofNullable(mapProdPageLink.get(map.getKey())).orElse(0);
            mapPageLink.put(map.getKey(), preValue + prodValue);
        }

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "exportBrowsingLink", "mapPrePageLink.size()", mapPrePageLink.size()));
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "exportBrowsingLink", "mapProdPageLink.size()", mapProdPageLink.size()));
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "exportBrowsingLink", "mapPageLink.size()", mapPageLink.size()));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("小程序");
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("页面名称"),
                Collections.singletonList("页面路径"),
                Collections.singletonList("访问次数"),
                Collections.singletonList("分类聚合")
        );
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);

        List<Map.Entry<String, Integer>> lstData = new ArrayList<>(mapPageLink.entrySet());
        lstData.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        int rowIndex = 1;
        for (Map.Entry<String, Integer> map : lstData) {
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, "");
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 2, map.getKey());
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 3, map.getValue());
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, 4, "");
        }

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\顾客浏览路径聚合-" + System.currentTimeMillis() + ".xlsx");
    }

    private Map<String, Integer> getMapPageLink(Database database) {
        Table table = database.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstPageLink = srcDataList(database,
                Wrappers.lambdaQuery(table)
                        .isNotNUll("page_link")
                        .select("page_link"));
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "getMapPageLink", "lstPageLink.size()", lstPageLink.size()));

        Pattern pattern = Pattern.compile("^[a-zA-Z]");
        Map<String, Integer> mapPageLink = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : lstPageLink) {
            String pageLink = (String) mapData.get("page_link");
            if (!StringUtils.hasText(pageLink)) {
                continue;
            }

            if (pageLink.contains("?")) {
                pageLink = pageLink.split("\\?")[0];
            }

            if (!pageLink.contains("/")) {
                continue;
            }

            Matcher matcher = pattern.matcher(pageLink);
            if (!matcher.find()) {
                continue;
            }

            if (!mapPageLink.containsKey(pageLink)) {
                mapPageLink.put(pageLink, 0);
            }

            Integer value = mapPageLink.get(pageLink) + 1;
            mapPageLink.put(pageLink, value);
        }

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "getMapPageLink", "mapPageLink.size()", mapPageLink.size()));

        return mapPageLink;
    }

    private void tencentSceneTest() {
        Table preTable = preDatabaseHologres.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstWxOpenId = srcDataList(preDatabaseHologres,
                Wrappers.lambdaQuery(preTable)
                        .eq("type", 3)
                        .isNotNUll("wx_open_id")
                        .groupBy("wx_open_id")
                        .select("wx_open_id")
        );

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "tencentSceneTest", "lstWxOpenId.size()", lstWxOpenId.size()));
        Map<String, String> mapWxOpenId = lstWxOpenId.stream()
                .filter(po -> !po.get("wx_open_id").toString().isEmpty())
                .map(po -> po.get("wx_open_id").toString())
                .collect(Collectors.toMap(Function.identity(), Function.identity()));

        Table prodTable = prodDatabaseHologres.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstData = srcDataList(prodDatabaseHologres,
                Wrappers.lambdaQuery(prodTable)
                        .eq("event", 6)
                        .isNotNUll("scene_value")
                        .isNotNUll("wx_open_id")
                        .select("wx_open_id", "scene_value")
        );

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "tencentSceneTest", "lstData.size()", lstData.size()));

        Map<String, List<Map<String, Object>>> mapSceneValue = new HashMap<>();
        for (Map<String, Object> mapData : lstData) {
            String wxOpenId = (String) mapData.get("wx_open_id");
            if (!mapWxOpenId.containsKey(wxOpenId)) {
                continue;
            }

            String sceneValue = (String) mapData.get("scene_value");
            if (!mapSceneValue.containsKey(sceneValue)) {
                mapSceneValue.put(sceneValue, new ArrayList<>());
            }
            mapSceneValue.get(sceneValue).add(mapData);
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("广点通场景值");
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);

        List<List<String>> lstHeader = Arrays.asList(
                Collections.singletonList("序号"),
                Collections.singletonList("微信小程序标识"),
                Collections.singletonList("场景值"),
                Collections.singletonList("场景值含义")
        );
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);

        int rowIndex = 1;
        List<Map<String, String>> lstScene = CsvUtil.toMap("/csv/小程序场景值.csv");
        for (Map.Entry<String, List<Map<String, Object>>> map : mapSceneValue.entrySet()) {
            for (Map<String, Object> mapData : map.getValue()) {
                String wxOpenId = mapData.get("wx_open_id").toString();
                String sceneValue = mapData.get("scene_value").toString();
                Map<String, String> mapScene = lstScene.stream()
                        .filter(po -> Objects.equals(po.get("scene"), sceneValue)).findFirst().orElse(new HashMap<>());
                String sceneDesc = mapScene.get("desc");
                if (!StringUtils.hasText(sceneDesc)) {
                    continue;
                }

                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 0, rowIndex);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 1, wxOpenId);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 2, sceneValue);
                PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, 3, sceneDesc);
                rowIndex++;
                break;
            }
        }

        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\微信小程序场景值(广点通)-" + System.currentTimeMillis() + ".xlsx");

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "tencentSceneTest", "mapSceneValue.size()", mapSceneValue.size()));
    }

    private void insertData() {
        Table table = preDatabaseMaxCompute.getMapTable().get("dws_customer_registration_atomic_di");

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("registered_customer_cnt", 891);
        mapData.put("registered_customer_miniprogram_cnt", 594);
        mapData.put("registered_customer_app_cnt", 297);
        mapData.put("login_customer_cnt", 534);
        mapData.put("registered_customer_login_cnt", 534);
        mapData.put("ds", "20240912");

//        String insertSql = getInsertSQl(mapData, table);
//        String insertSql = "INSERT INTO `dws_customer_registration_atomic_di` PARTITION(ds = '20240912') (`registered_customer_cnt`, `login_customer_cnt`, `registered_customer_login_cnt`, `registered_customer_app_cnt`, `registered_customer_miniprogram_cnt`) VALUES (891, 534, 534, 297, 594)";
//        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "insertData", "insertSql", insertSql));
        String removeSql = getRemoveSQl(
                Wrappers.lambdaQuery(table)
                        .eq("ds", "20240912"));
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "insertData", "removeSql", removeSql));

//        boolean result = desDataInsert(preDatabaseMaxCompute, insertSql);
        boolean result = desDataInsert(preDatabaseMaxCompute, removeSql);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "insertData", "result", result));
    }

    private void syncTableData() {
        List<String> tableNames = Arrays.asList(
//                "ods_ocean_engine_advertising",
                "ods_ocean_engine_advertising_report",
//                "ods_tencent_advertising",
                "ods_tencent_advertising_report",
//                "ods_little_advertising",
                "ods_little_advertising_report"
        );

        for (String tableName : tableNames) {
            Table table = preDatabaseMaxCompute.getMapTable().get(tableName);
            List<Map<String, Object>> lstData = srcDataList(preDatabaseMaxCompute, table);
            for (Map<String, Object> mapData : lstData) {
                String insertSql = getInsertSQl(mapData, table);
                LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "syncTableData", "insertSql", insertSql));

                boolean result = JDBCUtil.getResult(prodDatabaseMaxCompute, insertSql);
                LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "result", result));
                System.out.println("----------------------------------------------------------------------------------------------------------");
            }
        }
    }

    private void updateData() {
        Table reportTable = preDatabaseMaxCompute.getMapTable().get("ods_ocean_engine_advertising_report");
        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\table-data.json");
        List<Map<String, Object>> lstData = GenUtil.fromJsonString(content, List.class);
        for (Map<String, Object> mapData : lstData) {
            String updateSql = getUpdateSQl(mapData,
                    Wrappers.lambdaQuery(reportTable)
                            .eq("id", mapData.get("id")));

            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "updateData", "updateSql", updateSql));
            System.out.println("===========================================================================================================");

            desDataUpdate(preDatabaseMaxCompute, updateSql);
        }
    }

    private void saveData() {
        Table reportTable = preDatabaseMaxCompute.getMapTable().get("ods_ocean_engine_advertising_report");
        Map<String, Map<String, Object>> mapReportData = getMapData(srcDataList(preDatabaseMaxCompute, reportTable));
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
        Table testTable = preDatabaseMaxCompute.getMapTable().get("ods_ocean_engine_advertising_report");
        List<Map<String, Object>> lstData = srcSetDataList(preDatabaseMaxCompute,
                Wrappers.lambdaQuery(testTable)
//                        .eq("event", 8)
//                        .in("promotion_id", "1826230234437730306")
                        .select("COUNT(*) `count`"));
//        List<Map<String, Object>> lstData = srcSetDataList(preDatabaseMaxCompute,
//                Wrappers.lambdaQuery(testTable)
//                        .eq("event", 8)
//                        .in("promotion_id", "1826230234437730306", "1826584778565984258")
//                        .groupBy("promotion_id", "event")
//                        .select("promotion_id", "event", "COUNT(*)"));

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "countTableData", "lstData", lstData));
    }

    private void modifyTable() {
//        String sql = "ALTER TABLE `test_odps` ADD COLUMN `data` STRING";
//        String sql = "ALTER TABLE `ods_event_data` ADD COLUMN `status` INT";
//        String sql = "ALTER TABLE `test_odps` MODIFY COLUMN `data` INT";
//        String sql = "ALTER TABLE `test_odps` DROP COLUMN `data`";
//        boolean result = JDBCUtil.getResult(preDatabaseMaxCompute, sql);
//        boolean result = JDBCUtil.getResult(prodDatabaseMaxCompute, sql);
//        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "modifyTable", "result", result));

        Map<String, String> mapField = getMapField();
        for (Map.Entry<String, String> map : mapField.entrySet()) {
            String sql = String.format("ALTER TABLE `ods_event_data` ADD COLUMN `%s` %s", map.getKey(), map.getValue());
            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "modifyTable", "sql", sql));

            boolean result = JDBCUtil.getResult(preDatabaseMaxCompute, sql);
            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "modifyTable", "result", result));
            System.out.println("--------------------------------------------------------------------------------------------------------");
        }
    }

    private Map<String, String> getMapField() {
        Map<String, String> mapField = new HashMap<>();
        mapField.put("imei_md5", "STRING");
        mapField.put("oaid1", "STRING");
        mapField.put("idfa", "STRING");
        mapField.put("idfa1", "STRING");
        mapField.put("android_id", "STRING");
        mapField.put("android_id_md5", "STRING");
        mapField.put("ua1", "STRING");
        mapField.put("readds", "STRING");
        mapField.put("custid1", "STRING");
        mapField.put("timestamp", "BIGINT");
        mapField.put("app_key", "STRING");
        mapField.put("app_name", "STRING");
        mapField.put("unit_id", "BIGINT");
        mapField.put("creativity_id", "BIGINT");
        mapField.put("content", "STRING");
        mapField.put("red_id", "STRING");
        mapField.put("paid", "STRING");
        mapField.put("placement", "INT");
        return mapField;
    }

    private void createTestData() {
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "test-odps.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "ad-store-tt.sql");
        String sqlPath = FileUtil.getAbsPath(false,
                "src", "main", "resources", "sql", "max-compute", "device-record.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "ocean-engine-advertising-report.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "tencent-advertising-report.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "little-advertising-report.sql");
        String sqlStr = FileUtil.read(sqlPath);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "sqlStr", sqlStr));
        System.out.println("==========================================================================================================");

        String[] lstSql = sqlStr.split(";");
        for (String createSql : lstSql) {
            if (!StringUtils.hasText(createSql)) {
                continue;
            }
//            boolean result = JDBCUtil.getResult(preDatabaseMaxCompute, createSql);
            boolean result = JDBCUtil.getResult(prodDatabaseMaxCompute, createSql);

            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "createSql", createSql));
            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "result", result));
            System.out.println("----------------------------------------------------------------------------------------------------------");
        }
    }

    private void getAllData() {
//        Table testTable = preDatabaseMaxCompute.getMapTable().get("ods_test_odps");
//        Table testTable = preDatabaseMaxCompute.getMapTable().get("ods_ad_store_tt");
//        Table testTable = preDatabaseMaxCompute.getMapTable().get("ods_test_odps");
        Table testTable = preDatabaseMaxCompute.getMapTable().get("dws_customer_registration_atomic_di");
        List<Map<String, Object>> lstData = srcDataList(preDatabaseMaxCompute,
                Wrappers.lambdaQuery(testTable)
//                        .eq("cdp_promotion_id", 7350259605072920617L)
//                        .between("stat_time_day", strToTimestamp("2024-08-01"), strToTimestamp("2024-09-02"))
//                        .between("stat_time_day", strToTimestamp("2024-08-01"), strToTimestamp("2024-09-02"))
//                        .in("stat_time_day", strToTimestamp("2024-02-28"), strToTimestamp("2024-02-29"))
//                        .orderByDesc("stat_time_day")
        );

//        lstData = lstData.stream().sorted(
//                        Comparator.comparing(
//                                po -> (Long) ((Map<String, Object>) po).get("stat_time_day")).reversed())
//                .collect(Collectors.toList());

        Collections.reverse(lstData);

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "getAllData", "lstData.size()", lstData.size()));
    }

    private void testSql() {
//        String sql = "show full columns from ods_tencent_advertising_report";
//        String sql = "desc extended ods_tencent_advertising_report";
        String sql = "show create table ods_tencent_advertising_report";
        List<Map<String, Object>> lstData = JDBCUtil.getResultSet(preDatabaseMaxCompute, sql);
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
