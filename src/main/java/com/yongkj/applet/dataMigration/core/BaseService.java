package com.yongkj.applet.dataMigration.core;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.util.Wrappers;

import java.util.Collections;

public abstract class BaseService {

    protected final Database srcDatabase;
    protected final Database desDatabase;

    protected BaseService(Database srcDatabase, Database desDatabase) {
        this.srcDatabase = srcDatabase;
        this.desDatabase = desDatabase;
    }

    protected String srcList(Wrappers query) {
        return list(srcDatabase, query);
    }

    protected String desList(Wrappers query) {
        return list(desDatabase, query);
    }

    private String list(Database database, Wrappers query) {
        return SQL.getDataSelectSql(
                database.getMapTable().get(query.getTableName()).getFieldNames(),
                Collections.singletonList(query.getTableName()),
                query.getSqlSegment()
        );
    }

}
