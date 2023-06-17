package com.yongkj.applet.springTest;

import com.yongkj.util.GenUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringTest {

    public static void run(String[] args) {
        try {
            System.setProperty("spring.profiles.default", GenUtil.getProfile());
            SpringApplication.run(SpringTest.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
