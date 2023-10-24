package com.yongkj.util.excel;

import com.yongkj.util.GenUtil;
import com.yongkj.util.PoiExcelUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private ExcelWriter() {
    }

    public static void writePicture(SXSSFSheet sheet, int rowIndex, int rowOffset, int colIndex, int colOffset, String filePath) throws Exception {
        if (PoiExcelUtil.getDrawing() == null) {
            PoiExcelUtil.setDrawing(sheet.createDrawingPatriarch());
        }
        //图片位置偏移
        XSSFClientAnchor anchor = new XSSFClientAnchor(
                28000, 28000, -20000, -20000,
                colIndex, rowIndex, colIndex + rowOffset, rowIndex + colOffset
        );
        //图片写入
        int index = filePath.lastIndexOf(".");
        String suffix = filePath.substring(index + 1);
        PoiExcelUtil.getDrawing().createPicture(anchor, sheet.getWorkbook().addPicture(
                IOUtils.toByteArray(new FileInputStream(filePath)),
                "png".equals(suffix) ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG
        ));
    }

    public static void writeData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, List<Map<Integer, Object>> lstData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        int dataRow = rowIndex;
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置单元格样式
                setCellStyle(lstCellStyle, dataRow, sheet.getRow(rowIndex).getCell(colIndex));
                if (colIndex == 0) {
                    //设置行高
                    sheet.getRow(rowIndex).setHeightInPoints(180);
                    sheet.getRow(rowIndex).setHeight((short) (4 * 180));
                }
            }
        }
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置列宽
        ExcelHeader.setWidthColByAuto(sheet, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        ExcelWriter.setCellStyle(lstCellStyle, PoiExcelUtil.getDataRow(), sheet.getRow(rowIndex).getCell(colIndex));
        if (colIndex == 0) {
            //设置行高
            sheet.getRow(rowIndex).setHeightInPoints(180);
            sheet.getRow(rowIndex).setHeight((short) (4 * 180));
        }
    }

    public static void writeData(SXSSFSheet sheet, List<Map<Integer, Object>> lstData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                ExcelWriter.setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置列宽
                ExcelHeader.setWidthColByAuto(sheet, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置单元格样式
                ExcelWriter.setCellStyle(PoiExcelUtil.getLstCellStyle(), PoiExcelUtil.getDataRow(), sheet.getRow(rowIndex).getCell(colIndex));
                if (colIndex == 0) {
                    //设置行高
                    sheet.getRow(rowIndex).setHeightInPoints(180);
                    sheet.getRow(rowIndex).setHeight((short) (4 * 180));
                }
            }
        }
    }

    public static void writeRowData(SXSSFSheet sheet, Map<Integer, Object> mapData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (int colIndex = 0; colIndex < mapData.size(); colIndex++) {
            ExcelWriter.setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
            //设置列宽
            ExcelHeader.setWidthColByAuto(sheet, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
            //设置单元格样式
            ExcelWriter.setCellStyle(PoiExcelUtil.getLstCellStyle(), PoiExcelUtil.getDataRow(), sheet.getRow(rowIndex).getCell(colIndex));
            if (colIndex == 0) {
                //设置行高
                sheet.getRow(rowIndex).setHeightInPoints(180);
                sheet.getRow(rowIndex).setHeight((short) (4 * 180));
            }
        }
    }

    public static void writeCellData(SXSSFSheet sheet, int rowIndex, int colIndex, Object cellData) {
        ExcelWriter.setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置列宽
        ExcelHeader.setWidthColByAuto(sheet, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        ExcelWriter.setCellStyle(PoiExcelUtil.getLstCellStyle(), PoiExcelUtil.getDataRow(), sheet.getRow(rowIndex).getCell(colIndex));
        if (colIndex == 0) {
            //设置行高
            sheet.getRow(rowIndex).setHeightInPoints(180);
            sheet.getRow(rowIndex).setHeight((short) (4 * 180));
        }
    }

    public static void writePartialData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, List<Map<Integer, Object>> lstData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        int colSize = lstData.get(0).size();
        for (int i = 0; i < lstData.size(); i++, rowIndex++) {
            Map<Integer, Object> mapData = lstData.get(i);
            for (int colIndex = 0; colIndex < colSize; colIndex++) {
                setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
                //设置单元格样式
                setCellStyle(lstCellStyle, dataRow, sheet.getRow(rowIndex).getCell(colIndex));
                if (colIndex == 0) {
                    //设置行高
                    sheet.getRow(rowIndex).setHeightInPoints(180);
                    sheet.getRow(rowIndex).setHeight((short) (4 * 180));
                }
            }
        }
    }

    public static void writeRowData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, Map<Integer, Object> mapData) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (int colIndex = 0; colIndex < mapData.size(); colIndex++) {
            setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(mapData.get(colIndex)));
            //设置单元格样式
            setCellStyle(lstCellStyle, dataRow, sheet.getRow(rowIndex).getCell(colIndex));
            if (colIndex == 0) {
                //设置行高
                sheet.getRow(rowIndex).setHeightInPoints(180);
                sheet.getRow(rowIndex).setHeight((short) (4 * 180));
            }
        }
    }

    public static void writeCellData(SXSSFSheet sheet, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        Cell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
        if (colIndex == 0) {
            //设置行高
            sheet.getRow(rowIndex).setHeightInPoints(180);
            sheet.getRow(rowIndex).setHeight((short) (4 * 180));
        }
    }

    public static void writeCellData(SXSSFSheet sheet, int colIndex, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, Object cellData) {
        Cell cell = setCellValue(sheet, rowIndex, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
        if (colIndex == 0) {
            //设置行高
            sheet.getRow(rowIndex).setHeightInPoints(180);
            sheet.getRow(rowIndex).setHeight((short) (4 * 180));
        }
    }

    public static void writeCellData(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int colIndex, Object cellData) {
        SXSSFCell cell = setCellValue(row, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
//        setCellStyle(row.getRowNum(), cell, lstCellStyle, dataRow);
        if (colIndex == 0) {
            //设置行高
            row.setHeightInPoints(180);
            row.setHeight((short) (4 * 180));
        }
    }

    public static void writeCellDataByRow(SXSSFRow row, List<CellStyle> lstCellStyle, int dataRow, int rowIndex, int colIndex, Object cellData) {
        SXSSFCell cell = setCellValue(row, colIndex, GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, rowIndex, cell);
        if (colIndex == 0) {
            //设置行高
            row.setHeightInPoints(180);
            row.setHeight((short) (4 * 180));
        }
    }

    public static void writeCellData(SXSSFCell cell, List<CellStyle> lstCellStyle, int dataRow, Object cellData) {
        cell.setCellValue(GenUtil.objToStr(cellData));
        //设置单元格样式
        setCellStyle(lstCellStyle, dataRow, cell);
    }


    public static void write(SXSSFWorkbook workbook, String fileName) {
        try {
            workbook.write(new FileOutputStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static void setCellStyle(int row, Cell cell, List<CellStyle> lstCellStyle, int dataRow) {
        //设置行单元格样式：带斑马纹表格
        if (dataRow % 2 == 0) {
            if (row % 2 == 0) {
                cell.setCellStyle(lstCellStyle.get(2));
            } else {
                cell.setCellStyle(lstCellStyle.get(1));
            }
        } else {
            if (row % 2 != 0) {
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
