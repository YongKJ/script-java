package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import com.yongkj.util.ThreadUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.*;
import java.util.stream.Collectors;

public class ShopWorkerDataExport extends BaseService {

    private final String path;
    private final boolean enable;
    private final String environment;
    private final int ROW_ACCESS_WINDOW_SIZE;
    private final Map<String, List<String>> mapTables;
    private final Map<String, Map<String, Map<String, Object>>> mapTableData;

    public ShopWorkerDataExport(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        this.path = "";
        this.ROW_ACCESS_WINDOW_SIZE = 100;
        this.mapTableData = new HashMap<>();
        Map<String, Object> shopWorkerDataExport = GenUtil.getMap("shop-worker-data-export");
        this.enable = GenUtil.objToBoolean(shopWorkerDataExport.get("enable"));
        this.environment = GenUtil.objToStr(shopWorkerDataExport.get("environment"));
        List<Map<String, Object>> tables = (List<Map<String, Object>>) shopWorkerDataExport.get("tables");
        this.mapTables = tables.stream().collect(Collectors.toMap(po -> GenUtil.objToStr(po.get("name")), po -> (List<String>) po.get("fields")));
    }

    public void apply() {
        if (!enable) return;
        initTableData();
        exportData();
    }

    private void initTableData() {
        for (Map.Entry<String, List<String>> map : mapTables.entrySet()) {
            Table table = Objects.equals(environment, "test") ?
                    desDatabase.getMapTable().get(map.getKey()) :
                    srcDatabase.getMapTable().get(map.getKey());
            List<Map<String, Object>> lstData = Objects.equals(environment, "test") ?
                    desDataList(table) : srcDataList(table);
            Map<String, Map<String, Object>> mapData = new HashMap<>();
            switch (map.getKey()) {
                case "admin_role_users":
                    mapData.putAll(getMapData(lstData, Arrays.asList("user_id", "role_id")));
                    break;
                case "organization_worker":
                    mapData.putAll(getMapData(lstData, Arrays.asList("organization_id", "worker_id")));
                    break;
                case "rel_worker__worker_type":
                    mapData.putAll(getMapData(lstData, Arrays.asList("organization_id", "worker_id", "type_id")));
                    break;
                default:
                    mapData.putAll(getMapData(lstData));
            }
            mapTableData.put(map.getKey(), mapData);
        }
    }

    private void exportData() {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        exportShopData(workbook.createSheet("商家店铺信息"), lstCellStyle);
        exportWorkerData(workbook.createSheet("店铺服务人员信息"), lstCellStyle);
        PoiExcelUtil.write(workbook, String.format("%s-%s.xlsx", path, System.currentTimeMillis()));
    }

    private void exportWorkerData(SXSSFSheet sheet, List<CellStyle> lstCellStyle) {
        List<List<String>> lstHeader = getWorkerDataHeader();
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        List<Map<String, Object>> lstData = getWorkerData();
        writeSheetData(lstData, sheet, lstCellStyle, lstHeader.get(0).size());
    }

    private List<Map<String, Object>> getWorkerData() {
        return new ArrayList<>();
    }

    private List<List<String>> getWorkerDataHeader() {
        List<String> lstName = Arrays.asList(
                "商家组织标识", "商家主体名称", "商家状态",
                "店铺标识", "店铺名称",
                "服务人员标识", "服务人员名称", "服务人员手机号", "服务人员状态"
        );
        List<List<String>> lstHeader = new ArrayList<>();
        for (String name : lstName) {
            lstHeader.add(Collections.singletonList(name));
        }
        return lstHeader;
    }

    private void exportShopData(SXSSFSheet sheet, List<CellStyle> lstCellStyle) {
        List<List<String>> lstHeader = getShopDataHeader();
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        List<Map<String, Object>> lstData = getShopData();
        writeSheetData(lstData, sheet, lstCellStyle, lstHeader.get(0).size());
    }

    private List<List<String>> getShopDataHeader() {
        List<String> lstName = Arrays.asList(
                "用户标识", "应用名称", "用户名称", "用户密码", "注册手机号", "数据角色", "用户状态", "用户创建时间",
                "商家组织标识", "商家主体名称", "商家组织类型", "法人名称", "法人手机号", "商家状态",
                "店铺标识", "店铺名称", "店铺地址", "服务城市", "服务地区"
        );
        List<List<String>> lstHeader = new ArrayList<>();
        for (String name : lstName) {
            lstHeader.add(Collections.singletonList(name));
        }
        return lstHeader;
    }

    private List<Map<String, Object>> getShopData() {
        return new ArrayList<>();
    }

    private void writeSheetData(List<Map<String, Object>> lstData, SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow) {
        for (int i = 0; i < lstData.size(); ) {
            int dataSize = i + ROW_ACCESS_WINDOW_SIZE;
            dataSize = Math.min(dataSize, lstData.size());
            ThreadUtil.executeWithListDataByThreadPool(1, sheet, i, dataSize, (row, index) -> {
                int colIndex = 0;
                PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, row.getRowNum());
                for (Map.Entry<String, Object> map : Optional.ofNullable(lstData.get(index)).orElse(new HashMap<>()).entrySet()) {
                    PoiExcelUtil.writeCellData(row, lstCellStyle, dataRow, colIndex++, map.getValue());
                }
            });
            i = dataSize;
        }
    }

    private void test() {
        LogUtil.loggerLine(Log.of("ShopWorkerDataExport", "test", "mapTables", mapTables));
    }
}
