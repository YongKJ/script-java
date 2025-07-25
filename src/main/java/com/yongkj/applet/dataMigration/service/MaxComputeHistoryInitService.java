package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaxComputeHistoryInitService extends BaseService {

    private final boolean enable;

    public MaxComputeHistoryInitService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeHistory = GenUtil.getMap("max-compute-history-init");
        this.enable = GenUtil.objToBoolean(maxComputeHistory.get("enable"));
    }

    public void apply() {
        if (!enable) return;

//        init_dwd_merchant_review_di_history_data();
//        migrate_dwd_merchant_review_di_history_data();
//        init_dws_registered_merchant_statistics_di_history_data();
//        init_dwd_customer_browse_path_di_history_data();
//        init_ods_browse_path_info_history_data();
//        init_dws_customer_registration_atomic_di();
//        init_dws_customer_registration_atomic_di_prod();
//        init_dws_customer_registration_statistics_di();
//        init_dws_customer_registration_statistics_di_prod();
//        init_dws_customer_login_atomic_di();
//        init_dws_customer_login_statistics_di();
//        init_dwd_customer_evaluate_di();
//        init_dws_customer_evaluate_statistics_di();
//        init_insert_ads_overall_customer_data_trends_di();
        init_hologres_data_to_max_compute_data();
//        count_hologres_data_to_max_compute_data();
    }

    private void count_hologres_data_to_max_compute_data() {
        List<String> tableNames = Arrays.asList(
                "dws_business_home_sales_category_di",
                "dwd_active_customer_screen_di",
                "dwd_worker_orbit_detail_di",
                "dwd_worker_orbit_job_di",
                "dws_account_creator_statistics_screen_di",
                "dws_business_home_dashboard_di",
                "dws_content_content_summary_screen_di",
                "dws_content_visit_summary_screen_di",
                "dws_customer_screen_di",
                "dws_marketing_screen_di",
                "dws_worker_orbit_di",
                "ods_little_advertising",
                "ods_little_advertising_report",
                "ods_merchant_device_user_info",
                "ods_merchant_event_data",
                "ods_ocean_engine_advertising",
                "ods_ocean_engine_advertising_report",
                "ods_tencent_advertising",
                "ods_tencent_advertising_report",
                "ods_worker_device_user_info",
                "ods_worker_event_data"
        );

        for (String tableName : tableNames) {
            Table table = prodDatabaseHologres.getMapTable().get(tableName);
            List<Map<String, Object>> lstHologresData = srcDataList(prodDatabaseHologres, table);

            String tableNameLatest = tableName + "_latest";
            Table tableLatest = preDatabaseMaxCompute.getMapTable().get(tableNameLatest);
            List<Map<String, Object>> lstMaxComputeData = srcDataList(preDatabaseMaxCompute, tableLatest);
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_hologres_data_to_max_compute_data", "lstMaxComputeData.size()", lstMaxComputeData.size()));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_hologres_data_to_max_compute_data", "lstHologresData.size()", lstHologresData.size()));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_hologres_data_to_max_compute_data", "tableName", tableName));
            System.out.println("================================================================================================================================");
        }
    }

    private void init_hologres_data_to_max_compute_data() {
        List<String> tableNames = Arrays.asList(
                "dws_business_home_sales_category_di",
                "dwd_active_customer_screen_di",
                "dwd_worker_orbit_detail_di",
                "dwd_worker_orbit_job_di",
                "dws_account_creator_statistics_screen_di",
                "dws_business_home_dashboard_di",
                "dws_content_content_summary_screen_di",
                "dws_content_visit_summary_screen_di",
                "dws_customer_screen_di",
                "dws_marketing_screen_di",
                "dws_worker_orbit_di",
                "ods_little_advertising",
                "ods_little_advertising_report",
                "ods_merchant_device_user_info",
                "ods_merchant_event_data",
                "ods_ocean_engine_advertising",
                "ods_ocean_engine_advertising_report",
                "ods_tencent_advertising",
                "ods_tencent_advertising_report",
                "ods_worker_device_user_info",
                "ods_worker_event_data"
        );

        for (String tableName : tableNames) {
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_hologres_data_to_max_compute_data", "tableName", tableName));
            Table table = prodDatabaseHologres.getMapTable().get(tableName);
            List<Map<String, Object>> lstData = srcDataList(prodDatabaseHologres, table);
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_hologres_data_to_max_compute_data", "lstData.size()", lstData.size()));
            System.out.println("================================================================================================================================");

            String tableNameLatest = tableName + "_latest";
            Table tableLatest = prodDatabaseMaxCompute.getMapTable().get(tableNameLatest);
            init_hologres_data_to_max_compute_data(lstData, prodDatabaseMaxCompute, tableLatest);
        }
    }

    private void init_hologres_data_to_max_compute_data(List<Map<String, Object>> lstData, Database database, Table table) {
        for (int i = 0; i < lstData.size(); i += 1000) {
            int size = Math.min(i + 1000, lstData.size());
            List<Map<String, Object>> tempLstData = lstData.subList(i, size);
            String insertSql = getMaxComputeInsertListSQlNonePartition(tempLstData, table);
            srcDataInsert(database, insertSql);
        }
    }

    private void init_insert_ads_overall_customer_data_trends_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\evaluate\\insert_ads_overall_customer_data_trends_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_insert_ads_overall_customer_data_trends_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_insert_ads_overall_customer_data_trends_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_evaluate_statistics_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\evaluate\\insert_dws_customer_evaluate_statistics_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_evaluate_statistics_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_evaluate_statistics_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dwd_customer_evaluate_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\evaluate\\insert_dwd_customer_evaluate_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_customer_evaluate_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_customer_evaluate_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_login_statistics_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\login\\insert_dws_customer_login_statistics_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dwd_customer_login_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\login\\insert_dwd_customer_login_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_login_atomic_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\login\\insert_dws_customer_login_atomic_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_registration_statistics_di_prod() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\registration\\insert_dws_customer_registration_statistics_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\utc-created-data.json");
        Map<String, String> mapCreateTime = GenUtil.fromJsonString(content, Map.class);
        for (Map.Entry<String, String> map : mapCreateTime.entrySet()) {
            String createTime = map.getKey();
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(prodDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_statistics_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_statistics_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_registration_statistics_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\registration\\insert_dws_customer_registration_statistics_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_registration_atomic_di() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\registration\\insert_dws_customer_registration_atomic_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> createTimes = getCreateTime();
        for (String createTime : createTimes) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_customer_registration_atomic_di_prod() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\registration\\insert_dws_customer_registration_atomic_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        String content = FileUtil.read("C:\\Users\\Admin\\Desktop\\utc-created-data.json");
        Map<String, String> mapCreateTime = GenUtil.fromJsonString(content, Map.class);
        for (Map.Entry<String, String> map : mapCreateTime.entrySet()) {
            String createTime = map.getKey();
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", createTime);

            boolean flag = runMaxComputeTask(prodDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "createTime", createTime));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private List<String> getCreateTime() {
        Map<String, Object> mapFilter = new HashMap<>();
        mapFilter.put("event", 7);
        List<String> eventDataCreateTime = getUtcCreated(preDatabaseHologres, "ods_event_data", "create_time", mapFilter);

        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "eventDataCreateTime.size()", eventDataCreateTime.size()));
        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_customer_registration_atomic_di", "eventDataCreateTime", eventDataCreateTime));
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");

        LocalDate endDate = LocalDate.now();
        List<String> createTimes = new ArrayList<>();
        LocalDate startDate = GenUtil.strToLocalDate(eventDataCreateTime.get(0), "yyyyMMdd");
//        LocalDate startDate = GenUtil.strToLocalDate("20241024", "yyyyMMdd");
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            String dateStr = GenUtil.localDateToStr(date, "yyyyMMdd");
            createTimes.add(dateStr);
        }
        return createTimes;
    }

    private void migrate_dwd_merchant_review_di_history_data() {
        Table srcTable = preDatabaseMaxCompute.getMapTable().get("dwd_merchant_review_di");
        Table desTable = prodDatabaseMaxCompute.getMapTable().get("dwd_merchant_review_di");
        List<Map<String, Object>> tableData = srcDataList(preDatabaseMaxCompute, srcTable);
        for (Map<String, Object> mapData : tableData) {
            String insertSql = getMaxComputeInsertSQl(mapData, desTable);
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "migrate_dwd_merchant_review_di_history_data", "insertSql", insertSql));
            srcDataInsert(prodDatabaseMaxCompute, insertSql);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
        }
    }

    private void init_ods_browse_path_info_history_data() {
        Map<String, Map<String, String>> mapBrowsePathGroup = CsvUtil.toMap("/csv/max-compute/statistics/browse-path-group.csv")
                .stream().collect(Collectors.toMap(po -> po.get("id"), Function.identity()));
//        List<Map<String, String>> lstBrowsePathInfo = PoiExcelUtil.toMap("C:\\Users\\Admin\\Desktop\\顾客浏览路径聚合-1729481289990(1).xlsx");
        List<Map<String, String>> lstBrowsePathInfo = PoiExcelUtil.toMap("C:\\Users\\Admin\\Desktop\\顾客浏览路径聚合-1729481289990(1)-latest.xlsx");
        List<Map<String, Object>> tableData = srcDataList(prodDatabaseMaxCompute, Wrappers.lambdaQuery("ods_browse_path_info"));
        Table table = prodDatabaseMaxCompute.getMapTable().get("ods_browse_path_info");
        for (Map<String, String> mapBrowsePathInfo : lstBrowsePathInfo) {
            String pageLink = mapBrowsePathInfo.get("页面路径");
            String pageName = mapBrowsePathInfo.get("页面名称");
            String groupId = mapBrowsePathInfo.get("分类聚合");
            if (!mapBrowsePathGroup.containsKey(groupId)) {
                continue;
            }
            List<Map<String, Object>> tempTableData = tableData.stream().filter(po -> Objects.equals(po.get("page_link"), pageLink)).collect(Collectors.toList());
            if (!tempTableData.isEmpty()) {
                continue;
            }

            String pageGroupName = mapBrowsePathGroup.get(groupId).get("name");
            String pageGroupLink = mapBrowsePathGroup.get(groupId).get("link");
            String ds = GenUtil.timestampToStr(System.currentTimeMillis() / 1000);

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("ds", ds);
            mapData.put("page_link", pageLink);
            mapData.put("page_name", pageName);
            mapData.put("page_group_link", pageGroupLink);
            mapData.put("page_group_name", pageGroupName);

            String insertSql = getMaxComputeInsertSQl(mapData, table);
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_ods_browse_path_info_history_data", "insertSql", insertSql));
            srcDataInsert(prodDatabaseMaxCompute, insertSql);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
        }
    }

    private void init_dwd_customer_browse_path_di_history_data() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\customer\\browse\\insert_dwd_customer_browse_path_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> userSessionUtcCreated = getUtcCreatedByMaxCompute(preDatabaseMaxCompute, "dwd_con_user_session_path_di");
        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_customer_browse_path_di_history_data", "userSessionUtcCreated.size()", userSessionUtcCreated.size()));
        for (String utcCreated : userSessionUtcCreated) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", utcCreated);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_customer_browse_path_di_history_data", "utcCreated", utcCreated));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_customer_browse_path_di_history_data", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dws_registered_merchant_statistics_di_history_data() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\merchant\\registration\\insert_dws_registered_merchant_statistics_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> organizationUtcCreated = getUtcCreated(preDatabase, "organization");
        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_registered_merchant_statistics_di_history_data", "organizationUtcCreated.size()", organizationUtcCreated.size()));

        organizationUtcCreated = filterUtcCreated(preDatabaseMaxCompute, "dws_registered_merchant_statistics_di", organizationUtcCreated);
        for (String utcCreated : organizationUtcCreated) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", utcCreated);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, true);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_registered_merchant_statistics_di_history_data", "utcCreated", utcCreated));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dws_registered_merchant_statistics_di_history_data", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private void init_dwd_merchant_review_di_history_data() {
        String sqlScriptPath = "D:\\Document\\MyCodes\\Worker\\MaxCompute\\service-warehouse\\scripts\\merchant\\registration\\insert_dwd_merchant_review_di.osql";
        String sqlScriptContent = FileUtil.read(sqlScriptPath);

        List<String> organizationLogUtcCreated = getUtcCreated(prodDatabase, "organization_log");
        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_merchant_review_di_history_data", "organizationLogUtcCreated.size()", organizationLogUtcCreated.size()));

        organizationLogUtcCreated = filterUtcCreated(prodDatabaseMaxCompute, "dwd_merchant_review_di", organizationLogUtcCreated);
        for (String utcCreated : organizationLogUtcCreated) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", utcCreated);

            boolean flag = runMaxComputeTask(prodDatabaseMaxCompute, sqlScript, false);

            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_merchant_review_di_history_data", "utcCreated", utcCreated));
            LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_merchant_review_di_history_data", "flag", flag));
            System.out.println("================================================================================================================================");

            if (!flag) {
                break;
            }
        }
    }

    private List<String> filterUtcCreated(Database database, String tableName, List<String> lstUtcCreated) {
        Table table = database.getMapTable().get(tableName);
        List<Map<String, Object>> tableData = srcDataList(database, table);
        Map<String, String> mapUtcCreatedData = tableData.stream().map(po -> (String) po.get("ds"))
                .collect(Collectors.toMap(Function.identity(), Function.identity()));

        List<String> lstUtcCreatedData = new ArrayList<>();
        for (String utcCreated : lstUtcCreated) {
            if (mapUtcCreatedData.containsKey(utcCreated)) {
                continue;
            }

            lstUtcCreatedData.add(utcCreated);
        }

        return lstUtcCreatedData;
    }

    private List<String> getUtcCreatedByMaxCompute(Database database, String tableName) {
        List<Map<String, Object>> tableData = srcDataList(database, Wrappers.lambdaQuery(tableName));

        Map<String, String> mapUtcCreatedData = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : tableData) {
            Long utcCreated = (Long) mapData.get("create_time");
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated / 1000);
            if (utcCreatedStr.startsWith("1970")) {
                continue;
            }
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        return new ArrayList<>(mapUtcCreatedData.values());
    }

    private List<String> getUtcCreated(Database database, String tableName) {
        Map<String, Object> mapFilter = new HashMap<>();
        mapFilter.put("utc_deleted", 0);
        return getUtcCreated(database, tableName, "utc_created", mapFilter);
    }

    private List<String> getUtcCreated(Database database, String tableName, String fieldName, Map<String, Object> mapFilter) {
        Wrappers wrappers = Wrappers.lambdaQuery(tableName);
        for (Map.Entry<String, Object> map : mapFilter.entrySet()) {
            wrappers.eq(map.getKey(), map.getValue());
        }
        wrappers.select(fieldName);
        List<Map<String, Object>> tableData = srcDataList(database, wrappers);

        Map<String, String> mapUtcCreatedData = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : tableData) {
            String fieldValue = mapData.get(fieldName).toString();
            Long utcCreated = getUtcCreated(Long.parseLong(fieldValue));
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated);
            if (utcCreatedStr.startsWith("1970")) {
                continue;
            }
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        return new ArrayList<>(mapUtcCreatedData.values());
    }

    private Long getUtcCreated(Long utcCreated) {
        Long currentTimestamp = System.currentTimeMillis();
        if (utcCreated.toString().length() != currentTimestamp.toString().length()) {
            return utcCreated;
        }

        return utcCreated / 1000;
    }

}
