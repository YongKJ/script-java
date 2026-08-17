package com.yongkj.util.excel;

import com.yongkj.util.GenUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    private ExcelReader() {
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        Workbook workbook = getWorkbook(excelFile);
        try {
            return toMap(workbook, sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
        } finally {
            closeQuietly(workbook);
        }
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        Workbook workbook = getWorkbook(excelName);
        try {
            return toMap(workbook, sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
        } finally {
            closeQuietly(workbook);
        }
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        if (workbook == null || (!(sheetName instanceof String) && !(sheetName instanceof Integer))) {
            return new ArrayList<>();
        }
        Sheet sheet;
        if (sheetName instanceof String) {
            sheet = workbook.getSheet((String) sheetName);
        } else {
            sheet = workbook.getSheetAt((Integer) sheetName);
        }
        if (sheet == null) {
            return new ArrayList<>();
        }

        List<Integer> lstHeaderCol = new ArrayList<>();
        Row headerRowObj = sheet.getRow(headerRow);
        if (headerLastCol == -1) {
            headerLastCol = headerRowObj == null ? 0 : headerRowObj.getLastCellNum();
        }
        for (int i = headerCol; i < headerLastCol; i++) {
            lstHeaderCol.add(i);
        }
        if (dataLastRow == -1) {
            dataLastRow = sheet.getLastRowNum() + 1;
        }
        //数据行用 int 区间直接遍历，避免为大规模数据构建 List<Integer>
        return toMap(sheet, headerRow, lstHeaderCol, dataRow, dataLastRow, extraData);
    }

    private static List<Map<String, String>> toMap(Sheet sheet, int headerRow, List<Integer> headerCol, int dataStartRow, int dataEndRow, Map<String, String> extraData) {
        Map<String, Integer> mapHeader = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Integer col : headerCol) {
            getExcelHeader(sheet, mapHeader, formatter, headerRow, col);
        }
        //合并单元格一次性索引，避免对每个空单元格 O(合并区域数) 全量扫描
        Map<Long, String> mergedIndex = buildMergedCellIndex(sheet, formatter);
        List<Map<String, String>> lstMap = new ArrayList<>();
        for (int row = dataStartRow; row < dataEndRow; row++) {
            Map<String, String> map = getExcelData(sheet, mapHeader, formatter, mergedIndex, row);
            if (map.isEmpty()) {
                continue;
            }
            map.putAll(extraData);
            lstMap.add(map);
        }
        return lstMap;
    }

    public static Workbook getWorkbook(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.endsWith(".xlsx")) {
                return new XSSFWorkbook(file.getInputStream());
            }
            return new HSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("读取上传 Excel 失败: " + file.getOriginalFilename(), e);
        }
    }

    public static Workbook getWorkbook(String excelFileName) {
        try {
            if (excelFileName.startsWith("/")) {
                try (InputStream in = new ClassPathResource(excelFileName).getInputStream()) {
                    return excelFileName.endsWith(".xlsx") ? new XSSFWorkbook(in) : new HSSFWorkbook(in);
                }
            }
            try (InputStream in = new FileInputStream(excelFileName)) {
                return excelFileName.endsWith(".xlsx") ? new XSSFWorkbook(in) : new HSSFWorkbook(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败: " + excelFileName, e);
        }
    }

    private static void closeQuietly(Workbook workbook) {
        if (workbook == null) {
            return;
        }
        try {
            workbook.close();
        } catch (IOException ignored) {
        }
    }

    private static void getExcelHeader(Sheet sheet, Map<String, Integer> mapHeader, DataFormatter formatter, int row, int col) {
        Row rowObj = sheet.getRow(row);
        Cell cell = rowObj == null ? null : rowObj.getCell(col);
        mapHeader.put(cell == null ? "" : getCellText(formatter, cell), col);
    }

    private static Map<String, String> getExcelData(Sheet sheet, Map<String, Integer> mapHeader, DataFormatter formatter, Map<Long, String> mergedIndex, int row) {
        Map<String, String> map = new HashMap<>();
        Row rowObj = sheet.getRow(row);
        if (rowObj == null) {
            return map;
        }
        for (Map.Entry<String, Integer> header : mapHeader.entrySet()) {
            Cell cell = rowObj.getCell(header.getValue());
            if (cell == null) {
                continue;
            }
            //设置单元格类型为String
            String value = getCellText(formatter, cell);
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    value = GenUtil.douToStr(GenUtil.round(cell.getNumericCellValue(), 2));
                } catch (IllegalStateException e) {
                    value = GenUtil.objToStr(cell.getRichStringCellValue());
                }
            }
            //获取合并单元格数据
            if (!StringUtils.hasText(value)) {
                value = mergedIndex.get(cellKey(row, header.getValue()));
            }
            if (!StringUtils.hasText(value)) {
                continue;
            }
            map.put(header.getKey(), value);
        }
        return map;
    }

    /**
     * 单元格文本：字符串单元格直接取值，绕开 DataFormatter 的格式化开销，
     * 仅对数字/日期/布尔等类型走 formatter。
     */
    private static String getCellText(DataFormatter formatter, Cell cell) {
        String value = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : formatter.formatCellValue(cell);
        return value == null ? "" : value.trim();
    }

    /** 把每个合并区域内的所有单元格映射到其左上角单元格的值，O(1) 查询。 */
    private static Map<Long, String> buildMergedCellIndex(Sheet sheet, DataFormatter formatter) {
        int numMerged = sheet.getNumMergedRegions();
        Map<Long, String> index = new HashMap<>();
        for (int i = 0; i < numMerged; i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            Row firstRow = sheet.getRow(region.getFirstRow());
            if (firstRow == null) {
                continue;
            }
            Cell topLeft = firstRow.getCell(region.getFirstColumn());
            if (topLeft == null) {
                continue;
            }
            String value = getCellText(formatter, topLeft);
            if (!StringUtils.hasText(value)) {
                continue;
            }
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                    index.put(cellKey(r, c), value);
                }
            }
        }
        return index;
    }

    private static long cellKey(int row, int col) {
        return ((long) row << 32) | (col & 0xffffffffL);
    }

}
