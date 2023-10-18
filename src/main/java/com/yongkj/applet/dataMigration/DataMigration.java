package com.yongkj.applet.dataMigration;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DataMigration {

    private Manager srcManager;
    private Manager desManager;
    private Database srcDatabase;
    private Database desDatabase;

    private DataMigration() {
        this.srcDatabase = Database.get("src");
        this.desDatabase = Database.get("des");
        this.srcManager = SQLUtil.getConnection(srcDatabase);
        this.desManager = SQLUtil.getConnection(desDatabase);
    }

    private void apply() {
        List<String> catalogNames = getCatalogs(srcManager);
        LogUtil.loggerLine(Log.of("DataMigration", "apply", "catalogNames", catalogNames));
        SQLUtil.closeAll(srcManager);
        SQLUtil.closeAll(desManager);
    }

    private List<String> getCatalogs(Manager manager) {
        List<String> catalogNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            LogUtil.loggerLine(Log.of("DataMigration", "getCatalogs", "resultSet", resultSet));
            while (resultSet.next()) {
                String tableCat = resultSet.getString("TABLE_CAT");
                catalogNames.add(tableCat);
            }
            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catalogNames;
    }

    public static void run(String[] args) {
        new DataMigration().apply();
    }

}
