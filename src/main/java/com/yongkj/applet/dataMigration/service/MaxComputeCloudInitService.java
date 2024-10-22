package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class MaxComputeCloudInitService extends BaseService {

    private final boolean enable;

    public MaxComputeCloudInitService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeCloud = GenUtil.getMap("max-compute-cloud-init");
        this.enable = GenUtil.objToBoolean(maxComputeCloud.get("enable"));
    }

    public String apply() {
        if (!StringUtils.hasText(this.bizDate) && !enable) return null;

        return countEventData();
    }

    private String countEventData() {
        Table testTable = preDatabaseMaxCompute.getMapTable().get("ods_event_data");
        List<Map<String, Object>> lstData = srcSetDataList(preDatabaseMaxCompute,
                Wrappers.lambdaQuery(testTable)
                        .select("COUNT(*) `count`"));

        LogUtil.loggerLine(Log.of("MaxComputeCloudInitService", "countEventData", "lstData", lstData));

        return lstData.isEmpty() ? "" : lstData.get(0).get("count").toString();
    }

}
