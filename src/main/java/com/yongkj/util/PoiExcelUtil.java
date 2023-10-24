package com.yongkj.util;

import com.yongkj.pojo.dto.Coords;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PoiExcelUtil {

    private static final int[][] MOVE = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private static List<CellStyle> lstCellStyle;
    private static SXSSFDrawing drawing;
    private static int dataRow = 1;

    private PoiExcelUtil() {
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, dataRow, widthCol, null);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, int dataRow, int widthCol, List<Integer> lstExcludeRow) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        boolean[][] lstFlag = new boolean[rowSize][colSize];
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

                    merge(sheet, lstCoords, lstCellStyle, widthCol);
                }
            }
        }
        //单元格冻结：从上往下，冻结 dataRow 行；从左往右，冻结 dataCol 列
        sheet.createFreezePane(dataCol, dataRow, dataCol, dataRow);
    }

    private static void checkMergeRange(List<List<String>> lstHeader, boolean[][] lstFlag, List<Coords> lstCoords, int x, int y, String value) {
        int colSize = lstHeader.size();
        int rowSize = lstHeader.get(0).size();
        for (int[] move : MOVE) {
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

    private static void merge(SXSSFSheet sheet, List<Coords> lstCoords, List<CellStyle> lstCellStyle, int widthCol) {
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
        GenUtil.setCellValue(sheet, lstCoords.get(0).getX(), lstCoords.get(0).getY(), lstCoords.get(0).getValue());
        for (Coords coords : lstCoords) {
            //设置列宽
            sheet.setColumnWidth(coords.getY(), widthCol * 256);
            //设置行高
            Row row = CellUtil.getRow(coords.getX(), sheet);
            row.setHeightInPoints(180);
            row.setHeight((short) (4 * 180));
            //设置单元格样式
            Cell cell = CellUtil.getCell(row, coords.getY());
            cell.setCellStyle(lstCellStyle.get(0));
        }
        //合并单元格
        if (lstCoords.size() > 1) {
            Coords minCoords = lstCoords.get(0);
            Coords maxCoords = lstCoords.get(lstCoords.size() - 1);
            sheet.addMergedRegion(new CellRangeAddress(minCoords.getX(), maxCoords.getX(), minCoords.getY(), maxCoords.getY()));
        }
    }

    public static void writeCellData(SXSSFSheet sheet, int rowIndex, int colIndex, List<CellStyle> lstCellStyle, int dataRow, Object cellData) {
        Cell cell = GenUtil.setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(cell, lstCellStyle, dataRow);
        if (colIndex == 0) {
            //设置行高
            sheet.getRow(rowIndex).setHeightInPoints(180);
            sheet.getRow(rowIndex).setHeight((short) (4 * 180));
        }
    }

    private static void setCellStyle(Cell cell, List<CellStyle> lstCellStyle, int dataRow) {
        //设置行单元格样式：带斑马纹表格
        if (dataRow % 2 == 0) {
            if (cell.getRowIndex() % 2 == 0) {
                cell.setCellStyle(lstCellStyle.get(2));
            } else {
                cell.setCellStyle(lstCellStyle.get(1));
            }
        } else {
            if (cell.getRowIndex() % 2 != 0) {
                cell.setCellStyle(lstCellStyle.get(2));
            } else {
                cell.setCellStyle(lstCellStyle.get(1));
            }
        }
    }

}
