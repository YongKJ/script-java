package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SmallAssignmentUpdateService extends BaseService {

    private final boolean enable;

    public SmallAssignmentUpdateService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        Map<String, Object> smallAssignmentUpdate = GenUtil.getMap("small-assignment-update");
        this.enable = GenUtil.objToBoolean(smallAssignmentUpdate.get("enable"));
    }

    public void apply() {
        if (!enable) return;
//        categoryModifierIdUpdate();
//        relWorkerTypeLogExport();
        msgTemplateImport();
    }

    private void msgTemplateImport() {
        Table msgTemplateTable = desDatabase.getMapTable().get("_msg_template");
        String excelPath = "C:\\Users\\Admin\\Downloads\\msg-template-1702631689927(1).xlsx";
        List<Map<String, String>> lstData = PoiExcelUtil.toMap(excelPath);
        lstData = lstData.stream().filter(po -> po.containsKey("id")).collect(Collectors.toList());
        for (Map<String, String> mapData : lstData) {

            Map<String, Object> tempMapData = new HashMap<>();
            tempMapData.put("link_mark", "aftersale_detail");
            Long id = GenUtil.objToLong(mapData.get("id"));
            mapData.forEach(tempMapData::put);
            tempMapData.put("is_audio", 0);
            tempMapData.put("id", id);


            String insertSql = getInsertSQl(tempMapData, msgTemplateTable.getName(),
                    Wrappers.lambdaQuery()
                            .eq("id", id));

            LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "categoryModifierIdUpdate", "insertSql", insertSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");

            desDataInsert(insertSql);
        }

        LogUtil.loggerLine(Log.of("SmallAssignmentUpdateService", "msgTemplateImport", "lstData", lstData));
    }

    private void relWorkerTypeLogExport() {
        Table relWorkerTypeLogTable = desDatabase.getMapTable().get("rel_worker__worker_type_log");
        Table workerTypeTable = desDatabase.getMapTable().get("worker_type");

        List<Map<String, String>> lstData = new ArrayList<>();
        List<Map<String, Object>> relWorkerTypeLogDataList = desDataList(relWorkerTypeLogTable);
        Map<String, Map<String, Object>> mapWorkerType = getMapData(desDataList(workerTypeTable));
        for (Map<String, Object> relWorkerTypeLogData : relWorkerTypeLogDataList) {
            String relatedTypesStr = (String) relWorkerTypeLogData.get("related_types");
            Map<String, Object> relatedTypes = (Map<String, Object>) GenUtil.fromJsonString(relatedTypesStr, Map.class);
            List<Map<String, Object>> relWorkerTypes = (List<Map<String, Object>>) relatedTypes.get("relWorkerTypes");

            Long utcCreated = (Long) relWorkerTypeLogData.get("utc_created");
            LocalDateTime dateTime = GenUtil.toLocalDateTime(new Date(utcCreated * 1000));
            String dateTimeStr = GenUtil.localDateTimeToStr(dateTime);

            List<String> workerTypeNames = new ArrayList<>();
            for (Map<String, Object> relWorkerType : relWorkerTypes) {
                String typeId = relWorkerType.get("typeId") + "";
                if (!mapWorkerType.containsKey(typeId)) {
                    continue;
                }
                Map<String, Object> workerType = mapWorkerType.get(typeId);
                String name = GenUtil.objToStr(workerType.get("name"));
                workerTypeNames.add(name);
            }

            Map<String, String> mapData = new HashMap<>();
            mapData.put("dateTimeStr", dateTimeStr);
            mapData.put("workerTypeNameStr", String.join("、", workerTypeNames));
            lstData.add(mapData);
            System.out.println("-----------[SmallAssignmentUpdateService] relWorkerTypeLogExport -> relatedTypes: " + relatedTypes);
        }

        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("工种列表"));
        lstHeader.add(Collections.singletonList("变更时间"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        int rowIndex = lstHeader.get(0).size();
        for (Map<String, String> mapData : lstData) {
            String dateTimeStr = mapData.get("dateTimeStr");
            String workerTypeNameStr = mapData.get("workerTypeNameStr");

            int colIndex = 0;
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, workerTypeNameStr);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, colIndex, dateTimeStr);
        }
        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\worker-types.xlsx");
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
