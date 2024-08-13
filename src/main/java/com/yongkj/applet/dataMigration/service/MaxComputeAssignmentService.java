package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

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
    }

}
