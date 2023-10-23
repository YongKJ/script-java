package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminMenuDataMigration extends BaseService {

    private final boolean enable;
    private final List<String> tableNames;

    public AdminMenuDataMigration(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        Map<String, Object> dataMigration = GenUtil.getMap("admin-menu-data-migration");
        this.tableNames = (List<String>) dataMigration.get("table-names");
        this.enable = GenUtil.objToBoolean(dataMigration.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        for (String tableName : tableNames) {
            compareAndMigrationData(tableName);
        }
        SQLUtil.close(srcDatabase.getManager());
        SQLUtil.close(desDatabase.getManager());
    }

    private void compareAndMigrationData(String tableName) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        switch (tableName) {
            case "admin_apply_menu":
                adminApplyMenuMigrationData(srcTable, desTable);
                break;
            case "admin_menu":
                adminMenuMigrationData(srcTable, desTable);
                break;
            case "admin_role_menu":
                adminRoleMenuMigrationData(srcTable, desTable);
                break;
            default:
        }
    }

    private void adminRoleMenuMigrationData(Table srcTable, Table desTable) {
        Map<String, Map<String, Object>> srcTableData = getMapData(srcList(srcTable));
        Map<String, Map<String, Object>> desTableData = getMapData(desList(desTable));

        List<String> ids = getRetainIds(srcTableData, desTableData);
    }

    private List<String> getAdminRoleMenuIds(Map<String, Map<String, Object>> srcTableData, Map<String, Map<String, Object>> desTableData) {
        List<String> lstFieldName =
    }

    private void adminMenuMigrationData(Table srcTable, Table desTable) {
        if (srcTable.getFieldNames().contains("id")) {
            compareAndMigrationDataById(desTable);
        } else {
            compareAndMigrationDataByMd5(srcTable, desTable);
        }
    }

    private void adminApplyMenuMigrationData(Table srcTable, Table desTable) {
        if (srcTable.getFieldNames().contains("id")) {
            compareAndMigrationDataById(desTable);
        } else {
            compareAndMigrationDataByMd5(srcTable, desTable);
        }
    }

    private void compareAndMigrationDataByMd5(Table srcTable, Table desTable) {
        Map<String, Map<String, Object>> srcTableData = getMapData(srcList(srcTable));
        Map<String, Map<String, Object>> desTableData = getMapData(desList(desTable));

        List<String> ids = getRetainIds(srcTableData, desTableData);
        for (String id : ids) {
            Map<String, Object> srcData = srcTableData.get(id);
//            insertDesData(desTable, srcData);
        }
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataByMd5", "ids.size()", ids.size()));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void compareAndMigrationDataById(Table desTable) {
        String tableName = desTable.getName();
        Map<String, Map<String, Object>> desTableData = getMapData(desList(
                Wrappers.lambdaQuery(tableName).select("id")
        ));
        List<Long> lstId = desTableData.values().stream()
                .map(v -> GenUtil.objToLong(v.get("id"))).collect(Collectors.toList());
        Map<String, Map<String, Object>> srcTableData = lstId.isEmpty() ? new HashMap<>() : getMapData(srcList(
                Wrappers.lambdaQuery(tableName).notIn("id", lstId)
        ));
        List<String> ids = new ArrayList<>(srcTableData.keySet());
        for (String id : ids) {
            Map<String, Object> srcData = srcTableData.get(id);
//            insertDesData(desTable, srcData);
        }
        LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "compareAndMigrationDataById", "ids.size()", ids.size()));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void insertDesData(Table table, Map<String, Object> data) {
        if (table == null || data == null) {
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "error", "table or data not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            String insertSql = getInsertSql(table, data);
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "insertSql", insertSql));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(insertSql);

            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("DataIncrementMigrationService", "insertDesData", "success", "insert data success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getInsertSql(Table table, Map<String, Object> data) {
        return SQL.getDataInsertSql(
                table.getName(),
                table.getFieldNames(),
                getDataValue(table.getFieldNames(), data)
        );
    }

    private List<String> getDataValue(List<String> lstFieldName, Map<String, Object> mapData) {
        List<String> lstData = new ArrayList<>();
        for (String fieldName : lstFieldName) {
            Object value = mapData.get(fieldName);
            if (value instanceof String) {
                lstData.add(String.format("'%s'", value));
            } else {
                lstData.add(value.toString());
            }
        }
        return lstData;
    }

}
