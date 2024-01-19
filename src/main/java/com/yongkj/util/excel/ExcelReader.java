package com.yongkj.util.excel;

import com.yongkj.util.GenUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.*;

public class ExcelReader {

    private ExcelReader() {
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return toMap(getWorkbook(excelFile), sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return toMap(getWorkbook(excelName), sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        if (!(sheetName instanceof String) && !(sheetName instanceof Integer)) {
            return new ArrayList<>();
        }
        Sheet sheet;
        if (sheetName instanceof String) {
            sheet = workbook.getSheet((String) sheetName);
        } else {
            sheet = workbook.getSheetAt((Integer) sheetName);
        }

        List<Integer> lstHeaderCol = new ArrayList<>();
        if (headerLastCol == -1) {
            headerLastCol = sheet.getRow(headerRow).getLastCellNum();
        }
        for (int i = headerCol; i < headerLastCol; i++) {
            lstHeaderCol.add(i);
        }
        List<Integer> lstDataRow = new ArrayList<>();
        if (dataLastRow == -1) {
            dataLastRow = sheet.getLastRowNum() + 1;
        }
        for (int i = dataRow; i < dataLastRow; i++) {
            lstDataRow.add(i);
        }
        return toMap(sheet, headerRow, lstHeaderCol, lstDataRow, extraData);
    }

    private static List<Map<String, String>> toMap(Sheet sheet, int headerRow, List<Integer> headerCol, List<Integer> dataRow, Map<String, String> extraData) {
        Map<String, Integer> mapHeader = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Integer col : headerCol) {
            getExcelHeader(sheet, mapHeader, formatter, headerRow, col);
        }
        List<Map<String, String>> lstMap = new ArrayList<>();
        for (Integer row : dataRow) {
            Map<String, String> map = getExcelData(sheet, mapHeader, formatter, row);
            if (map.isEmpty()) {
                continue;
            }
            map.putAll(extraData);
            lstMap.add(map);
        }
        return lstMap;
    }

    public static Workbook getWorkbook(MultipartFile file) {
        Workbook workbook = null;
        try {
            if (Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else {
                workbook = new HSSFWorkbook(file.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static Workbook getWorkbook(String excelFileName) {
        Workbook workbook = null;
        try {
            if (excelFileName.startsWith("/")) {
                if (excelFileName.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(new ClassPathResource(excelFileName).getInputStream());
                } else {
                    workbook = new HSSFWorkbook(new ClassPathResource(excelFileName).getInputStream());
                }
            } else {
                if (excelFileName.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(new FileInputStream(excelFileName));
                } else {
                    workbook = new HSSFWorkbook(new FileInputStream(excelFileName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workbook;
    }

    private static void getExcelHeader(Sheet sheet, Map<String, Integer> mapHeader, DataFormatter formatter, int row, int col) {
        Cell cell = sheet.getRow(row).getCell(col);
        mapHeader.put(formatter.formatCellValue(cell).trim(), col);
    }

    private static Map<String, String> getExcelData(Sheet sheet, Map<String, Integer> mapHeader, DataFormatter formatter, int row) {
        Map<String, String> map = new HashMap<>();
        if (sheet.getRow(row) == null) {
            return map;
        }
        for (Map.Entry<String, Integer> header : mapHeader.entrySet()) {
            Cell cell = sheet.getRow(row).getCell(header.getValue());
            if (cell == null) {
                continue;
            }
            //设置单元格类型为String
            String value = formatter.formatCellValue(cell).trim();
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    value = GenUtil.douToStr(GenUtil.round(cell.getNumericCellValue(), 2));
                } catch (IllegalStateException e) {
                    System.out.println("getExcelData -> value: " + value);
                    System.out.println("getExcelData -> map: " + GenUtil.toJsonString(map));
                    value = GenUtil.objToStr(cell.getRichStringCellValue());
                    e.printStackTrace();
                }
            }
            //获取合并单元格数据
            if (!StringUtils.hasText(value)) {
                value = getMergedCellValue(sheet, formatter, row, header.getValue());
            }
            if (!StringUtils.hasText(value)) {
                continue;
            }
            map.put(header.getKey(), value);
        }
        return map;
    }

    private static String getMergedCellValue(Sheet sheet, DataFormatter formatter, int row, int col) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellAddresses = sheet.getMergedRegion(i);
            if (cellAddresses.getFirstRow() <= row && row <= cellAddresses.getLastRow() &&
                    cellAddresses.getFirstColumn() <= col && col <= cellAddresses.getLastColumn()) {
                Cell cell = sheet.getRow(cellAddresses.getFirstRow()).getCell(cellAddresses.getFirstColumn());
                return formatter.formatCellValue(cell).trim();
            }
        }
        return null;
    }

}
