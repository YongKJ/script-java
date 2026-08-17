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

public class ExcelHeaderByWidthColAndCellStyle {

    private ExcelHeaderByWidthColAndCellStyle() {
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
                        ExcelHeader.checkMergeRange(lstHeader, lstFlag, lstCoords, row, col, lstHeader.get(col).get(row));
                    }

                    merge(sheet, lstCoords, lstCellStyle, widthCol);
                }
            }
        }
        //单元格冻结：从上往下，冻结 dataRow 行；从左往右，冻结 dataCol 列
        sheet.createFreezePane(dataCol, dataRow, dataCol, dataRow);
    }

    private static void merge(SXSSFSheet sheet, List<Coords> lstCoords, List<CellStyle> lstCellStyle, int widthCol) {
        //一次 O(n) 遍历求最小/最大坐标（替代原先非传递性的全量排序）
        Coords minCoords = ExcelHeader.minCoords(lstCoords);
        Coords maxCoords = ExcelHeader.maxCoords(lstCoords);
        //表头数据写入到最小坐标的单元格中
        ExcelWriter.setCellValue(sheet, minCoords.getX(), minCoords.getY(), minCoords.getValue());
        for (Coords coords : lstCoords) {
            //设置固定列宽
            sheet.setColumnWidth(coords.getY(), widthCol * 256);
            //设置行高
            Row row = CellUtil.getRow(coords.getX(), sheet);
            row.setHeight(ExcelHeader.ROW_HEIGHT);
            //设置单元格样式
            Cell cell = CellUtil.getCell(row, coords.getY());
            cell.setCellStyle(lstCellStyle.get(0));
        }
        //合并单元格
        if (lstCoords.size() > 1) {
            sheet.addMergedRegion(new CellRangeAddress(minCoords.getX(), maxCoords.getX(), minCoords.getY(), maxCoords.getY()));
        }
    }

}
