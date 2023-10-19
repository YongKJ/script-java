package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLUtil {

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
