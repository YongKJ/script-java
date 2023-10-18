package com.yongkj.applet.dataMigration.util;

import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.dto.Manager;

import java.sql.DriverManager;

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
            if (manager.getResultSet() != null) {
                manager.getResultSet().close();
            }
            if (manager.getPreparedStatement() != null) {
                manager.getPreparedStatement().close();
            }
            if (manager.getStatement() != null) {
                manager.getStatement().close();
            }
            if (manager.getConnection() != null) {
                manager.getConnection().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
