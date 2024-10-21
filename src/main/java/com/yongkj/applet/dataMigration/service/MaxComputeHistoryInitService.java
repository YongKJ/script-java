package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        init_dws_registered_merchant_statistics_di_history_data();
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

        List<String> organizationLogUtcCreated = getUtcCreated(preDatabase, "organization_log");
        LogUtil.loggerLine(Log.of("MaxComputeHistoryInitService", "init_dwd_merchant_review_di_history_data", "organizationLogUtcCreated.size()", organizationLogUtcCreated.size()));

        organizationLogUtcCreated = filterUtcCreated(preDatabaseMaxCompute, "dwd_merchant_review_di", organizationLogUtcCreated);
        for (String utcCreated : organizationLogUtcCreated) {
            String sqlScript = sqlScriptContent.replaceAll("\\$\\{bizdate\\}", utcCreated);

            boolean flag = runMaxComputeTask(preDatabaseMaxCompute, sqlScript, false);

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

    private List<String> getUtcCreated(Database database, String tableName) {
        List<Map<String, Object>> tableData = srcDataList(database,
                Wrappers.lambdaQuery(tableName)
                        .eq("utc_deleted", 0));

        Map<String, String> mapUtcCreatedData = new ConcurrentSkipListMap<>();
        for (Map<String, Object> mapData : tableData) {
            Long utcCreated = (Long) mapData.get("utc_created");
            String utcCreatedStr = GenUtil.timestampToStr(utcCreated);
            if (utcCreatedStr.startsWith("1970")) {
                continue;
            }
            mapUtcCreatedData.put(utcCreatedStr, utcCreatedStr);
        }

        return new ArrayList<>(mapUtcCreatedData.values());
    }

}
