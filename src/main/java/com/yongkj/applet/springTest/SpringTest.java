package com.yongkj.applet.springTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringTest {

    public static void run(String[] args) {
        try {
            System.setProperty("spring.profiles.default", "spring-test");
            SpringApplication.run(SpringTest.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
