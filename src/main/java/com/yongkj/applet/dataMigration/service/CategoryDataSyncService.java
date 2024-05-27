package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CategoryDataSyncService extends BaseService {

    private final boolean enable;

    public CategoryDataSyncService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> smallAssignmentUpdate = GenUtil.getMap("category-data-sync");
        this.enable = GenUtil.objToBoolean(smallAssignmentUpdate.get("enable"));
    }

    public void apply() {
        if (!enable) return;

        Database devOrder = mapDatabase.get("dev_order");
        Database testOrder = mapDatabase.get("test_order");
        Database devContent = mapDatabase.get("dev_content");
        Database testContent = mapDatabase.get("test_content");

//        syncCategoryData(testOrder, devOrder, "category");
        syncCategoryData(testOrder, devOrder, "category_show");
//        syncCategoryData(testContent, devContent, "article_category_show");
//        syncCategoryData(testContent, devContent, "article_category_show");
    }

    private void syncCategoryData(Database srcDatabase, Database desDatabase, String tableName) {
        LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "tableName", tableName));
        System.out.println("==================================================================================================================");

        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        Map<String, Map<String, Object>> srcMapTableData = getMapData(srcDataList(srcDatabase, srcTable));
        Map<String, Map<String, Object>> desMapTableData = getMapData(desDataList(desDatabase, desTable));

        List<String> lstInsertSql = new ArrayList<>();
        List<String> lstUpdateSql = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : srcMapTableData.entrySet()) {

            Integer status = (Integer) map.getValue().get("status");
            Long utcDeleted = (Long) map.getValue().get("utc_deleted");
            if (utcDeleted != 0 || !tableName.contains("show") && status != 2) {
                continue;
            }

            if (!desMapTableData.containsKey(map.getKey())) {
                String insertSql = getInsertSQl(map.getValue(), desTable);

//                LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "insertSql", insertSql));
//                System.out.println("------------------------------------------------------------------------------------------------------------------");

                lstInsertSql.add(insertSql);
//                desDataInsert(desDatabase, insertSql);
                continue;
            }

            Long id = (Long) map.getValue().get("id");
            if (!tableName.contains("show") && id > 1000000) {
                continue;
            }

            String srcName = (String) map.getValue().get(map.getValue().containsKey("name") ? "name" : "category_name");
            String desName = (String) desMapTableData.get(map.getKey()).get(map.getValue().containsKey("name") ? "name" : "category_name");
            if (Objects.equals(srcName, desName)) {
                continue;
            }

            String updateSql = getUpdateSQl(map.getValue(),
                    Wrappers.lambdaQuery(desTable)
                            .eq("id", map.getValue().get("id")));

//            LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "updateSql", updateSql));
//            System.out.println("------------------------------------------------------------------------------------------------------------------");
            lstUpdateSql.add(updateSql);
//            desDataUpdate(desDatabase, updateSql);
        }

        for (String insertSql : lstInsertSql) {
            LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

//            desDataInsert(desDatabase, insertSql);
        }

        for (String updateSql : lstUpdateSql) {
            LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

//            desDataUpdate(desDatabase, updateSql);
        }

        System.out.println("==================================================================================================================\n\n");
    }

}
