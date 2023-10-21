package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.util.GenUtil;

import java.util.List;

public class IncrementMigrationService {

    private final Database srcDatabase;
    private final Database desDatabase;
    private List<String> tableNames;

    public IncrementMigrationService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
        this.tableNames = GenUtil.getList("table-names");
    }

    public void apply() {

    }

}
