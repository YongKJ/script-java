package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.util.GenUtil;

import java.util.Map;

public class MaxComputeHistoryInitService extends BaseService {

    private final boolean enable;

    public MaxComputeHistoryInitService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeHistory = GenUtil.getMap("max-compute-history-init");
        this.enable = GenUtil.objToBoolean(maxComputeHistory.get("enable"));
    }

    public void apply() {
        if (!enable) return;

    }

}
