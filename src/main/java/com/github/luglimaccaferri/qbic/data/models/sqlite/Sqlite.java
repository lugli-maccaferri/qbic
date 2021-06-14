package com.github.luglimaccaferri.qbic.data.models.sqlite;

import com.github.luglimaccaferri.qbic.Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sqlite {

    private static boolean initialized = false;
    private static Connection connection;

    private Sqlite(){}

    public static Connection getConnection(){ return connection; }

    public static void init() throws SQLException {

        if(initialized) return;

        String url = String.format("jdbc:sqlite:%s", Core.SQLITE_PATH);
        connection = DriverManager.getConnection(url);
        initialized = true;

    }

}
