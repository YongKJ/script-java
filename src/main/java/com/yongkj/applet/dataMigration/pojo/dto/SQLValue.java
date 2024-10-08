package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.applet.dataMigration.pojo.dict.SQLOperate;
import com.yongkj.util.GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLValue {

    private Object value;
    private String fieldName;
    private SQLOperate operate;

    private SQLValue(SQLOperate operate) {
        this.value = "";
        this.fieldName = "";
        this.operate = operate;
    }

    private SQLValue(SQLOperate operate, Object value) {
        this.value = value;
        this.fieldName = "";
        this.operate = operate;
    }

    private SQLValue(String fieldName, SQLOperate operate, Object value) {
        this.value = value;
        this.operate = operate;
        this.fieldName = fieldName;
    }

    public static SQLValue of(SQLOperate operate) {
        return new SQLValue(operate);
    }

    public static SQLValue of(SQLOperate operate, Object value) {
        return new SQLValue(operate, value);
    }

    public static SQLValue of(String fieldName, SQLOperate operate, Object value) {
        return new SQLValue(fieldName, operate, value);
    }

    public String getSqlSegment() {
        List<Object> lstValue;
        switch (operate) {
            case and:
            case or:
                return operate.getValue();
            case andWrapper:
            case orWrapper:
            case having:
                return String.format(operate.getValue(), value);
            case limit:
            case orderByAsc:
            case orderByDesc:
                lstValue = (List<Object>) value;
                return String.format(operate.getValue(), getListValueStr(lstValue, false));
            case groupBy:
                lstValue = (List<Object>) value;
                return String.format(operate.getValue(), getListValueStr(lstValue, true));
            default:
                return fieldName.contains("`") || fieldName.contains(".") ?
                        String.format("%s %s", fieldName, getOperateValue()) :
                        String.format("`%s` %s", fieldName, getOperateValue());
        }
    }

    private String getOperateValue() {
        switch (operate) {
            case isNull:
            case isNotNUll:
                return operate.getValue();
            case like:
            case notLike:
            case likeLeft:
            case likeRight:
                return String.format(
                        operate.getValue(),
                        value instanceof String ? value : getValueStr(value)
                );
            case in:
            case notIn:
                List<Object> lstValue = (List<Object>) value;
                return String.format(
                        operate.getValue(),
                        getListValueStr(lstValue, false)
                );
            case between:
            case notBetween:
                Map<Integer, Object> mapValue = (Map<Integer, Object>) value;
                return String.format(
                        operate.getValue(),
                        getValueStr(mapValue.get(1)),
                        getValueStr(mapValue.get(2))
                );
            default:
                return String.format(
                        "%s %s",
                        operate.getValue(),
                        getValueStr(value)
                );
        }
    }

    private String getListValueStr(List<Object> lstValue, boolean isGroupBy) {
        List<String> lstValueStr = new ArrayList<>();
        for (Object value : lstValue) {
            if (isGroupBy) {
                lstValueStr.add(value.toString());
                continue;
            }
            lstValueStr.add(getValueStr(value));
        }
        return String.join(", ", lstValueStr);
    }

    private String getValueStr(Object value) {
        if (value == null) return "";
        if (value instanceof String) {
            return ((String) value).contains("`") || ((String) value).contains(".") ?
                    (String) value : String.format("'%s'", value);
        } else if (value instanceof List || value instanceof Map) {
            return String.format("'%s'", GenUtil.toJsonString(value));
        } else {
            return GenUtil.objToStr(value);
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public SQLOperate getOperate() {
        return operate;
    }

    public void setOperate(SQLOperate operate) {
        this.operate = operate;
    }
}
