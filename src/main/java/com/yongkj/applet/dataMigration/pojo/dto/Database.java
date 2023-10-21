package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.util.GenUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Manager manager;
    List<String> tableNames;
    private List<String> databaseNames;
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
        this.tableNames = new ArrayList<>();
        this.databaseNames = new ArrayList<>();
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
        database.setMapTable(Table.getTables(database.getManager()));
        database.setDatabaseNames(getDatabasesBySql(database.getManager()));
        database.setTableNames(Table.getTableNamesBySql(database.getManager()));
        return database;
    }

    public static List<String> getDatabasesBySql(Manager manager) {
        List<String> databases = new ArrayList<>();
        try {
            String sql = "SHOW DATABASES";
            Statement statement = manager.getConnection().createStatement();
            ResultSet sqlResult = statement.executeQuery(sql);

            while (sqlResult.next()) {
                String database = sqlResult.getString(1);
                databases.add(database);
            }

            manager.setResultSet(sqlResult);
            manager.setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databases;
    }

    public static List<String> getDatabases(Manager manager) {
        List<String> databases = new ArrayList<>();
        try {
            DatabaseMetaData metaData = manager.getConnection().getMetaData();
            ResultSet resultSet = metaData.getCatalogs();

            while (resultSet.next()) {
                String database = resultSet.getString("TABLE_CAT");
                databases.add(database);
            }

            manager.setResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databases;
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

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public List<String> getDatabaseNames() {
        return databaseNames;
    }

    public void setDatabaseNames(List<String> databaseNames) {
        this.databaseNames = databaseNames;
    }

    public Map<String, Table> getMapTable() {
        return mapTable;
    }

    public void setMapTable(Map<String, Table> mapTable) {
        this.mapTable = mapTable;
    }
}
