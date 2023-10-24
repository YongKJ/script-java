package com.yongkj.util.excel;

import com.yongkj.pojo.dto.Coords;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.ArrayList;
import java.util.List;

public class ExcelHeaderByCellStyle {

    private static int dataRow = 1;

    private ExcelHeaderByCellStyle() {
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle) {
        writeHeader(sheet, lstHeader, lstCellStyle, 0);
    }

    public static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol) {
        writeHeader(sheet, lstHeader, lstCellStyle, dataCol, null);
    }

    private static void writeHeader(SXSSFSheet sheet, List<List<String>> lstHeader, List<CellStyle> lstCellStyle, int dataCol, List<Integer> lstExcludeRow) {
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
                        ExcelHeader.checkMergeRange(lstHeader, lstFlag, lstCoords, row, col, lstHeader.get(col).get(row));
                    }

                    merge(sheet, lstCoords, lstCellStyle);
                }
            }
        }
        //单元格冻结：从上往下，冻结 dataRow 行；从左往右，冻结 dataCol 列
        sheet.createFreezePane(dataCol, dataRow, dataCol, dataRow);
    }

    private static void merge(SXSSFSheet sheet, List<Coords> lstCoords, List<CellStyle> lstCellStyle) {
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
        ExcelHeader.setCellValue(sheet, lstCoords.get(0).getX(), lstCoords.get(0).getY(), lstCoords.get(0).getValue());
        for (Coords coords : lstCoords) {
            //设置列宽
            ExcelHeader.setWidthColByAuto(sheet, coords.getY(), coords.getValue());
            //设置行高
            Row row = CellUtil.getRow(coords.getX(), sheet);
            row.setHeightInPoints(180);
            row.setHeight((short) (4 * 180));
            //设置单元格样式
            Cell cell = CellUtil.getCell(row, coords.getY());
            cell.setCellStyle(lstCellStyle.get(0));
            //设置数据行号
            if (coords.getX() + 1 > dataRow) {
                dataRow = coords.getX() + 1;
            }
        }
        //合并单元格
        if (lstCoords.size() > 1) {
            Coords minCoords = lstCoords.get(0);
            Coords maxCoords = lstCoords.get(lstCoords.size() - 1);
            sheet.addMergedRegion(new CellRangeAddress(minCoords.getX(), maxCoords.getX(), minCoords.getY(), maxCoords.getY()));
        }
    }

}
