package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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

    public static void closeAll(Manager manager) {
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
            if (manager.getConnection() != null) {
                manager.getConnection().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
