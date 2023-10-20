package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.util.GenUtil;

import java.util.HashMap;
import java.util.Map;

public class Database {

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Manager manager;
    private Map<String, Table> mapTable;

    private Database() {
    }

    private Database(String name, String driver, String url, String username, String password) {
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.manager = new Manager();
        this.mapTable = new HashMap<>();
    }

    public static Database of(String name, String driver, String url, String username, String password) {
        return new Database(name, driver, url, username, password);
    }

    public static Database get(String key) {
        Object value = GenUtil.getObject(key);
        if (!(value instanceof Map)) {
            return new Database();
        }
        Object mapDatabase = ((Map<String, Object>) value).get("database");
        if (!(mapDatabase instanceof Map)) {
            return new Database();
        }
        String name = GenUtil.objToStr(((Map<String, Object>) mapDatabase).get("name"));
        String driver = GenUtil.objToStr(((Map<String, Object>) mapDatabase).get("driver"));
        String url = GenUtil.objToStr(((Map<String, Object>) mapDatabase).get("url"));
        String username = GenUtil.objToStr(((Map<String, Object>) mapDatabase).get("username"));
        String password = GenUtil.objToStr(((Map<String, Object>) mapDatabase).get("password"));
        Database database = new Database(name, driver, url, username, password);
        database.setManager(SQLUtil.getConnection(database));
        database.setMapTable(Table.getTables(database.getManager(), database.getName()));
        return database;
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

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Map<String, Table> getMapTable() {
        return mapTable;
    }

    public void setMapTable(Map<String, Table> mapTable) {
        this.mapTable = mapTable;
    }
}
