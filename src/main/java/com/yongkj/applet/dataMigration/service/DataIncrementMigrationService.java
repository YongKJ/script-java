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
import java.util.List;
import java.util.Map;

public class DataIncrementMigrationService extends BaseService {

    private final List<String> tableNames;

    public DataIncrementMigrationService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        this.tableNames = GenUtil.getList("table-names");
    }

    public void apply() {
        for (String tableName : tableNames) {
            compareAndMigrationData(tableName);
        }
        SQLUtil.close(srcDatabase.getManager());
        SQLUtil.close(desDatabase.getManager());
    }

    private void compareAndMigrationData(String tableName) {
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        Map<Long, Map<String, Object>> srcTableData = getMapData(srcList(srcTable));
        Map<Long, Map<String, Object>> desTableData = getMapData(desList(desTable));

        List<Long> ids = getRetainIds(srcTableData, desTableData);
        for (Long id : ids) {
            Map<String, Object> srcData = srcTableData.get(id);
            insertDesData(desTable, srcData);
        }
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

    public List<Map<String, Object>> dataSelectTest(String keyword, String level) {
        return desList(Wrappers.lambdaQuery("amap_district")
                .eq("level", level)
                .and(w -> w
                        .eq("id", keyword)
                        .or()
                        .like("name", keyword)));
    }

}
