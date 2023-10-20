package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DataMigration {

    private final Database srcDatabase;
    private final Database desDatabase;
    private final List<String> tableNames;

    private DataMigration() {
        this.srcDatabase = Database.get("src");
        this.desDatabase = Database.get("des");
        this.tableNames = GenUtil.getList("table-names");
    }

    private void apply() {
        List<String> srcDatabases = getDatabases(srcDatabase.getManager());
        List<String> desDatabases = getDatabases(desDatabase.getManager());
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "srcDatabases", srcDatabases));
        if (!srcDatabases.contains(srcDatabase.getName()) ||
                !desDatabases.contains(desDatabase.getName())) {
            System.out.println("数据库不存在！");
            return;
        }

        SQLUtil.closeAll(srcDatabase.getManager());
        SQLUtil.closeAll(desDatabase.getManager());
    }

    private List<String> getDatabases(Manager manager) {
        List<String> databases = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            LogUtil.loggerLine(Log.of("DataMigration", "getDatabases", "resultSet", resultSet));
            while (resultSet.next()) {
                String database = resultSet.getString("TABLE_CAT");
                databases.add(database);
            }
            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databases;
    }

    public static void run(String[] args) {
        new DataMigration().apply();
    }

}
