package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.util.GenUtil;

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

    }

    public List<Map<String, Object>> dataSelectTest(String keyword, String level) {
        return desList(Wrappers.query("amap_district")
                .eq("level", level)
                .and(w -> w
                        .eq("id", keyword)
                        .or()
                        .like("name", keyword)));
    }

}
