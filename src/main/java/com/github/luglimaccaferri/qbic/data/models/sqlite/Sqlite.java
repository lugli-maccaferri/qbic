package com.github.luglimaccaferri.qbic.data.models.sqlite;

import com.github.luglimaccaferri.qbic.Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sqlite {

    private static boolean initialized = false;
    private static Connection connection;

    private Sqlite(){}

    public static Connection getConnection(){ return connection; } // meglio condividere una connessione a sqlite per tutti i thread
    // tanto sqlite usa i lock a livello di fs, quindi in ogni caso fare tante connessioni non cambierebbe nulla

    public static void init() throws SQLException {

        if(initialized) return;

        String url = String.format("jdbc:sqlite:%s", Core.SQLITE_PATH);
        connection = DriverManager.getConnection(url);
        connection.setAutoCommit(false);
        initialized = true;

    }

}
