package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.dto.Database;
import com.yongkj.applet.dataMigration.pojo.po.Field;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.applet.dataMigration.util.SQLUtil;
import com.yongkj.pojo.dto.Log;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FieldIncrementMigrationService extends BaseService {

    private final boolean tableDelete;
    private final boolean fieldDelete;
    private final List<String> tableNames;

    public FieldIncrementMigrationService(Database srcDatabase, Database desDatabase) {
        super(srcDatabase, desDatabase);
        this.tableNames = GenUtil.getList("table-names");
        this.tableDelete = GenUtil.getBoolean("table-delete");
        this.fieldDelete = GenUtil.getBoolean("field-delete");
    }

    public void apply() {
        for (String tableName : tableNames) {
            compareAndMigrationTable(tableName);
        }
        SQLUtil.close(srcDatabase.getManager());
        SQLUtil.close(desDatabase.getManager());
    }

    private void compareAndMigrationTable(String tableName) {
        if (!tableDelete && !srcDatabase.getTableNames().contains(tableName)) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "compareAndMigrationTable", "error", "table delete not allowed!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        Table srcTable = srcDatabase.getMapTable().get(tableName);
        Table desTable = desDatabase.getMapTable().get(tableName);
        String tableOperate = desTable == null ? "create" : "update";
        if (tableDelete && srcTable == null) {
            tableOperate = "delete";
        }
        switch (tableOperate) {
            case "create":
                createDesTable(srcTable);
                break;
            case "update":
                updateDesTable(srcTable, desTable);
                break;
            case "delete":
                deleteDesTable(desTable);
                break;
            default:
        }
    }

    private void updateDesTable(Table srcTable, Table desTable) {
        if (srcTable == null || desTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesTable", "error", "srcTable or desTable not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        Set<String> keyAndIndexes = new HashSet<>();
        for (String fieldName : srcTable.getFieldNames()) {
            Field srcField = srcTable.getMapField().get(fieldName);
            Field desField = desTable.getMapField().get(fieldName);
            compareAndMigrationField(srcField, desField, keyAndIndexes);
        }
        createKeyOrIndexes(keyAndIndexes);
        for (String fieldName : desTable.getFieldNames()) {
            Field srcField = srcTable.getMapField().get(fieldName);
            Field desField = desTable.getMapField().get(fieldName);
            if (srcField != null) continue;
            if (!fieldDelete) {
                LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesTable", "error", "field delete not allowed!"));
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
                break;
            }
            deleteDesField(desField);
        }
    }

    private void createKeyOrIndexes(Set<String> keyAndIndexes) {
        if (keyAndIndexes.isEmpty()) return;
        try {
            for (String keyAndIndex : keyAndIndexes) {
                LogUtil.loggerLine(Log.of("IncrementMigrationService", "createKeyOrIndexes", "keyAndIndex", keyAndIndex));

                Statement statement = desDatabase.getManager().getConnection().createStatement();
                boolean sqlResult = statement.execute(keyAndIndex);

                LogUtil.loggerLine(Log.of("IncrementMigrationService", "createKeyOrIndexes", "sqlResult", sqlResult));
                LogUtil.loggerLine(Log.of("IncrementMigrationService", "createKeyOrIndexes", "success", "createKeyOrIndexes success!"));
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

                desDatabase.getManager().setStatement(statement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareAndMigrationField(Field srcField, Field desField, Set<String> keyAndIndexes) {
        String fieldOperate = desField == null ? "create" :
                !fieldTypeCompare(srcField, desField) ? "update" : "";
        switch (fieldOperate) {
            case "create":
                createDesField(srcField, keyAndIndexes);
                break;
            case "update":
                updateDesField(srcField);
                break;
            default:
        }
    }

    private void deleteDesField(Field desField) {
        if (desField == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesField", "error", "desField not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesField", "desField.getDeleteSql()", desField.getDeleteSql()));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(desField.getDeleteSql());

            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesField", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesField", "success", "createDesTable success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDesField(Field srcField) {
        if (srcField == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesField", "error", "srcField not exist!"));
            return;
        }
        try {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesField", "srcField.getModifySql()", srcField.getModifySql()));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(srcField.getModifySql());

            LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesField", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "updateDesField", "success", "createDesTable success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDesField(Field srcField, Set<String> keyAndIndexes) {
        if (srcField == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesField", "error", "srcField not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesField", "srcField.getCreateSql()", srcField.getCreateSql()));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(srcField.getCreateSql());
            keyAndIndexes.addAll(srcField.getIndexes());
            keyAndIndexes.addAll(srcField.getKeys());

            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesField", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesField", "success", "createDesTable success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean fieldTypeCompare(Field srcField, Field desField) {
        return Objects.equals(srcField.getType(), desField.getType()) &&
                Objects.equals(srcField.getLength(), desField.getLength()) &&
                Objects.equals(srcField.getNotNull(), desField.getNotNull()) &&
                Objects.equals(srcField.getDefaultValue(), desField.getDefaultValue()) &&
                Objects.equals(srcField.getComment(), desField.getComment()) &&
                Objects.equals(srcField.getBeforeField(), desField.getBeforeField());
    }

    private void deleteDesTable(Table desTable) {
        if (desTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "error", "desTable not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "desTable.getDeleteSql()", desTable.getDeleteSql()));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(desTable.getDeleteSql());

            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "deleteDesTable", "success", "deleteDesTable success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDesTable(Table srcTable) {
        if (srcTable == null) {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "error", "srcTable not exist!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            return;
        }
        try {
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "srcTable.getCreateSql()", srcTable.getCreateSql()));

            Statement statement = desDatabase.getManager().getConnection().createStatement();
            boolean sqlResult = statement.execute(srcTable.getCreateSql());

            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "sqlResult", sqlResult));
            LogUtil.loggerLine(Log.of("IncrementMigrationService", "createDesTable", "success", "createDesTable success!"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

            desDatabase.getManager().setStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
