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

public class SyncMenuPermissionsService extends BaseService {

    private final boolean enable;

    public SyncMenuPermissionsService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> smallAssignmentUpdate = GenUtil.getMap("sync-menu-permissions");
        this.enable = GenUtil.objToBoolean(smallAssignmentUpdate.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        diffRoleMenuData();
    }

    private void diffRoleMenuData() {
        Database devUser = mapDatabase.get("dev_user");
        Database testUser = mapDatabase.get("test_user");
        Database preUser = mapDatabase.get("pre_user");
        Database prodUser = mapDatabase.get("prod_user");

//        distinctRoleMenuData(devUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(devUser, "admin_role_menu", "role_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(devUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(devUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");

//        distinctRoleMenuData(testUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(testUser, "admin_role_menu", "role_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(testUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(testUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");

        distinctRoleMenuData(preUser, "admin_menu", "pid", "name", "alias_name", "desc", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
        System.out.println("==================================================================================================================\n");
        distinctRoleMenuData(preUser, "admin_role_menu", "role_id", "menu_id");
        System.out.println("==================================================================================================================\n");
        distinctRoleMenuData(preUser, "admin_apply_menu", "apply_id", "menu_id");
        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(preUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");

//        distinctRoleMenuData(prodUser, "admin_menu", "pid", "name", "alias_name", "type", "sort", "apply_kind", "platform_kind", "apply_id", "route", "route_param");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(prodUser, "admin_role_menu", "role_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(prodUser, "admin_apply_menu", "apply_id", "menu_id");
//        System.out.println("==================================================================================================================\n");
//        distinctRoleMenuData(prodUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");

//        diffRoleMenuData(devUser, testUser, "admin_menu", "id");
//        diffRoleMenuData(devUser, testUser, "admin_apply", "id");
//        diffRoleMenuData(devUser, testUser, "admin_role_menu", "role_id", "menu_id");
//        diffRoleMenuData(devUser, testUser, "admin_apply_menu", "apply_id", "menu_id");
//        diffRoleMenuData(devUser, testUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");

        diffRoleMenuData(testUser, preUser, "admin_menu", "id");
        diffRoleMenuData(testUser, preUser, "admin_apply", "id");
        diffRoleMenuData(testUser, preUser, "admin_role_menu", "role_id", "menu_id");
        diffRoleMenuData(testUser, preUser, "admin_apply_menu", "apply_id", "menu_id");
//        diffRoleMenuData(testUser, preUser, "admin_apply_organization", "apply_id", "organization_id");
        System.out.println("==================================================================================================================\n");

//        diffRoleMenuData(preUser, prodUser, "admin_menu", "id");
//        diffRoleMenuData(preUser, prodUser, "admin_apply", "id");
//        diffRoleMenuData(preUser, prodUser, "admin_role_menu", "role_id", "menu_id");
//        diffRoleMenuData(preUser, prodUser, "admin_apply_menu", "apply_id", "menu_id");
//        diffRoleMenuData(preUser, prodUser, "admin_apply_organization", "apply_id", "organization_id");
//        System.out.println("==================================================================================================================\n");
    }

    private void distinctRoleMenuData(Database database, String desTableName, String... fields) {
        Table desTable = database.getMapTable().get(desTableName);
        List<Map<String, Object>> lstData = desDataList(database, desTable);
        Map<String, Map<String, Object>> mapData = new HashMap<>();
        for (Map<String, Object> data : lstData) {
            String key = getMd5Key(data, Arrays.asList(fields));
            if (!mapData.containsKey(key)) {
                mapData.put(key, data);
                continue;
            }

            String deleteSql = getRemoveSQl(data, desTableName);
            if (Objects.equals(desTableName, "admin_apply_organization")) {
                Map<String, Object> tempMapData = getMapData(data, Arrays.asList(fields));
                deleteSql = getRemoveSQl(tempMapData, desTableName);
            }
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "distinctRoleMenuData", "deleteSql", deleteSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataRemove(database, deleteSql);
        }
    }

    private Map<String, Object> getMapData(Map<String, Object> mapData, List<String> fields) {
        Map<String, Object> tempMapData = new HashMap<>();
        for (Map.Entry<String, Object> map : mapData.entrySet()) {
            if (!fields.contains(map.getKey())) {
                continue;
            }

            tempMapData.put(map.getKey(), map.getValue());
        }
        return tempMapData;
    }

    private void diffRoleMenuData(Database srcDatabase, Database desDatabase, String tableName, String... fields) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        List<Map<String, Object>> srcTableData =
                !Objects.equals(tableName, "admin_role_menu") ?
                        srcDataList(srcDatabase, srcTable) :
                        srcDataList(
                                srcDatabase,
                                Wrappers.lambdaQuery(srcTable)
                                        .in("role_id", Arrays.asList(1, 2)));

        List<Map<String, Object>> desTableData = desDataList(desDatabase, desTable);
        Map<String, Map<String, Object>> srcMapTableData = getMapData(srcTableData, Arrays.asList(fields));
        Map<String, Map<String, Object>> desMapTableData = getMapData(desTableData, Arrays.asList(fields));

        List<String> lstKey = new ArrayList<>(srcTableData.get(0).keySet());
        for (Map.Entry<String, Map<String, Object>> map : srcMapTableData.entrySet()) {
            if (desMapTableData.containsKey(map.getKey())) {
                if (Objects.equals(tableName, "admin_menu")) {
                    String srcMd5Key = getMd5Key(map.getValue(), lstKey);
                    String desMd5Key = getMd5Key(desMapTableData.get(map.getKey()), lstKey);
                    if (Objects.equals(srcMd5Key, desMd5Key)) {
                        continue;
                    }

                    String updateSql = getUpdateSQl(map.getValue(),
                            Wrappers.lambdaQuery(desTable)
                                    .eq("id", map.getValue().get("id")));
                    LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "diffRoleMenuData", "updateSql", updateSql));
                    System.out.println("------------------------------------------------------------------------------------------------------------------");

                    desDataUpdate(desDatabase, updateSql);
                }
                continue;
            }

            String insertSql = getInsertSQl(map.getValue(), desTable);
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "diffRoleMenuData", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(desDatabase, insertSql);
        }
    }

}
