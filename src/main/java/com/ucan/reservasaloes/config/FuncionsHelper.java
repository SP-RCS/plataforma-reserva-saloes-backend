package com.ucan.reservasaloes.config;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

public class FuncionsHelper {
    
    public static String getCellAsString(Cell cell) {

        if (cell == null) {
            return "";
        }
        
        CellType cellType = cell.getCellType();

        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }
        
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value) && !Double.isInfinite(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

    public static Double getCellAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            CellType cellType = cell.getCellType();
            
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            
            if (cellType == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cellType == CellType.STRING) {
                String stringValue = cell.getStringCellValue().trim();
                if (!stringValue.isEmpty()) {
                    return Double.parseDouble(stringValue);
                }
            }
        } catch (Exception e) {
           
        }
        
        return null;
    }
}
