package com.yongkj.applet.dataMigration.pojo.dto;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.JDBCUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Database {

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Manager manager;
    List<String> tableNames;
    private boolean isMaxCompute;
    private boolean isPostGreSQl;
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
        this.isMaxCompute = false;
        this.isPostGreSQl = false;
        this.manager = new Manager();
        this.mapTable = new HashMap<>();
        this.tableNames = new ArrayList<>();
        this.databaseNames = new ArrayList<>();
    }

    public static Database of(String name, String driver, String url, String username, String password) {
        return new Database(name, driver, url, username, password);
    }

    public static Database get(String key, DataMigration dataMigration) {
        Map<String, Object> mapDatabaseConfig = GenUtil.getMap("database-config");
        String value = (String) mapDatabaseConfig.get(key);
        switch (value) {
            case "dev":
                return dataMigration.getDevDatabase();
            case "test":
                return dataMigration.getTestDatabase();
            case "pre":
                return dataMigration.getPreDatabase();
            case "prod":
                return dataMigration.getProdDatabase();
            default:
                return new Database();
        }
    }

    public static Map<String, Database> initMaxComputeMapDatabase() {
        Map<String, Database> mapDatabase = new HashMap<>();
        List<Map<String, Object>> lstData = GenUtil.getListMap("max-compute");
        for (Map<String, Object> mapData : lstData) {
            String databaseName = GenUtil.objToStr(mapData.get("name"));
            mapDatabase.put(databaseName, get(mapData));
        }
        return mapDatabase;
    }

    public static Map<String, Database> initMapDatabase() {
        Map<String, Database> mapDatabase = new HashMap<>();
        List<String> keys = Arrays.asList("dev", "test", "pre", "prod");
        Map<String, Object> mapDatabaseConfig = GenUtil.getMap("database-config");
        List<String> databases = (List<String>) mapDatabaseConfig.get("databases");
        if (databases.size() == 1) {
            return mapDatabase;
        }

        for (int i = 1; i < databases.size(); i++) {
            String database = databases.get(i);
            for (String key : keys) {
                String databaseName = key + "_" + database;
                mapDatabase.put(databaseName, get(key, databaseName));
            }
        }
        return mapDatabase;
    }

    public static Database get(String key, Map<String, Database> mapDatabases) {
        if (mapDatabases.containsKey(key)) {
            return mapDatabases.get(key);
        }
        Map<String, Object> mapDatabaseConfig = GenUtil.getMap("database-config");
        String databaseName = key + "_" + ((List<String>) mapDatabaseConfig.get("databases")).get(0);
        if (mapDatabases.containsKey(databaseName)) {
            return mapDatabases.get(databaseName);
        }
        mapDatabases.put(databaseName, get(key, databaseName));
        return mapDatabases.get(databaseName);
    }

    private static Database get(String key, String databaseName) {
        Object value = GenUtil.getObject(key);
        if (!(value instanceof List)) {
            return new Database();
        }
        Map<String, Object> mapDatabase = ((List<Map<String, Object>>) value).stream()
                .filter(po -> Objects.equals(po.get("name"), databaseName)).findFirst().orElse(new HashMap<>());
        if (mapDatabase.isEmpty()) {
            return new Database();
        }
        return get(mapDatabase);
    }

    private static Database get(Map<String, Object> mapDatabase) {
        String name = GenUtil.objToStr(mapDatabase.get("name"));
        LogUtil.loggerLine(Log.of("Database", "get", "name", name));

        String driver = GenUtil.objToStr(mapDatabase.get("driver"));
        String url = GenUtil.objToStr(mapDatabase.get("url"));
        String username = GenUtil.objToStr(mapDatabase.get("username"));
        String password = GenUtil.objToStr(mapDatabase.get("password"));
        Database database = new Database(name, driver, url, username, password);
        database.setMaxCompute(driver.endsWith("OdpsDriver"));
        database.setPostGreSQl(driver.contains("postgresql"));
        database.setManager(JDBCUtil.getConnection(database));
        database.setMapTable(Table.getTables(database.getManager()));
        database.setDatabaseNames(getDatabasesBySql(database.getManager()));
        database.setTableNames(Table.getTableNamesBySql(database.getManager()));
        return database;
    }

    public static List<String> getDatabasesBySql(Manager manager) {
        if (manager.isMaxCompute()) {
            return new ArrayList<>();
        }
        List<String> databases = new ArrayList<>();
        try {
            String sql = "SHOW DATABASES";
            if (manager.isPostGreSQl()) {
                sql = "SELECT datname FROM pg_database";
            }
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

    public boolean isPostGreSQl() {
        return isPostGreSQl;
    }

    public void setPostGreSQl(boolean postGreSQl) {
        isPostGreSQl = postGreSQl;
    }

    public boolean isMaxCompute() {
        return isMaxCompute;
    }

    public void setMaxCompute(boolean maxCompute) {
        isMaxCompute = maxCompute;
    }
}
