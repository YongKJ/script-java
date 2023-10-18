package com.yongkj.applet.dataMigration.pojo.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Manager {

    private Statement statement;
    private ResultSet resultSet;
    private Connection connection;
    private PreparedStatement preparedStatement;

    public Manager() {
    }

    private Manager(Connection connection) {
        this.statement = null;
        this.resultSet = null;
        this.connection = connection;
        this.preparedStatement = null;
    }

    public static Manager get(Connection connection) {
        return new Manager(connection);
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
}
