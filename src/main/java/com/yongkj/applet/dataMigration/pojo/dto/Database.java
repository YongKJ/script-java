package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.util.GenUtil;

import java.util.Map;

public class Database {

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;

    private Database() {
    }

    private Database(String name, String driver, String url, String username, String password) {
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static Database of(String name, String driver, String url, String username, String password) {
        return new Database(name, driver, url, username, password);
    }

    public static Database get(String key) {
        Object value = GenUtil.getObject(key);
        if (!(value instanceof Map)) {
            return new Database();
        }
        Object database = ((Map<String, Object>) value).get("database");
        if (!(database instanceof Map)) {
            return new Database();
        }
        String name = GenUtil.objToStr(((Map<String, Object>) database).get("name"));
        String driver = GenUtil.objToStr(((Map<String, Object>) database).get("driver"));
        String url = GenUtil.objToStr(((Map<String, Object>) database).get("url"));
        String username = GenUtil.objToStr(((Map<String, Object>) database).get("username"));
        String password = GenUtil.objToStr(((Map<String, Object>) database).get("password"));
        return new Database(name, driver, url, username, password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
