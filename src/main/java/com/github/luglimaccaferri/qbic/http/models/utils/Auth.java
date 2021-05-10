package com.github.luglimaccaferri.qbic.http.models.utils;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.luglimaccaferri.qbic.data.mysql.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Auth {

    private Auth(){} // classe di metodi util

    public static void login(String username, String password){

        Connection conn = Connector.connection;
        CompletableFuture<QueryResult> loginQuery = conn.sendPreparedStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?",
                List.of(username, password)
        );

    }

}
