package com.yongkj.util;

import com.yongkj.util.excel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 读写统一门面。
 *
 * <p>仅做转发，不持有任何可变状态——原先的静态字段（lstCellStyle / drawing / dataRow /
 * mapSheetColWidth）已迁移到 {@link ExcelContext}，按 workbook 维度隔离，参见
 * {@code ExcelContext} 的说明。并发导出不同 workbook 互不影响。
 */
public class PoiExcelUtil {

    private PoiExcelUtil() {
    }

    // ==================== 读取 ====================

    public static Workbook getWorkbook(MultipartFile file) {
        return ExcelReader.getWorkbook(file);
    }

    public static Workbook getWorkbook(String excelFileName) {
        return ExcelReader.getWorkbook(excelFileName);
    }

    public static List<Map<String, String>> toMap(Workbook workbook) {
        return toMap(workbook, 0);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName) {
        return toMap(workbook, sheetName, 0);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow) {
        return toMap(workbook, sheetName, headerRow, headerRow + 1);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int dataRow) {
        return toMap(workbook, sheetName, headerRow, dataRow, -1);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int dataRow, int dataLastRow) {
        return toMap(workbook, sheetName, headerRow, dataRow, dataLastRow, new HashMap<>());
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return toMap(workbook, sheetName, headerRow, 0, -1, dataRow, dataLastRow, extraData);
    }

    public static <T> List<Map<String, String>> toMap(Workbook workbook, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return ExcelReader.toMap(workbook, sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
    }

    public static List<Map<String, String>> toMap(MultipartFile excelFile) {
        return toMap(excelFile, 0);
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName) {
        return toMap(excelFile, sheetName, 0);
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow) {
        return toMap(excelFile, sheetName, headerRow, headerRow + 1);
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int dataRow) {
        return toMap(excelFile, sheetName, headerRow, dataRow, -1);
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int dataRow, int dataLastRow) {
        return toMap(excelFile, sheetName, headerRow, dataRow, dataLastRow, new HashMap<>());
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return toMap(excelFile, sheetName, headerRow, 0, -1, dataRow, dataLastRow, extraData);
    }

    public static <T> List<Map<String, String>> toMap(MultipartFile excelFile, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return ExcelReader.toMap(excelFile, sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
    }

    public static List<Map<String, String>> toMap(String excelName) {
        return toMap(excelName, 0);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName) {
        return toMap(excelName, sheetName, 0);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow) {
        return toMap(excelName, sheetName, headerRow, headerRow + 1);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int dataRow) {
        return toMap(excelName, sheetName, headerRow, dataRow, -1);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int dataRow, int dataLastRow) {
        return toMap(excelName, sheetName, headerRow, dataRow, dataLastRow, new HashMap<>());
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return toMap(excelName, sheetName, headerRow, 0, -1, dataRow, dataLastRow, extraData);
    }

    public static <T> List<Map<String, String>> toMap(String excelName, T sheetName, int headerRow, int headerCol, int headerLastCol, int dataRow, int dataLastRow, Map<String, String> extraData) {
        return ExcelReader.toMap(excelName, sheetName, headerRow, headerCol, headerLastCol, dataRow, dataLastRow, extraData);
    }

    // ==================== 样式 ====================

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook) {
        return ExcelHeader.getCellStyles(workbook);
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook, Color headerBackgroundColor, Color dataBackgroundColor, Color borderColor) {
        return ExcelHeader.getCellStyles(workbook, headerBackgroundColor, dataBackgroundColor, borderColor);
    }

    // ==================== 表头 ====================

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader) {
        writeHeader(sheet, lstHeader, 0);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol) {
        writeHeader(sheet, lstHeader, dataCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol, List<Integer> lstExcludeRow) {
        ExcelHeader.writeHeader(sheet, lstHeader, dataCol, lstExcludeRow);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle) {
        writeHeader(sheet, lstHeader, lstCellStyle, 0);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, List<Integer> lstExcludeRow) {
        ExcelHeaderByCellStyle.writeHeader(sheet, lstHeader, lstCellStyle, dataCol, lstExcludeRow);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int widthCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, ExcelContext.of(sheet.getWorkbook()).getDataStartRow(), widthCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol, List<Integer> lstExcludeRow) {
        ExcelHeaderByWidthColAndCellStyle.writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, lstExcludeRow);
    }

    // ==================== 图片 / 数据 ====================

    public static void writePicture(SXSSFSheet sheet, int rowIndex, int colIndex, String filePath) throws Exception {
        writePicture(sheet, rowIndex, 0, colIndex, 0, filePath);
    }

    public static void writePicture(SXSSFSheet sheet, int rowIndex, int rowOffset, int colIndex, int colOffset, String filePath) throws Exception {
        ExcelWriter.writePicture(sheet, rowIndex, rowOffset, colIndex, colOffset, filePath);
    }

    public static void writeData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, List<Map<Integer, Object>> lstData) {
        ExcelWriter.writeData(sheet, lstCellStyle, lstData);
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.writeCellData(sheet, lstCellStyle, rowIndex, colIndex, cellData);
    }

    public static void writeData(SXSSFSheet sheet, List<Map<Integer, Object>> lstData) {
        ExcelWriter.writeData(sheet, lstData);
    }

    public static void writeRowData(SXSSFSheet sheet, Map<Integer, Object> mapData) {
        ExcelWriter.writeRowData(sheet, mapData);
    }

    public static void writeCellData(SXSSFSheet sheet, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.writeCellData(sheet, rowIndex, colIndex, cellData);
    }

    public static void writePartialData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, List<Map<Integer, Object>> lstData) {
        ExcelWriter.writePartialData(sheet, lstCellStyle, dataRow, lstData);
    }

    public static void writeRowData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, Map<Integer, Object> mapData) {
        ExcelWriter.writeRowData(sheet, lstCellStyle, dataRow, mapData);
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.writeCellData(sheet, lstCellStyle, dataRow, rowIndex, colIndex, cellData);
    }

    public static void writeCellData(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int colIndex, Object cellData) {
        ExcelWriter.writeCellData(row, lstCellStyle, dataRow, colIndex, cellData);
    }

    public static void writeCellDataByRow(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.writeCellDataByRow(row, lstCellStyle, dataRow, rowIndex, colIndex, cellData);
    }

    public static void writeCellData(SXSSFCell cell, List<CellStyle> lstCellStyle, int dataRow, Object cellData) {
        ExcelWriter.writeCellData(cell, lstCellStyle, dataRow, cellData);
    }

    // ==================== 列宽 ====================

    public static Map<Integer, Integer> getInitColWidths(SXSSFSheet sheet) {
        return ExcelHeader.getInitColWidths(sheet);
    }

    public static void updateColWidth(Map<Integer, Integer> mapColWidth, Integer col, Object value) {
        ExcelHeader.updateColWidth(mapColWidth, col, value);
    }

    public static void updateColWidths(SXSSFSheet sheet, Map<Integer, Integer> mapColWidth) {
        ExcelHeader.updateColWidths(sheet, mapColWidth);
    }

    // ==================== 写盘 ====================

    /**
     * 将 workbook 写出到文件。
     *
     * @return 是否写出成功。注意：方法不会关闭 workbook（可能还有后续写出），
     * 调用方结束使用后应自行 {@code workbook.close()} 释放临时文件。
     */
    public static boolean write(SXSSFWorkbook workbook, String fileName) {
        return ExcelWriter.write(workbook, fileName);
    }
}
