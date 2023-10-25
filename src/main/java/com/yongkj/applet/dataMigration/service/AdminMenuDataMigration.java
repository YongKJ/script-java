package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminMenuDataMigration extends BaseService {

    private final boolean enable;
    private final List<Long> lstMenuId;
    private final List<String> tableNames;
    private final List<String> filterNames;

    public AdminMenuDataMigration(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        this.lstMenuId = new ArrayList<>();
        Map<String, Object> menuMigration = GenUtil.getMap("admin-menu-data-migration");
        this.filterNames = (List<String>) menuMigration.get("filter-names");
        this.tableNames = (List<String>) menuMigration.get("table-names");
        this.enable = GenUtil.objToBoolean(menuMigration.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        for (String tableName : tableNames) {
            compareAndMigrationData(tableName);
        }
        JDBCUtil.close(srcDatabase.getManager());
        JDBCUtil.close(desDatabase.getManager());
    }

    private void compareAndMigrationData(String tableName) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        switch (tableName) {
            case "admin_menu":
//                adminMenuMigrationDataTestOne(srcTable, desTable);
//                adminMenuRouteParamMigrationData(srcTable, desTable);
//                adminMenuMigrationDataTest(desTable);
                adminMenuMigrationData(desTable);
                break;
            case "admin_role_menu":
                adminRoleMenuMigrationData(srcTable, desTable);
                break;
            case "admin_apply_menu":
                adminApplyMenuMigrationData(srcTable, desTable);
                break;
            default:
        }
    }

    private void adminMenuRouteParamMigrationData(Table srcTable, Table desTable) {
        List<Map<String, Object>> srcListData = srcList(
                Wrappers.lambdaQuery(srcTable.getName())
                        .ne("route_param", ""));
        List<Map<String, Object>> desListData = desList(desTable);

        List<String> lstField = Arrays.asList("pid", "name", "apply_id", "route", "route_param");
        Map<String, Map<String, Object>> srcMapData = getMapData(srcListData, lstField);
        Map<String, Map<String, Object>> desMapData = getMapData(desListData, lstField);

        List<Map<String, Object>> lstFilterData = new ArrayList<>();
        List<String> ids = getRetainIds(srcMapData, desMapData);
        for (String id : ids) {
            lstFilterData.add(srcMapData.get(id));
        }
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuRouteParamMigrationData", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuRouteParamMigrationData", "lstFilterData", lstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        List<Map<String, Object>> tempLstFilterData = new ArrayList<>();
        List<String> fields = Arrays.asList("pid", "name", "apply_id", "route");
        Map<String, Map<String, Object>> tempDesMapData = getMapData(desListData, fields);
        for (Map<String, Object> data : lstFilterData) {
            String key = getMd5Key(data, fields);
            Map<String, Object> tempData = tempDesMapData.get(key);
            if (tempData == null) continue;
            String routeParam = GenUtil.objToStr(data.get("route_param"));
            tempData.put("route_param", routeParam);
            tempLstFilterData.add(tempData);
        }
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuRouteParamMigrationData", "tempLstFilterData.size()", tempLstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuRouteParamMigrationData", "tempLstFilterData", tempLstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        for (Map<String, Object> filterData : tempLstFilterData) {
            String sql = getUpdateSql(desTable, filterData);
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuRouteParamMigrationData", "sql", sql));
            updateDesData(desTable, filterData);
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void adminMenuMigrationData(Table desTable) {
        List<Map<String, Object>> srcListData = compareDataById(desTable);
        List<Map<String, Object>> tempSrcListData = compareDataByMd5(
                srcListData, desTable, Arrays.asList("pid", "name", "apply_id", "route")
        );
        List<Map<String, Object>> lstFilterData = getDataByTime(tempSrcListData);
        for (Map<String, Object> data : lstFilterData) {
            String name = GenUtil.objToStr(data.get("name"));
            if (filterNames != null && !filterNames.isEmpty() && !filterNames.contains(name)) continue;
            Long id = GenUtil.objToLong(data.get("id"));
            insertDesData(desTable, data);
            lstMenuId.add(id);
        }
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationData", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationData", "lstFilterData", lstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void adminRoleMenuMigrationData(Table srcTable, Table desTable) {
        List<Map<String, Object>> srcData = srcTable.getFieldNames().contains("id") ?
                compareDataById(desTable) : compareDataByMd5(srcTable, desTable);
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map<String, Object> data : srcData) {
            Long menuId = GenUtil.objToLong(data.get("menu_id"));
            if (!lstMenuId.contains(menuId)) continue;
            lstData.add(data);
        }
        List<Map<String, Object>> lstFilterData = getDataByTime(lstData);
        for (Map<String, Object> data : lstFilterData) {
            insertDesData(desTable, data);
        }
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminRoleMenuMigrationData", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminRoleMenuMigrationData", "lstFilterData", lstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void adminApplyMenuMigrationData(Table srcTable, Table desTable) {
        List<Map<String, Object>> srcData = srcTable.getFieldNames().contains("id") ?
                compareDataById(desTable) : compareDataByMd5(srcTable, desTable);
        List<Map<String, Object>> lstData = new ArrayList<>();
        for (Map<String, Object> data : srcData) {
            Long menuId = GenUtil.objToLong(data.get("menu_id"));
            if (!lstMenuId.contains(menuId)) continue;
            lstData.add(data);
        }
        List<Map<String, Object>> lstFilterData = getDataByTime(lstData);
        for (Map<String, Object> data : lstFilterData) {
            insertDesData(desTable, data);
        }
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminApplyMenuMigrationData", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminApplyMenuMigrationData", "lstFilterData", lstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void updateDesData(Table table, Map<String, Object> data) {
        if (table == null || data == null) {
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "updateDesData", "error", "table or data not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            String updateSql = getUpdateSql(table, data);
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "updateDesData", "updateSql", updateSql));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(updateSql);

            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "updateDesData", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "updateDesData", "success", "update data success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUpdateSql(Table table, Map<String, Object> data) {
        String id = GenUtil.objToStr(data.get("id"));
        String sqlSegment = String.format("`%s` = %s", "id", id);
        return SQL.getDataUpdateSql(
                table.getName(),
                data,
                sqlSegment
        );
    }

    private void insertDesData(Table table, Map<String, Object> data) {
        if (table == null || data == null) {
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "insertDesData", "error", "table or data not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            String insertSql = getInsertSql(table, data);
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "insertDesData", "insertSql", insertSql));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(insertSql);

            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "insertDesData", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "insertDesData", "success", "insert data success!"));
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

    private void adminMenuMigrationDataTest(Table desTable) {
        List<Map<String, Object>> srcListData = compareDataById(desTable);
        List<Map<String, Object>> tempSrcListData = compareDataByMd5(
                srcListData, desTable, Arrays.asList("pid", "name", "apply_id", "route")
        );
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "srcListData.size()", srcListData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "srcListData", srcListData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempSrcListData.size()", tempSrcListData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempSrcListData", tempSrcListData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        Map<String, Map<String, Object>> srcMapData = getMapData(srcListData);
        Map<String, Map<String, Object>> tempSrcMapData = getMapData(tempSrcListData);

        List<Map<String, Object>> lstRetainData = new ArrayList<>();
        List<String> ids = getRetainIds(srcMapData, tempSrcMapData);
        for (String id : ids) {
            lstRetainData.add(srcMapData.get(id));
        }
        Map<String, Map<String, Object>> mapRetainData = getMapData(lstRetainData);

        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "srcMapData.size()", srcMapData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "srcMapData", srcMapData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempSrcMapData.size()", tempSrcMapData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempSrcMapData", tempSrcMapData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "mapRetainData.size()", mapRetainData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "mapRetainData", mapRetainData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        List<Map<String, Object>> lstFilterData = getDataByTime(tempSrcListData);
        Map<String, Map<String, Object>> tempLstFilterData = getMapData(lstFilterData);

        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "lstFilterData", lstFilterData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempLstFilterData.size()", tempLstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTest", "tempLstFilterData", tempLstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

    }

    private void adminMenuMigrationDataTestOne(Table srcTable, Table desTable) {
        List<Map<String, Object>> srcListData = srcList(
                Wrappers.lambdaQuery(srcTable.getName())
                        .ne("route_param", "")
        );
        List<Map<String, Object>> desListData = desList(
                Wrappers.lambdaQuery(desTable.getName())
                        .ne("route_param", "")
        );

        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "srcListData.size()", srcListData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "srcListData", srcListData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "desListData.size()", desListData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "desListData", desListData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        List<String> lstField = Arrays.asList("pid", "name", "apply_id", "route", "route_param");
        Map<String, Map<String, Object>> srcMapData = getMapData(srcListData, lstField);
        Map<String, Map<String, Object>> desMapData = getMapData(desListData, lstField);

        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "srcMapData.size()", srcMapData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "srcMapData", srcMapData));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "desMapData.size()", desMapData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "desMapData", desMapData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        List<Map<String, Object>> lstFilterData = new ArrayList<>();
        List<String> ids = getRetainIds(srcMapData, desMapData);
        for (String id : ids) {
            lstFilterData.add(srcMapData.get(id));
        }

        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "ids.size()", ids.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "ids", ids));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "lstFilterData.size()", lstFilterData.size()));
        LogUtil.loggerLine(Log.of("AdminMenuDataMigration", "adminMenuMigrationDataTestOne", "lstFilterData", lstFilterData));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    private List<Map<String, Object>> getDataByTime(List<Map<String, Object>> lstData) {
        LocalDateTime nowTime = LocalDateTime.now();
        List<Map<String, Object>> tempLstData = new ArrayList<>();
        for (Map<String, Object> data : lstData) {
            Long timestamp = GenUtil.objToLong(data.get("utc_created")) * 1000;
            LocalDateTime dateTime = GenUtil.timestampToLdt(timestamp);
            if (dateTime.plusDays(14).isBefore(nowTime)) continue;
            tempLstData.add(data);
        }
        return tempLstData;
    }

    private List<Map<String, Object>> compareDataByMd5(List<Map<String, Object>> lstData, Table desTable, List<String> fields) {
        Map<String, Map<String, Object>> srcTableData = getMapData(lstData, fields);
        Map<String, Map<String, Object>> desTableData = getMapData(desList(desTable), fields);

        List<Map<String, Object>> tempLstData = new ArrayList<>();
        List<String> ids = getRetainIds(srcTableData, desTableData);
        for (String id : ids) {
            tempLstData.add(srcTableData.get(id));
        }
        return tempLstData;
    }

    private List<Map<String, Object>> compareDataByMd5(Table srcTable, Table desTable) {
        Map<String, Map<String, Object>> srcTableData = getMapData(srcList(srcTable));
        Map<String, Map<String, Object>> desTableData = getMapData(desList(desTable));

        List<Map<String, Object>> lstData = new ArrayList<>();
        List<String> ids = getRetainIds(srcTableData, desTableData);
        for (String id : ids) {
            lstData.add(srcTableData.get(id));
        }
        return lstData;
    }

    private List<Map<String, Object>> compareDataById(Table desTable) {
        String tableName = desTable.getName();
        Map<String, Map<String, Object>> desTableData = getMapData(desList(
                Wrappers.lambdaQuery(tableName).select("id")
        ));
        List<Long> lstId = desTableData.values().stream()
                .map(v -> GenUtil.objToLong(v.get("id"))).collect(Collectors.toList());
        return lstId.isEmpty() ? new ArrayList<>() : srcList(
                Wrappers.lambdaQuery(tableName).notIn("id", lstId)
        );
    }

}
