package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class MaxComputeAssignmentService extends BaseService {

    private final boolean enable;

    public MaxComputeAssignmentService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeAssignment = GenUtil.getMap("max-compute-assignment");
        this.enable = GenUtil.objToBoolean(maxComputeAssignment.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "apply", "this.dataphinChunDevDatabase.getMapTable().size()", this.dataphinChunDevDatabase.getMapTable().size()));

//        getAllData();
        createTestData();
    }

    private void createTestData() {
        String sqlPath = FileUtil.getAbsPath(false,
                "src", "main", "resources", "sql", "max-compute", "test-odps.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "ad-store-tt.sql");
//        String sqlPath = FileUtil.getAbsPath(false,
//                "src", "main", "resources", "sql", "max-compute", "device-record.sql");
        String sqlStr = FileUtil.read(sqlPath);
        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "sqlStr", sqlStr));
        System.out.println("==========================================================================================================");

        String[] lstSql = sqlStr.split(";");
        for (String createSql : lstSql) {
            if (!StringUtils.hasText(createSql)) {
                continue;
            }
            boolean result = JDBCUtil.getResult(dataphinChunDevDatabase, createSql);

            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "createSql", createSql));
            LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "createTestData", "result", result));
            System.out.println("----------------------------------------------------------------------------------------------------------");
        }
    }

    private void getAllData() {
//        Table testTable = dataphinChunDevDatabase.getMapTable().get("test_odps");
//        Table testTable = dataphinChunDevDatabase.getMapTable().get("ad_store_tt");
        Table testTable = dataphinChunDevDatabase.getMapTable().get("event_data");
        List<Map<String, Object>> lstData = srcDataList(dataphinChunDevDatabase, testTable);

        LogUtil.loggerLine(Log.of("MaxComputeAssignmentService", "getAllData", "lstData.size()", lstData.size()));
    }

}
