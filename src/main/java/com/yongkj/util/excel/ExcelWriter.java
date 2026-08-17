package com.yongkj.util.excel;

import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private ExcelWriter() {
    }

    public static void writePicture(SXSSFSheet sheet, int rowIndex, int rowOffset, int colIndex, int colOffset, String filePath) throws Exception {
        //图片位置偏移：XSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2)
        //rowOffset 作用到行、colOffset 作用到列（原实现二者写反）
        XSSFClientAnchor anchor = new XSSFClientAnchor(
                28000, 28000, -20000, -20000,
                colIndex, rowIndex, colIndex + colOffset, rowIndex + rowOffset
        );
        //图片数据（用后即关，避免 FileInputStream 泄漏）
        byte[] pictureData;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            pictureData = IOUtils.toByteArray(fis);
        }
        int index = filePath.lastIndexOf(".");
        String suffix = filePath.substring(index + 1);
        //每个 sheet 各自持有绘制对象，避免跨 sheet / 跨 workbook 串用
        SXSSFDrawing drawing = ExcelContext.of(sheet.getWorkbook()).getOrCreateDrawing(sheet);
        drawing.createPicture(anchor, sheet.getWorkbook().addPicture(
                pictureData,
                "png".equals(suffix) ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG
        ));
    }

    public static void writeData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, List<Map<Integer, Object>> lstData) {
        if (lstData == null || lstData.isEmpty()) {
            return;
        }
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        int dataRow = rowIndex;
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置单元格样式
                setCellStyle(lstCellStyle, dataRow, cell);
                setRowHeight(cell.getRow(), colIndex);
            }
        }
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int rowIndex, int colIndex, Object cellData) {
        String value = GenUtil.objToStr(cellData);
        SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, value);
        //记录列宽（写盘时统一应用）
        ExcelHeader.setWidthColByAuto(sheet, colIndex, value);
        //设置单元格样式
        setCellStyle(lstCellStyle, ExcelContext.of(sheet.getWorkbook()).getDataStartRow(), cell);
        setRowHeight(cell.getRow(), colIndex);
    }

    public static void writeData(SXSSFSheet sheet, List<Map<Integer, Object>> lstData) {
        if (lstData == null || lstData.isEmpty()) {
            return;
        }
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        List<CellStyle> lstCellStyle = ctx.getOrCreateLstCellStyle(sheet);
        int dataRow = ctx.getDataStartRow();
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                String value = GenUtil.objToStr(mapData.get(colIndex));
                SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, value);
                //记录列宽
                ExcelHeader.setWidthColByAuto(sheet, colIndex, value);
                //设置单元格样式
                setCellStyle(lstCellStyle, dataRow, cell);
                setRowHeight(cell.getRow(), colIndex);
            }
        }
    }

    public static void writeRowData(SXSSFSheet sheet, Map<Integer, Object> mapData) {
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        List<CellStyle> lstCellStyle = ctx.getOrCreateLstCellStyle(sheet);
        int dataRow = ctx.getDataStartRow();
        int rowIndex = sheet.getLastRowNum() + 1;
        for (int colIndex = 0; colIndex < mapData.size(); colIndex++) {
            String value = GenUtil.objToStr(mapData.get(colIndex));
            SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, value);
            //记录列宽
            ExcelHeader.setWidthColByAuto(sheet, colIndex, value);
            //设置单元格样式
            setCellStyle(lstCellStyle, dataRow, cell);
            setRowHeight(cell.getRow(), colIndex);
        }
    }

    public static void writeCellData(SXSSFSheet sheet, int rowIndex, int colIndex, Object cellData) {
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        String value = GenUtil.objToStr(cellData);
        SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, value);
        //记录列宽
        ExcelHeader.setWidthColByAuto(sheet, colIndex, value);
        //设置单元格样式
        setCellStyle(ctx.getOrCreateLstCellStyle(sheet), ctx.getDataStartRow(), cell);
        setRowHeight(cell.getRow(), colIndex);
    }

    public static void writePartialData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, List<Map<Integer, Object>> lstData) {
        if (lstData == null || lstData.isEmpty()) {
            return;
        }
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置单元格样式
                setCellStyle(lstCellStyle, dataRow, cell);
                setRowHeight(cell.getRow(), colIndex);
            }
        }
    }

    public static void writeRowData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, Map<Integer, Object> mapData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (int colIndex = 0; colIndex < mapData.size(); colIndex++) {
            SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
            //设置单元格样式
            setCellStyle(lstCellStyle, dataRow, cell);
            setRowHeight(cell.getRow(), colIndex);
        }
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        SXSSFCell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
        setRowHeight(cell.getRow(), colIndex);
    }

    public static void writeCellData(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int colIndex, Object cellData) {
        String value = GenUtil.objToStr(cellData);
        //记录列宽
        changeSheetColWidth(row, colIndex, value);
        //写入单元格数据
        SXSSFCell cell = setCellValue(row, colIndex, value);
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
        setRowHeight(row, colIndex);
    }

    public static void writeCellDataByRow(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        SXSSFCell cell = setCellValue(row, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, rowIndex, cell);
        setRowHeight(row, colIndex);
    }

    public static void writeCellData(SXSSFCell cell, List<CellStyle> lstCellStyle, int dataRow, Object cellData) {
        cell.setCellValue(GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
    }

    public static boolean write(SXSSFWorkbook workbook, String fileName) {
        try {
            saveSheetColWidth(workbook);
            try (FileOutputStream fos = new FileOutputStream(
                    Arrays.asList("/", "\\").contains(fileName.substring(0, 1)) ?
                            FileUtil.getAbsPath(false, "src", "main", "resources", fileName) : fileName)) {
                workbook.write(fos);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 每行只需在首列设置一次行高。 */
    private static void setRowHeight(Row row, int colIndex) {
        if (colIndex == 0) {
            row.setHeight(ExcelHeader.ROW_HEIGHT);
        }
    }

    private static void saveSheetColWidth(SXSSFWorkbook workbook) {
        ExcelContext ctx = ExcelContext.of(workbook);
        Map<String, Map<Integer, Integer>> mapSheetColWidth = ctx.getMapSheetColWidth();
        if (mapSheetColWidth.isEmpty()) {
            return;
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            SXSSFSheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            Map<Integer, Integer> mapColWidth = mapSheetColWidth.get(sheetName);
            if (mapColWidth == null) {
                continue;
            }
            ExcelHeader.updateColWidths(sheet, mapColWidth);
        }
        ctx.clearSheetColWidth();
    }

    private static void changeSheetColWidth(SXSSFRow row, int colIndex, String value) {
        SXSSFSheet sheet = row.getSheet();
        ExcelContext ctx = ExcelContext.of(sheet.getWorkbook());
        Map<Integer, Integer> mapColWidth = ctx.getOrCreateSheetColWidth(sheet);
        ExcelHeader.updateColWidth(mapColWidth, colIndex, value);
    }

    public static SXSSFCell setCellValue(SXSSFSheet sheet, int rowIndex, int colIndex, String value) {
        SXSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        SXSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value);

        return cell;
    }

    public static SXSSFCell setCellValue(SXSSFRow row, int colIndex, String value) {
        SXSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value);

        return cell;
    }

    public static XSSFCell setCellValue(XSSFSheet sheet, int rowIndex, int colIndex, String value) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        XSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value);

        return cell;
    }

    public static void setCellStyle(List<CellStyle> lstCellStyle, int dataRow, Cell cell) {
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

    private static void setCellStyle(List<CellStyle> lstCellStyle, int dataRow, int rowIndex, Cell cell) {
        //设置行单元格样式：带斑马纹表格
        if (dataRow % 2 == 0) {
            if (rowIndex % 2 == 0) {
                cell.setCellStyle(lstCellStyle.get(2));
            } else {
                cell.setCellStyle(lstCellStyle.get(1));
            }
        } else {
            if (rowIndex % 2 != 0) {
                cell.setCellStyle(lstCellStyle.get(2));
            } else {
                cell.setCellStyle(lstCellStyle.get(1));
            }
        }
    }

}
