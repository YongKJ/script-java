package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmallAssignmentUpdateService extends BaseService {

    private final boolean enable;

    public SmallAssignmentUpdateService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        Map<String, Object> smallAssignmentUpdate = GenUtil.getMap("small-assignment-update");
        this.enable = GenUtil.objToBoolean(smallAssignmentUpdate.get("enable"));
    }

    public void apply() {
        if (!enable) return;
        categoryModifierIdUpdate();
    }

    private void categoryModifierIdUpdate() {
        Table organizationTable = desDatabase.getMapTable().get("organization");
        Table organizationCategoryTable = desDatabase.getMapTable().get("organization_category_ultra");

        List<Map<String, Object>> lstData = new ArrayList<>();
        Map<String, Map<String, Object>> mapOrganizationData = getMapData(desDataList(organizationTable));
        Map<String, Map<String, Object>> mapOrganizationCategoryData = getMapData(desDataList(organizationCategoryTable));
        for (Map.Entry<String, Map<String, Object>> map : mapOrganizationCategoryData.entrySet()) {
            Long modifierId = (Long) map.getValue().get("modifier_id");
            if (modifierId > 0) {
                continue;
            }
            String organizationId = map.getValue().get("organization_id") + "";
            if (!mapOrganizationData.containsKey(organizationId)) {
                continue;
            }
            Map<String, Object> organization = mapOrganizationData.get(organizationId);
            Long administratorId = (Long) organization.get("administrator_id");
            if (administratorId == 0) {
                continue;
            }
            map.getValue().put("modifier_id", administratorId);
            lstData.add(map.getValue());
        }
        if (lstData.isEmpty()) {
            return;
        }
        for (Map<String, Object> mapData : lstData) {
            Long id = (Long) mapData.get("id");
            String updateSql = getUpdateSQl(mapData, organizationCategoryTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));
            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(updateSql);
        }
    }

}
