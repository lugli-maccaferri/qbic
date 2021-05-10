package com.github.luglimaccaferri.qbic.data.mysql;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

public class Connector {

    public static Connection connection = null;

    private Connector(JsonObject mysqlConfig) throws ExecutionException, InterruptedException {

        connection = MySQLConnectionBuilder.createConnectionPool(
                "jdbc:mysql://"
                        + mysqlConfig.get("host").getAsString()
                        + ":" + mysqlConfig.get("port").getAsShort()
                        + "/" + mysqlConfig.get("database").getAsString()
                        + "?user=" + mysqlConfig.get("username").getAsString()
                        + "&password=" + mysqlConfig.get("password").getAsString()
        );
        connection.connect().get();
    }

    public static Connector connect(JsonObject mysqlConfig) throws ExecutionException, InterruptedException {
        if(connection == null) return new Connector(mysqlConfig);
        throw new Error("mysql connection already initialized");
    }

}
