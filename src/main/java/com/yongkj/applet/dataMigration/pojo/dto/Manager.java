package com.yongkj.applet.dataMigration.pojo.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Manager {

    private Connection connection;
    private List<Statement> statements;
    private List<ResultSet> resultSets;
    private List<PreparedStatement> preparedStatements;

    public Manager() {
        this.connection = null;
        this.statements = new ArrayList<>();
        this.resultSets = new ArrayList<>();
        this.preparedStatements = new ArrayList<>();
    }

    private Manager(Connection connection) {
        this.connection = connection;
        this.statements = new ArrayList<>();
        this.resultSets = new ArrayList<>();
        this.preparedStatements = new ArrayList<>();
    }

    public static Manager get(Connection connection) {
        return new Manager(connection);
    }

    public void setStatement(Statement statement) {
        this.statements.add(statement);
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSets.add(resultSet);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatements.add(preparedStatement);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public List<ResultSet> getResultSets() {
        return resultSets;
    }

    public void setResultSets(List<ResultSet> resultSets) {
        this.resultSets = resultSets;
    }

    public List<PreparedStatement> getPreparedStatements() {
        return preparedStatements;
    }

    public void setPreparedStatements(List<PreparedStatement> preparedStatements) {
        this.preparedStatements = preparedStatements;
    }
}
