package com.yongkj.applet.dataMigration.core;

import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.task.SQLTask;
import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseService {

    protected Database srcDatabase;
    protected Database desDatabase;
    protected final Database devDatabase;
    protected final Database preDatabase;
    protected final Database testDatabase;
    protected final Database prodDatabase;
    protected final List<Database> databases;
    protected Map<String, Database> mapDatabase;
    protected final Database preDatabaseHologres;
    protected final Database prodDatabaseHologres;
    protected final Database preDatabaseMaxCompute;
    protected final Database prodDatabaseMaxCompute;

    protected BaseService(DataMigration dataMigration) {
        this.mapDatabase = dataMigration.getMapDatabase();
        this.srcDatabase = dataMigration.getSrcDatabase();
        this.desDatabase = dataMigration.getDesDatabase();
        this.devDatabase = dataMigration.getDevDatabase();
        this.preDatabase = dataMigration.getPreDatabase();
        this.testDatabase = dataMigration.getTestDatabase();
        this.prodDatabase = dataMigration.getProdDatabase();
        this.preDatabaseHologres = dataMigration.getPreDatabaseHologres();
        this.prodDatabaseHologres = dataMigration.getProdDatabaseHologres();
        this.preDatabaseMaxCompute = dataMigration.getPreDatabaseMaxCompute();
        this.prodDatabaseMaxCompute = dataMigration.getProdDatabaseMaxCompute();
        this.databases = Arrays.asList(
                this.devDatabase, this.testDatabase,
                this.preDatabase, this.prodDatabase
        );
    }

    protected boolean runMaxComputeTask(Database database, String sqlScript, boolean isScriptMode) {
        Account account = new AliyunAccount(database.getUsername(), database.getPassword());
        Odps odps = new Odps(account);
        odps.setEndpoint(getEndpoint(database));
        odps.setDefaultProject(getProject(database));

        try {
            Instance instance = SQLTask.run(odps, getProject(database), sqlScript, getHints(isScriptMode), null);
            instance.waitForSuccess();

            List<Record> recordList = SQLTask.getResult(instance);
            for (Record record : recordList) {
                System.out.println(record.get(0));
                System.out.println(record.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getEndpoint(Database database) {
        String url = database.getUrl().replace("jdbc:odps:", "");
        return url.split("\\?")[0];
    }

    private String getProject(Database database) {
        String[] projectStrArray = database.getUrl().split("project=");
        return projectStrArray[1].split("&")[0];
    }

    private Map<String, String> getHints(boolean isScriptMode) {
        Map<String, String> hints = new HashMap<>();
        hints.put("odps.sql.submit.mode", "statement");
        if (isScriptMode) {
            hints.put("odps.sql.submit.mode", "script");
        }
        return hints;
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

    protected boolean srcDataInsert(Database database, String insertSql) {
        return JDBCUtil.getResult(database, insertSql);
    }

    protected boolean desDataInsert(Database database, String insertSql) {
        return JDBCUtil.getResult(database, insertSql);
    }

    protected boolean desDataInsert(String insertSql) {
        return JDBCUtil.getResult(desDatabase, insertSql);
    }

    protected boolean srcDataUpdate(String updateSql) {
        return JDBCUtil.getResult(srcDatabase, updateSql);
    }

    protected boolean desDataUpdate(Database database, String updateSql) {
        return JDBCUtil.getResult(database, updateSql);
    }

    protected boolean desDataUpdate(String updateSql) {
        return JDBCUtil.getResult(desDatabase, updateSql);
    }

    protected boolean srcDataRemove(Database database, String removeSql) {
        return JDBCUtil.getResult(database, removeSql);
    }

    protected boolean srcDataRemove(String removeSql) {
        return JDBCUtil.getResult(srcDatabase, removeSql);
    }

    protected boolean desDataRemove(Database database, String removeSql) {
        return JDBCUtil.getResult(database, removeSql);
    }

    protected boolean desDataRemove(String removeSql) {
        return JDBCUtil.getResult(desDatabase, removeSql);
    }

    protected List<Map<String, Object>> srcSetDataList(Database database, Wrappers query) {
        return listSet(database, query);
    }

    protected List<Map<String, Object>> srcSetDataList(Wrappers query) {
        return listSet(srcDatabase, query);
    }

    protected List<Map<String, Object>> desSetDataList(Wrappers query) {
        return listSet(desDatabase, query);
    }

    protected List<Map<String, Object>> srcDataList(Database database, Wrappers query) {
        return list(database, query);
    }

    protected List<Map<String, Object>> srcDataList(Wrappers query) {
        return list(srcDatabase, query);
    }

    protected List<Map<String, Object>> desDataList(Wrappers query) {
        return list(desDatabase, query);
    }

    protected List<Map<String, Object>> srcDataList(Database database, Table table) {
        return list(database, table.getSelectDataSql(), new ArrayList<>());
    }

    protected List<Map<String, Object>> srcDataList(Table table) {
        return list(srcDatabase, table.getSelectDataSql(), new ArrayList<>());
    }

    protected List<Map<String, Object>> desDataList(Database database, Table table) {
        return list(database, table.getSelectDataSql(), new ArrayList<>());
    }

    protected List<Map<String, Object>> desDataList(Table table) {
        return list(desDatabase, table.getSelectDataSql(), new ArrayList<>());
    }

    private List<Map<String, Object>> listSet(Database database, Wrappers query) {
        String selectSql = getSelectSql(query);

        LogUtil.loggerLine(Log.of("BaseService", "listSet", "selectSql", selectSql));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        return JDBCUtil.getResultSet(database, selectSql);
    }

    private List<Map<String, Object>> list(Database database, Wrappers query) {
        Table table = database.getMapTable().get(query.getTableName());
        String selectSql = getSelectSql(table, query);

        LogUtil.loggerLine(Log.of("BaseService", "list", "selectSql", selectSql));
        System.out.println("------------------------------------------------------------------------------------------------------------");

        return list(database, selectSql, query.getFields());
    }

    private List<Map<String, Object>> list(Database database, String selectSql, List<String> filterFields) {
        return JDBCUtil.getResultSet(database, selectSql, filterFields);
    }

    private String getSelectSql(Wrappers query) {
        return SQL.getDataSelectSql(
                query.getFields(),
                query.getTableNames(),
                query.getSqlSegment()
        );
    }

    private String getSelectSql(Table table, Wrappers query) {
        List<String> fieldNames = new ArrayList<>();
        for (String fieldName : table.getFieldNames()) {
            if (!query.getFields().isEmpty() &&
                    !query.getFields().contains(fieldName)) continue;
            fieldNames.add(fieldName);
        }
        return SQL.getDataSelectSql(
                fieldNames,
                Collections.singletonList(query.getTableName()),
                query.getSqlSegment()
        );
    }

    protected String getRemoveSQl(Map<String, Object> mapData, String tableName) {
        return SQL.getDataRemoveSqlByObject(
                tableName,
                mapData);
    }

    protected String getRemoveSQl(Wrappers query) {
        return SQL.getDataRemoveSql(
                query.getTableName(),
                query.getSqlSegment());
    }

    protected String getUpdateSQl(Map<String, Object> mapData, Wrappers query) {
        return SQL.getDataUpdateSql(
                query.getTableName(),
                mapData,
                query.getSqlSegment());
    }

    protected String getUpdateSQl(Map<String, Object> mapData, String tableName, Wrappers query) {
        return SQL.getDataUpdateSql(
                tableName,
                mapData,
                query.getSqlSegment());
    }

    protected String getInsertSQl(Map<String, Object> mapData, Table table) {
        return SQL.getDataInsertSqlByObject(
                table.getName(),
                new ArrayList<>(mapData.keySet()),
                new ArrayList<>(mapData.values()));
    }

    protected String getMaxComputeInsertSQl(Map<String, Object> mapData, Table table) {
        Map<String, Object> mapObjectData = mapData.entrySet().stream()
                .filter(po -> !Objects.equals(po.getKey(), "ds"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return SQL.getMaxComputeDataInsertSqlByObject(
                table.getName(),
                mapData.get("ds").toString(),
                new ArrayList<>(mapObjectData.keySet()),
                new ArrayList<>(mapObjectData.values()));
    }

    protected String getInsertSQl(Map<String, Object> mapData, String tableName, Wrappers query) {
        return SQL.getDataInsertSqlByObject(
                tableName,
                new ArrayList<>(mapData.keySet()),
                new ArrayList<>(mapData.values()));
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
