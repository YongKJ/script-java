package com.yongkj.applet.dataMigration.core;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.util.GenUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseService {

    protected final Database srcDatabase;
    protected final Database desDatabase;

    protected BaseService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
    }

    protected boolean srcTableCreate(String createSql) {
        return JDBCUtil.getResult(srcDatabase, createSql);
    }

    protected boolean desTableCreate(String createSql) {
        return JDBCUtil.getResult(desDatabase, createSql);
    }

    protected boolean srcTableDelete(String deleteSql) {
        return JDBCUtil.getResult(srcDatabase, deleteSql);
    }

    protected boolean desTableDelete(String deleteSql) {
        return JDBCUtil.getResult(desDatabase, deleteSql);
    }

    protected boolean srcFieldCreate(String createSql) {
        return JDBCUtil.getResult(srcDatabase, createSql);
    }

    protected boolean desFieldCreate(String createSql) {
        return JDBCUtil.getResult(desDatabase, createSql);
    }

    protected boolean srcFieldModify(String modifySql) {
        return JDBCUtil.getResult(srcDatabase, modifySql);
    }

    protected boolean desFieldModify(String modifySql) {
        return JDBCUtil.getResult(desDatabase, modifySql);
    }

    protected boolean srcFieldDelete(String deleteSql) {
        return JDBCUtil.getResult(srcDatabase, deleteSql);
    }

    protected boolean desFieldDelete(String deleteSql) {
        return JDBCUtil.getResult(desDatabase, deleteSql);
    }

    protected boolean srcDataInsert(String insertSql) {
        return JDBCUtil.getResult(srcDatabase, insertSql);
    }

    protected boolean desDataInsert(String insertSql) {
        return JDBCUtil.getResult(desDatabase, insertSql);
    }

    protected boolean srcDataUpdate(String updateSql) {
        return JDBCUtil.getResult(srcDatabase, updateSql);
    }

    protected boolean desDataUpdate(String updateSql) {
        return JDBCUtil.getResult(desDatabase, updateSql);
    }

    protected boolean srcDataRemove(String removeSql) {
        return JDBCUtil.getResult(srcDatabase, removeSql);
    }

    protected boolean desDataRemove(String removeSql) {
        return JDBCUtil.getResult(desDatabase, removeSql);
    }

    protected List<Map<String, Object>> srcDataList(Wrappers query) {
        return list(srcDatabase, query);
    }

    protected List<Map<String, Object>> desDataList(Wrappers query) {
        return list(desDatabase, query);
    }

    protected List<Map<String, Object>> srcDataList(Table table) {
        return list(srcDatabase, table, table.getSelectDataSql());
    }

    protected List<Map<String, Object>> desDataList(Table table) {
        return list(desDatabase, table, table.getSelectDataSql());
    }

    private List<Map<String, Object>> list(Database database, Wrappers query) {
        Table table = database.getMapTable().get(query.getTableName());
        table.setSubFieldNames(query.getFields());
        String selectSql = getSelectSql(table, query);
        List<Map<String, Object>> lstData = list(database, table, selectSql);
        table.setSubFieldNames(new ArrayList<>());
        return lstData;
    }

    private List<Map<String, Object>> list(Database database, Table table, String selectSql) {
        return JDBCUtil.getResultSet(database, table, selectSql);
    }

    private String getSelectSql(Table table, Wrappers query) {
        List<String> fieldNames = new ArrayList<>();
        List<String> subFieldNames = table.getSubFieldNames();
        for (String fieldName : table.getFieldNames()) {
            if (!subFieldNames.isEmpty() &&
                    !subFieldNames.contains(fieldName)) continue;
            fieldNames.add(fieldName);
        }
        return SQL.getDataSelectSql(
                fieldNames,
                Collections.singletonList(query.getTableName()),
                query.getSqlSegment()
        );
    }

    protected Map<String, Map<String, Object>> getMapData(List<Map<String, Object>> lstData) {
        return getMapData(lstData, new ArrayList<>());
    }

    protected Map<String, Map<String, Object>> getMapData(List<Map<String, Object>> lstData, List<String> lstField) {
        return lstData.stream().collect(Collectors.toMap(d -> getMd5Key(d, lstField), Function.identity()));
    }

    protected String getMd5Key(Map<String, Object> mapData, List<String> lstField) {
        if ((lstField == null || lstField.isEmpty()) && mapData.containsKey("id")) {
            return mapData.get("id").toString();
        }
        List<String> lstData = new ArrayList<>();
        for (Map.Entry<String, Object> map : mapData.entrySet()) {
            if (lstField != null && !lstField.isEmpty() && !lstField.contains(map.getKey())) continue;
            lstData.add(map.getValue() == null ? "null" : map.getValue().toString());
        }
        return lstData.size() == 1 ? lstData.get(0) : GenUtil.getMd5Str(String.join("", lstData));
    }

    protected List<String> getRetainIds(Map<String, Map<String, Object>> srcTableData, Map<String, Map<String, Object>> desTableData) {
        List<String> lstId = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : srcTableData.entrySet()) {
            if (desTableData.containsKey(map.getKey())) continue;
            lstId.add(map.getKey());
        }
        return lstId;
    }

}
