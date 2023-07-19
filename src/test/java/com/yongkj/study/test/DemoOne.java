package com.yongkj.study.test;

public class DemoOne {

    private static int getNumber(int num) {
        if (num == 1 || num == 2) {
            return 1;
        } else {
            return getNumber(num - 1) + getNumber(num - 2);
        }
    }

    public static void main(String[] args) {
        System.out.println(DemoOne.getNumber(30));
    }

}
