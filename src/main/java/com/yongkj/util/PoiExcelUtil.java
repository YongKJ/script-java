package com.yongkj.util;

import com.yongkj.util.excel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.*;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoiExcelUtil {

    private static final int[][] MOVE = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private static Map<String, Map<Integer, Integer>> mapSheetColWidth;
    private static List<CellStyle> lstCellStyle;
    private static SXSSFDrawing drawing;
    private static int dataRow = 1;

    private PoiExcelUtil() {
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

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook) {
        return ExcelHeader.getCellStyles(workbook);
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook, Color headerBackgroundColor, Color dataBackgroundColor, Color borderColor) {
        return ExcelHeader.getCellStyles(workbook, headerBackgroundColor, dataBackgroundColor, borderColor);
    }

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
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol, List<Integer> lstExcludeRow) {
        ExcelHeaderByWidthColAndCellStyle.writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, lstExcludeRow);
    }

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

    public static Map<Integer, Integer> getInitColWidths(SXSSFSheet sheet) {
        return ExcelHeader.getInitColWidths(sheet);
    }

    public static void updateColWidth(Map<Integer, Integer> mapColWidth, Integer col, Object value) {
        ExcelHeader.updateColWidth(mapColWidth, col, value);
    }

    public static void updateColWidths(SXSSFSheet sheet, Map<Integer, Integer> mapColWidth) {
        ExcelHeader.updateColWidths(sheet, mapColWidth);
    }

    public static void write(SXSSFWorkbook workbook, String fileName) {
        ExcelWriter.write(workbook, fileName);
    }

    public static Map<String, Map<Integer, Integer>> getMapSheetColWidth() {
        return mapSheetColWidth;
    }

    public static void setMapSheetColWidth(Map<String, Map<Integer, Integer>> mapSheetColWidth) {
        PoiExcelUtil.mapSheetColWidth = mapSheetColWidth;
    }

    public static List<CellStyle> getLstCellStyle() {
        return lstCellStyle;
    }

    public static void setLstCellStyle(List<CellStyle> lstCellStyle) {
        PoiExcelUtil.lstCellStyle = lstCellStyle;
    }

    public static SXSSFDrawing getDrawing() {
        return drawing;
    }

    public static void setDrawing(SXSSFDrawing drawing) {
        PoiExcelUtil.drawing = drawing;
    }

    public static int getDataRow() {
        return dataRow;
    }

    public static void setDataRow(int dataRow) {
        PoiExcelUtil.dataRow = dataRow;
    }

    public static int[][] getMOVE() {
        return MOVE;
    }
}
