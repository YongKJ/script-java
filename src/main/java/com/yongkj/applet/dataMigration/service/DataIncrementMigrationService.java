package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.util.Wrapper;

public class DataIncrementMigrationService extends BaseService {

    public DataIncrementMigrationService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
    }

    public String dataSelectTest(String keyword, String level) {
        return desList(Wrapper.query("amap_district")
                .eq("level", level)
                .and(w -> w
                        .eq("id", keyword)
                        .or()
                        .like("name", keyword)));
    }

}
