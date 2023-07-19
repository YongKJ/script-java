package com.yongkj.study.test;

public class Demo extends Thread {

    private String ID;

    public Demo(String ID) {
        this.ID = ID;
    }

    @Override
    public void run() {
        System.out.print(this.ID);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread A = new Demo("A");
                Thread B = new Demo("B");
                Thread C = new Demo("C");
                A.start();
                A.join();
                B.start();
                B.join();
                C.start();
                C.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
