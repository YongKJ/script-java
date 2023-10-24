package com.yongkj.util.excel;

import com.yongkj.pojo.dto.Coords;
import com.yongkj.util.PoiExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExcelHeader {

    private ExcelHeader() {
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol) {
        writeHeader(sheet, lstHeader, dataCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, int dataCol, List<Integer> lstExcludeRow) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        boolean[][] lstFlag = new boolean[rowSize][colSize];
        if (PoiExcelUtil.getLstCellStyle() == null) {
            PoiExcelUtil.setLstCellStyle(getCellStyles(sheet.getWorkbook()));
        }
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                if (!lstFlag[row][col]) {
                    lstFlag[row][col] = true;

                    List<Coords> lstCoords = new ArrayList<>();
                    lstCoords.add(Coords.of(lstHeader, row, col));
                    //取消某些行的单元格合并
                    if (lstExcludeRow == null || lstExcludeRow.isEmpty() || !lstExcludeRow.contains(row)) {
                        checkMergeRange(lstHeader, lstFlag, lstCoords, row, col, lstHeader.get(col).get(row));
                    }

                    merge(sheet, lstCoords);
                }
            }
        }
        //单元格冻结：从上往下，冻结 dataRow 行；从左往右，冻结 dataCol 列
        sheet.createFreezePane(dataCol, PoiExcelUtil.getDataRow(), dataCol, PoiExcelUtil.getDataRow());
    }

    public static void checkMergeRange(List<List<String>> lstHeader, boolean[][] lstFlag, List<Coords> lstCoords, int x, int y, String value) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        for (int[] move : PoiExcelUtil.getMOVE()) {
            int moveX = x + move[0];
            int moveY = y + move[1];
            if (0 <= moveX && moveX < rowSize && 0 <= moveY && moveY < colSize && !lstFlag[moveX][moveY]) {
                if (Objects.equals(lstHeader.get(moveY).get(moveX), value)) {
                    lstFlag[moveX][moveY] = true;
                    lstCoords.add(Coords.of(lstHeader, moveX, moveY));
                    checkMergeRange(lstHeader, lstFlag, lstCoords, moveX, moveY, value);
                }
            }
        }
    }

    private static void merge(SXSSFSheet sheet, List<Coords> lstCoords) {
        //单元格坐标排序
        lstCoords.sort((c1, c2) -> {
            if (c1.getX() > c2.getX() || c1.getY() > c2.getY()) {
                return 1;
            } else if (c1.getX() < c2.getX() || c1.getY() < c2.getY()) {
                return -1;
            } else {
                return 0;
            }
        });
        //表头数据写入到最小坐标的单元格中
        ExcelWriter.setCellValue(sheet, lstCoords.get(0).getX(), lstCoords.get(0).getY(), lstCoords.get(0).getValue());
        for (Coords coords : lstCoords) {
            //设置列宽
//            if (coords.getX() > 0) {
//                setWidthColByAuto(sheet, coords.getY(), coords.getValue());
//            }
            setWidthColByAuto(sheet, coords.getY(), coords.getValue());
            //设置行高
            Row row = CellUtil.getRow(coords.getX(), sheet);
            row.setHeightInPoints(180);
            row.setHeight((short) (4 * 180));
            //设置单元格样式
            Cell cell = CellUtil.getCell(row, coords.getY());
            cell.setCellStyle(PoiExcelUtil.getLstCellStyle().get(0));
            //设置数据行号
            if (coords.getX() + 1 > PoiExcelUtil.getDataRow()) {
                PoiExcelUtil.setDataRow(coords.getX() + 1);
            }
        }
        //合并单元格
        if (lstCoords.size() > 1) {
            Coords minCoords = lstCoords.get(0);
            Coords maxCoords = lstCoords.get(lstCoords.size() - 1);
            sheet.addMergedRegion(new CellRangeAddress(minCoords.getX(), maxCoords.getX(), minCoords.getY(), maxCoords.getY()));
        }
    }

    public static void setWidthColByAuto(SXSSFSheet sheet, int col, String value) {
        int widthCol = sheet.getColumnWidth(col) / 256;
        int tempWidthCol = getWidthCol(value);
        if (widthCol < tempWidthCol) {
            widthCol = tempWidthCol;
        }
        sheet.setColumnWidth(col, widthCol * 256);
    }

    private static int getWidthCol(String value) {
        int chineseSum = 0;
        int englishSum = 0;
        int charSum = 0;
        char[] lstChar = value.toCharArray();
        for (char c : lstChar) {
            if (isChineseByScript(c)) {
                chineseSum += 2;
                charSum += 2;
            } else {
                englishSum += 1;
                charSum += 1;
            }
        }
        charSum = charSum > 1 ? 13 + charSum - 1 : 13;
        if (chineseSum == 0 && englishSum > 0) {
            charSum += 4;
        } else if (chineseSum > 0 && englishSum > 0) {
            double percent = (double) englishSum / chineseSum;
            if (percent < 0.2) {
                charSum -= 2;
            } else if (percent > 0.8) {
                charSum += 2;
            }
        }
        return charSum;
    }

    private static boolean isChineseByScript(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return sc == Character.UnicodeScript.HAN;
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook) {
        return getCellStyles(workbook, new Color(242, 242, 242), new Color(250, 250, 250), new Color(212, 212, 212));
//        return getCellStyles(workbook, new Color(239, 243, 245), new Color(247, 247, 247), new Color(221, 221, 221));
    }

    public static List<CellStyle> getCellStyles(SXSSFWorkbook workbook, Color headerBackColor, Color dataBackColor, Color borderColor) {
        List<XSSFColor> lstColor = Arrays.asList(
                new XSSFColor(headerBackColor, null),
                new XSSFColor(dataBackColor, null),
                new XSSFColor(borderColor, null)
        );
        return Arrays.asList(
                getCellStyle(workbook, lstColor, 0),
                getCellStyle(workbook, lstColor, 1),
                getCellStyle(workbook, lstColor, 2)
        );
    }

    private static XSSFCellStyle getCellStyle(SXSSFWorkbook workbook, List<XSSFColor> lstColor, int choose) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        if (choose == 0) {
            //设置表头字体样式：粗体、大小
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            //设置背景色
            cellStyle.setFillForegroundColor(lstColor.get(0));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (choose == 1) {
            //设置数据显示字体样式：大小
            font.setFontHeightInPoints((short) 10);
            //设置背景色
            cellStyle.setFillForegroundColor(lstColor.get(1));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (choose == 2) {
            //设置数据显示字体样式：大小
            font.setFontHeightInPoints((short) 10);
        }
        //设置字体: 微软雅黑
        font.setFontName("Microsoft YaHei");
        cellStyle.setFont(font);
        //设置居中：垂直居中、水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置边框宽度
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        //设置边框颜色
        cellStyle.setTopBorderColor(lstColor.get(2));
        cellStyle.setBottomBorderColor(lstColor.get(2));
        cellStyle.setLeftBorderColor(lstColor.get(2));
        cellStyle.setRightBorderColor(lstColor.get(2));
        return cellStyle;
    }

}
