package com.yongkj.applet.dataMigration.core;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Field;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseService {

    protected final Database srcDatabase;
    protected final Database desDatabase;

    protected BaseService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
    }

    protected List<Map<String, Object>> srcList(Wrappers query) {
        return list(srcDatabase, query);
    }

    protected List<Map<String, Object>> srcList(Table table) {
        return list(srcDatabase, table, table.getSelectDataSql());
    }

    protected List<Map<String, Object>> desList(Wrappers query) {
        return list(desDatabase, query);
    }

    protected List<Map<String, Object>> desList(Table table) {
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
        List<Map<String, Object>> lstData = new ArrayList<>();
        LogUtil.loggerLine(Log.of("BaseService", "list", "selectSql", selectSql));
        try {
            Statement statement = database.getManager().getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(selectSql);

            while (sqlResult.next()) {
                lstData.add(getRowData(table, sqlResult));
            }

            database.getManager().setResultSet(sqlResult);
            database.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstData;
    }

    private Map<String, Object> getRowData(Table table, ResultSet resultSet) {
        Map<String, Object> mapData = new LinkedHashMap<>();
        List<String> subFieldNames = table.getSubFieldNames();
        for (String fieldName : table.getFieldNames()) {
            Field field = table.getMapField().get(fieldName);
            if (field == null || subFieldNames.size() > 0 && !subFieldNames.contains(fieldName)) continue;
            try {
                String dataStr = resultSet.getString(fieldName);
                Object data = getTypeChangeData(dataStr, field.getType());
                mapData.put(fieldName, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mapData;
    }

    private Object getTypeChangeData(String data, String type) {
        switch (type) {
            case "INT":
            case "TINYINT":
                return GenUtil.strToInteger(data);
            case "BIGINT":
                return GenUtil.strToLong(data);
            case "DOUBLE":
            case "DECIMAL":
                return GenUtil.strToDouble(data);
            default:
                return data;
        }
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
        return GenUtil.getMd5Str(String.join("", lstData));
    }

    protected List<String> getRetainIds(Map<String, Map<String, Object>> srcTableData, Map<String, Map<String, Object>> desTableData) {
        List<String> lstId = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : srcTableData.entrySet()) {
            if (desTableData.containsKey(map.getKey())) continue;
            lstId.add(map.getKey());
        }
        return lstId;
    }

    protected String getValueStr(Object value) {
        if (value == null) return "";
        if (value instanceof String) {
            return String.format("'%s'", value);
        } else {
            return GenUtil.objToStr(value);
        }
    }

}
