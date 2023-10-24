package com.yongkj.pojo.dto;

import java.util.List;

public class Coords {

    private String value;
    private Integer x;
    private Integer y;

    private Coords(List<List<String>> lstHeader, int row, int col) {
        this.x = row;
        this.y = col;
        this.value = lstHeader.get(col).get(row);
    }

    public static Coords of(List<List<String>> lstHeader, int row, int col) {
        return new Coords(lstHeader, row, col);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
