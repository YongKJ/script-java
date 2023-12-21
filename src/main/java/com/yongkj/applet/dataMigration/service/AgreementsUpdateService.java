package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.AgreementInfo;
import com.yongkj.applet.dataMigration.pojo.dto.SQL;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.applet.dataMigration.util.Wrappers;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.CsvUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;
import com.yongkj.util.PoiExcelUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.*;
import java.util.stream.Collectors;

public class AgreementsUpdateService extends BaseService {

    private final boolean enable;
    private final String urlSuffix;
    private final List<String> tableNames;
    private final List<AgreementInfo> lstAgreementInfos;

    public AgreementsUpdateService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> agreementsUpdate = GenUtil.getMap("agreements-update");
        this.urlSuffix = GenUtil.objToStr(agreementsUpdate.get("url-suffix"));
        this.tableNames = (List<String>) agreementsUpdate.get("table-names");
        this.enable = GenUtil.objToBoolean(agreementsUpdate.get("enable"));
        lstAgreementInfos = AgreementInfo.getAgreements(
                (List<Map<String, Object>>) agreementsUpdate.get("agreement-infos")
        );
    }

    public void apply() {
        if (!enable) return;
//        for (String tableName : tableNames) {
//            updateAgreementInfo(tableName);
//        }
//        exportArticleCsvData();
//        exportArticleExcelData();
        updateArticleDataByExcel();
        JDBCUtil.close(srcDatabase.getManager());
        JDBCUtil.close(desDatabase.getManager());
    }

    private void updateArticleDataByExcel() {
        List<Map<String, String>> lstData = PoiExcelUtil.toMap("C:\\Users\\Admin\\Desktop\\article-link-check-latest.xlsx");
        List<Map<String, String>> delDataList = lstData.stream().filter(po -> Objects.equals("×", po.get("链接内容访问"))).collect(Collectors.toList());
        LogUtil.loggerLine(Log.of("AgreementsUpdateService", "updateArticleDataByExcel", "lstData", lstData));
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        LogUtil.loggerLine(Log.of("AgreementsUpdateService", "updateArticleDataByExcel", "delDataList", delDataList));
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        List<Map<String, Object>> lstUpdateData = new ArrayList<>();
        Table articleTable = desDatabase.getMapTable().get("_article");
        Map<String, Map<String, Object>> mapArticleData = getMapData(desDataList(articleTable));
        for (Map<String, String> mapData : delDataList) {
            String id = mapData.get("文章标识");
            if (!mapArticleData.containsKey(id)) {
                continue;
            }
            Map<String, Object> articleData = mapArticleData.get(id);
            Long nowSeconds = System.currentTimeMillis() / 1000;
            articleData.put("utc_deleted", nowSeconds);
            lstUpdateData.add(articleData);
        }
        if (lstUpdateData.isEmpty()) {
            return;
        }
        for (Map<String, Object> updateData : lstUpdateData) {
            String updateSql = getUpdateSQl(articleTable, updateData);
            LogUtil.loggerLine(Log.of("AgreementsUpdateService", "apply", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(updateSql);
        }
    }

    private void exportArticleExcelData() {
        List<Map<String, String>> lstData = CsvUtil.toMap("D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\article-link-check.csv");
        LogUtil.loggerLine(Log.of("AgreementsUpdateService", "exportArticleExcelData", "lstData", lstData));
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        List<List<String>> lstHeader = new ArrayList<>();
        lstHeader.add(Collections.singletonList("序号"));
        lstHeader.add(Collections.singletonList("文章位置"));
        lstHeader.add(Collections.singletonList("标题"));
        lstHeader.add(Collections.singletonList("链接内容访问"));
        lstHeader.add(Collections.singletonList("文章标识"));
        lstHeader.add(Collections.singletonList("链接"));

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet();
        List<CellStyle> lstCellStyle = PoiExcelUtil.getCellStyles(workbook);
        PoiExcelUtil.writeHeader(sheet, lstHeader, lstCellStyle, 1);
        int rowIndex = lstHeader.get(0).size();
        for (Map<String, String> mapData : lstData) {
            String id = mapData.get("id");
            String categoryName = mapData.get("categoryName");
            String title = mapData.get("title");
            String result = mapData.get("result");
            String link = mapData.get("link");

            int colIndex = 0;
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, rowIndex);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, categoryName);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, title);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, result);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex, colIndex++, id);
            PoiExcelUtil.writeCellData(sheet, lstCellStyle, rowIndex++, colIndex, link);
        }
        PoiExcelUtil.write(workbook, "C:\\Users\\Admin\\Desktop\\article-link-check.xlsx");
    }

    private void exportArticleCsvData() {
        Table articleCategoryTable = desDatabase.getMapTable().get("_article_category");
        Map<String, Map<String, Object>> mapArticleCategoryData = getMapData(desDataList(articleCategoryTable));
        Map<Long, Map<Long, Map<String, Object>>> mapTopCategorySonCategories = getTopCategorySonCategoriesWithTopCategory(mapArticleCategoryData);
        LogUtil.loggerLine(Log.of("AgreementsUpdateService", "exportArticleCsvData", "mapTopCategorySonCategories", mapTopCategorySonCategories));
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        List<Map<String, String>> lstData = new ArrayList<>();
        Table articleTable = desDatabase.getMapTable().get("_article");
        List<Map<String, Object>> lstArticleData = desDataList(articleTable);
        for (Map<String, Object> articleData : lstArticleData) {
            Long utcDeleted = (Long) articleData.get("utc_deleted");
            if (utcDeleted > 0) {
                continue;
            }
            Long categoryId = (Long) articleData.get("category_id");
            Long id = (Long) articleData.get("id");
            String title = (String) articleData.get("title");
            String link = (String) articleData.get("link");
            List<String> categoryNames = getCategoryNames(mapArticleCategoryData, mapTopCategorySonCategories, categoryId);
            Map<String, String> mapData = new HashMap<>();
            mapData.put("id", id + "");
            mapData.put("categoryId", categoryId + "");
            mapData.put("categoryName", String.join(" > ", categoryNames));
            mapData.put("title", title);
            mapData.put("link", link);
            lstData.add(mapData);
        }
        LogUtil.loggerLine(Log.of("AgreementsUpdateService", "exportArticleCsvData", "lstData", lstData));
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        CsvUtil.printRecords(
                "D:\\Document\\MyCodes\\Github\\script-java\\src\\main\\resources\\csv\\article-link-check.csv",
                lstData,
//                "文章标识", "栏目标识", "栏目位置", "标识", "链接"
                "id", "categoryId", "categoryName", "title", "link", "link"
        );
    }

    private List<String> getCategoryNames(Map<String, Map<String, Object>> mapArticleCategoryData, Map<Long, Map<Long, Map<String, Object>>> mapTopCategorySonCategories, Long categoryId) {
        for (Map.Entry<Long, Map<Long, Map<String, Object>>> map : mapTopCategorySonCategories.entrySet()) {
            if (!map.getValue().containsKey(categoryId)) {
                continue;
            }
            return getCategoryNames(mapArticleCategoryData, map.getKey(), categoryId);
        }
        return new ArrayList<>();
    }

    private List<String> getCategoryNames(Map<String, Map<String, Object>> mapArticleCategoryData, Long topCategoryId, Long sonCategoryId) {
        List<String> categoryNames = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : mapArticleCategoryData.entrySet()) {
            Long id = (Long) map.getValue().get("id");
            if (!Objects.equals(id, topCategoryId)) {
                continue;
            }
            String name = (String) map.getValue().get("name");
            categoryNames.add(name);
            categoryNames.addAll(getSonCategoryNames(mapArticleCategoryData, id, sonCategoryId));
        }
        return categoryNames;
    }

    private List<String> getSonCategoryNames(Map<String, Map<String, Object>> mapArticleCategoryData, Long topCategoryId, Long sonCategoryId) {
        List<String> categoryNames = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> map : mapArticleCategoryData.entrySet()) {
            Long id = (Long) map.getValue().get("id");
            Long pid = (Long) map.getValue().get("pid");
            if (!Objects.equals(pid, topCategoryId)) {
                continue;
            }
            if (!Objects.equals(id, sonCategoryId)) {
                continue;
            }
            String name = (String) map.getValue().get("name");
            categoryNames.add(name);
        }
        return categoryNames;
    }

    private Map<Long, Map<Long, Map<String, Object>>> getTopCategorySonCategoriesWithTopCategory(Map<String, Map<String, Object>> mapTableData) {
        Map<Long, Map<Long, Map<String, Object>>> mapData = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> map : mapTableData.entrySet()) {
            Long pid = (Long) map.getValue().get("pid");
            if (pid != 0) {
                continue;
            }
            Long id = (Long) map.getValue().get("id");
            Map<Long, Map<String, Object>> mapCategory = new HashMap<>();
            mapCategory.put(id, map.getValue());
            mapCategory.putAll(getSonCategories(id, mapTableData));
            mapData.put(id, mapCategory);
        }
        return mapData;
    }

    private Map<Long, Map<String, Object>> getSonCategories(Long id, Map<String, Map<String, Object>> mapTableData) {
        Map<Long, Map<String, Object>> mapData = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> map : mapTableData.entrySet()) {
            Long pid = (Long) map.getValue().get("pid");
            if (!Objects.equals(pid, id)) {
                continue;
            }
            Long sid = (Long) map.getValue().get("id");
            mapData.put(sid, map.getValue());
            mapData.putAll(getSonCategories(sid, mapTableData));
        }
        return mapData;
    }

    private void updateAgreementInfo(String tableName) {
        List<Map<String, Object>> lstData = new ArrayList<>();
        Table desTable = desDatabase.getMapTable().get(tableName);
        Map<String, Map<String, Object>> desTableData = getMapData(desDataList(desTable));
        for (Map.Entry<String, Map<String, Object>> map : desTableData.entrySet()) {
            if (map.getValue().containsKey("utc_deleted")) {
                Long utcDeleted = (Long) map.getValue().get("utc_deleted");
                if (utcDeleted > 0) {
                    continue;
                }
            }
            judgeDifference(lstData, map.getValue());
        }
        if (lstData.isEmpty()) {
            return;
        }
        for (Map<String, Object> mapData : lstData) {
            String updateSql = getUpdateSQl(desTable, mapData);
            LogUtil.loggerLine(Log.of("AgreementsUpdateService", "apply", "updateSql", updateSql));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            desDataUpdate(updateSql);
        }
    }

    private String getUpdateSQl(Table desTable, Map<String, Object> mapData) {
        Long id = (Long) mapData.get("id");
        return SQL.getDataUpdateSql(
                desTable.getName(),
                mapData,
                Wrappers.lambdaQuery()
                        .eq("id", id).getSqlSegment());
    }

    private void judgeDifference(List<Map<String, Object>> lstData, Map<String, Object> mapData) {
        String link = (String) mapData.get("link");
        String title = (String) mapData.get("title");
        for (AgreementInfo agreementInfo : lstAgreementInfos) {
            boolean diffFlag = false;
            String srcUrl = agreementInfo.getSrcUrl() + urlSuffix;
            String desUrl = agreementInfo.getDesUrl() + urlSuffix;
            if (Objects.equals(link, srcUrl) && !Objects.equals(link, desUrl)) {
                diffFlag = true;
                mapData.put("link", desUrl);
            }
            if (Objects.equals(title, agreementInfo.getSrcName()) && !Objects.equals(title, agreementInfo.getDesName())) {
                diffFlag = true;
                mapData.put("title", agreementInfo.getDesName());
            }
            if (diffFlag) {
                lstData.add(mapData);
                break;
            }
        }
    }
}
