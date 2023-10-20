package com.yongkj.applet.dataMigration.pojo.dto;

public class SQL {

    private static String TABLE_DELETE_SQL = "DROP TABLE IF EXISTS `%s`";
    private static String FIELD_CREATE_SQL = "ALTER TABLE `%s` ADD COLUMN `%s` %s %s DEFAULT %s %s %s `%s`";
    private static String FIELD_MODIFY_SQL = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s %s DEFAULT %s %s";
    private static String FIELD_DELETE_SQL;
    private static String DATA_INSERT_SQL;
    private static String DATA_UPDATE_SQL;
    private static String DATA_REMOVE_SQL;

    public static String getTableDeleteSql(String table) {
        return String.format(TABLE_DELETE_SQL, table);
    }

    public static String getFieldCreateSql(String table, String field, String type, String notNull, String defaultValue, String comment, String position, String positionField) {
        return String.format(FIELD_CREATE_SQL, table, field, type, notNull, defaultValue, comment, position, positionField);
    }

    public static String getFieldModifySql(String table, String field, String type, String notNull, String defaultValue, String comment) {
        return String.format(FIELD_MODIFY_SQL, table, field, type, notNull, defaultValue, comment);
    }

    public static String getFieldDeleteSql() {
        return FIELD_DELETE_SQL;
    }

    public static String getDataInsertSql() {
        return DATA_INSERT_SQL;
    }

    public static String getDataUpdateSql() {
        return DATA_UPDATE_SQL;
    }

    public static String getDataRemoveSql() {
        return DATA_REMOVE_SQL;
    }
}
