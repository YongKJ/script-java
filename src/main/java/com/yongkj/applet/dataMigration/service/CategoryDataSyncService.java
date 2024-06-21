package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.*;

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
        Database preOrder = mapDatabase.get("pre_order");
        Database prodOrder = mapDatabase.get("prod_order");
        Database devContent = mapDatabase.get("dev_content");
        Database testContent = mapDatabase.get("test_content");
        Database preContent = mapDatabase.get("pre_content");
        Database prodContent = mapDatabase.get("prod_content");

//        syncCategoryData(testOrder, preOrder, "category");
//        syncCategoryData(testOrder, preOrder, "category_show", "pid", "name", "utc_deleted");
//        syncCategoryData(testContent, preContent, "article_category");
//        syncCategoryData(testContent, preContent, "article_category_show", "pid", "name", "utc_deleted");

//        fixCategoryData(preOrder, "category_show");

//        syncCategoryDataByField(devOrder, testOrder, "category", "pid", "name", "apply_id", "status", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(devOrder, testOrder, "category_show", "pid", "name", "first_id", "app_type", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(devContent, testContent, "article_category", "pid", "category_name", "category_type", "status", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(devContent, testContent, "article_category_show", "pid", "name", "category_type", "utc_deleted");

//        syncCategoryDataByField(testOrder, preOrder, "category", "pid", "name", "apply_id", "status", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(testOrder, preOrder, "category_show", "pid", "name", "first_id", "app_type", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(testContent, preContent, "article_category", "pid", "category_name", "category_type", "status", "utc_deleted");
//        System.out.println("===========================================================================================");
//        syncCategoryDataByField(testContent, preContent, "article_category_show", "pid", "name", "category_type", "utc_deleted");

        syncCategoryDataByField(preOrder, prodOrder, "category", "pid", "name", "apply_id", "status", "utc_deleted");
        System.out.println("===========================================================================================");
        syncCategoryDataByField(preOrder, prodOrder, "category_show", "pid", "name", "first_id", "app_type", "utc_deleted");
        System.out.println("===========================================================================================");
        syncCategoryDataByField(preContent, prodContent, "article_category", "pid", "category_name", "category_type", "status", "utc_deleted");
        System.out.println("===========================================================================================");
        syncCategoryDataByField(preContent, prodContent, "article_category_show", "pid", "name", "category_type", "utc_deleted");
    }

    private void fixCategoryData(Database database, String tableName) {
        Table table = database.getMapTable().get(tableName);
        Map<String, Map<String, Object>> mapData = new HashMap<>();
        List<Map<String, Object>> lstTableData = srcDataList(database, table);

        List<Map<String, Object>> deleteTableData = new ArrayList<>();
        for (Map<String, Object> tableData : lstTableData) {
            String key = getMd5Key(tableData, Arrays.asList("pid", "name", "utc_deleted"));
            if (!mapData.containsKey(key)) {
                mapData.put(key, tableData);
                continue;
            }
            Map<String, Object> mapTableData = mapData.get(key);

            String icon = (String) tableData.get("icon");
            String mapIcon = (String) mapTableData.get("icon");
            if (icon.contains("env_test")) {
                deleteTableData.add(tableData);
            }
            if (mapIcon.contains("env_test")) {
                deleteTableData.add(mapTableData);
            }
        }

        for (Map<String, Object> data : deleteTableData) {

            Long id = (Long) data.get("id");
            String deleteSql = getRemoveSQl(
                    Wrappers.lambdaQuery(table.getName())
                            .eq("id", id));

            System.out.println("---------[CategoryDataSyncService] fixCategoryData -> tableData: " + data);
            System.out.println("---------[CategoryDataSyncService] fixCategoryData -> deleteSql: " + deleteSql);

//            srcDataRemove(database, deleteSql);
        }
    }

    private void syncCategoryDataByField(Database srcDatabase, Database desDatabase, String tableName, String... lstField) {
        LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncCategoryDataByField", "tableName", tableName));
        System.out.println("==================================================================================================================");

        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        List<Map<String, Object>> srcTableData = srcDataList(srcDatabase, srcTable);
        Map<String, Map<String, Object>> desMapTableDataByKey = getMapData(desDataList(desDatabase, desTable));
        Map<String, Map<String, Object>> desMapTableDataByField = getMapData(desDataList(desDatabase, desTable), Arrays.asList(lstField));

        List<String> lstInsertSql = new ArrayList<>();
        List<String> lstUpdateSql = new ArrayList<>();
        for (Map<String, Object> tableData : srcTableData) {

            Integer status = (Integer) tableData.get("status");
            Long utcDeleted = (Long) tableData.get("utc_deleted");
            if (utcDeleted != 0 || !tableName.contains("show") && status != 2) {
                continue;
            }

            String key = getMd5Key(tableData, Arrays.asList(lstField));
            if (desMapTableDataByField.containsKey(key)) {
                continue;
            }

            Long id = (Long) tableData.get("id");
            if (!desMapTableDataByKey.containsKey(id + "")) {
                String insertSql = getInsertSQl(tableData, desTable);
                lstInsertSql.add(insertSql);
                continue;
            }

            if (!tableName.contains("show") && id > 1000000) {
                continue;
            }
            String updateSql = getUpdateSQl(tableData,
                    Wrappers.lambdaQuery(desTable)
                            .eq("id", id));

            lstUpdateSql.add(updateSql);

        }

        List<String> lstSql = new ArrayList<>();
        for (String insertSql : lstInsertSql) {
            LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncCategoryDataByField", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            lstSql.add(insertSql);
//            desDataInsert(desDatabase, insertSql);
        }

        for (String updateSql : lstUpdateSql) {
            LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncCategoryDataByField", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            lstSql.add(updateSql);
//            desDataUpdate(desDatabase, updateSql);
        }

        System.out.println("==================================================================================================================\n\n");

//        FileUtil.write(
//                "C:\\Users\\Admin\\Desktop\\category-sql.sql",
//                String.join("\n", lstSql)
//        );
    }

    private void syncCategoryData(Database srcDatabase, Database desDatabase, String tableName, String... lstField) {
        LogUtil.loggerLine(Log.of("CategoryDataSyncService", "syncOrderCategoryData", "tableName", tableName));
        System.out.println("==================================================================================================================");

        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        Map<String, Map<String, Object>> srcMapTableData = getMapData(srcDataList(srcDatabase, srcTable), Arrays.asList(lstField));
        Map<String, Map<String, Object>> desMapTableData = getMapData(desDataList(desDatabase, desTable), Arrays.asList(lstField));

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
