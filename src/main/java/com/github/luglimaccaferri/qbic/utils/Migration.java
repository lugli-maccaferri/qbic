package com.github.luglimaccaferri.qbic.utils;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.models.sqlite.Sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class Migration {

    // TODO
    public static void createTable(String name, String[] data) throws SQLException {

        Connection conn = Sqlite.getConnection();
        Statement st = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS " + name + " (\n"; // è sempre hardcoded, quindi no problem per sql injection

        for(String col: data){

            sql = sql.concat(
                    col + "\n"
            );

        }

        sql += ")";

        Core.logger.warn("[migration] creating table " + name);

        conn.createStatement().execute(sql);
        conn.commit();

       // System.out.print(sql);

    }

}
