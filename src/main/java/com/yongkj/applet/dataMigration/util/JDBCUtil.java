package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;
import com.yongkj.applet.dataMigration.pojo.po.Field;
import com.yongkj.applet.dataMigration.pojo.po.Table;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JDBCUtil {

    public static Manager getConnection(Database database) {
        if (database == null) {
            return new Manager();
        }
        try {
            Class.forName(database.getDriver());
            return Manager.get(DriverManager.getConnection(
                    database.getUrl(),
                    database.getUsername(),
                    database.getPassword()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return new Manager();
        }
    }

    public static boolean getResult(Database database, String sql) {
        boolean result = false;
        Statement statement = null;
        try {
            statement = database.getManager().getConnection().createStatement();
            result = statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(null, statement);
        }
        return result;
    }

    public static List<Map<String, Object>> getResultSet(Database database, Table table, String sql) {
        Statement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> lstData = new ArrayList<>();
        try {
            statement = database.getManager().getConnection().createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                lstData.add(getRowData(table, resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(resultSet, statement);
        }
        return lstData;
    }

    private static Map<String, Object> getRowData(Table table, ResultSet resultSet) {
        Map<String, Object> mapData = new LinkedHashMap<>();
        List<String> subFieldNames = table.getSubFieldNames();
        for (String fieldName : table.getFieldNames()) {
            Field field = table.getMapField().get(fieldName);
            if (field == null || subFieldNames.size() > 0 && !subFieldNames.contains(fieldName)) continue;
            try {
                Object data;
                switch (field.getType()) {
                    case "INT":
                    case "TINYINT":
                        data = Integer.valueOf(resultSet.getInt(fieldName));
                        break;
                    case "BIGINT":
                        data = Long.valueOf(resultSet.getLong(fieldName));
                        break;
                    case "DOUBLE":
                    case "DECIMAL":
                        data = Double.valueOf(resultSet.getDouble(fieldName));
                        break;
                    default:
                        data = resultSet.getString(fieldName);
                }
                mapData.put(fieldName, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mapData;
    }

    public static void close(ResultSet resultSet, PreparedStatement preparedStatement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet resultSet, Statement statement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(Manager manager) {
        try {
            for (ResultSet resultSet : manager.getResultSets()) {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
            for (PreparedStatement preparedStatement : manager.getPreparedStatements()) {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            for (Statement statement : manager.getStatements()) {
                if (statement != null) {
                    statement.close();
                }
            }
            manager.setResultSets(new ArrayList<>());
            manager.setStatements(new ArrayList<>());
            manager.setPreparedStatements(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeAll(Manager manager) {
        try {
            close(manager);
            if (manager.getConnection() != null) {
                manager.getConnection().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}