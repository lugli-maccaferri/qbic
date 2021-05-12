package com.github.luglimaccaferri.qbic.http.models.misc;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.mysql.Connector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class User {

    protected final UUID uuid;
    protected String username;
    protected String hash;
    protected final HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();

    public User(UUID uuid){
        this.uuid = uuid;
    }
    public User fill() throws ExecutionException, InterruptedException {

        CompletableFuture<QueryResult> query = Connector.connection.sendPreparedStatement(
                "SELECT * FROM users WHERE uuid = ?",
                List.of(this.uuid.toString())
        );

        RowData result = query.get().getRows().get(0);

        this.username = String.valueOf(result.get("username"));
        this.hash = String.valueOf(result.get("password"));
        this.permissions.put("admin", "1".equals(result.get("admin")));
        this.permissions.put("edit_fs", "1".equals(result.get("edit_fs")));
        this.permissions.put("edit_others", "1".equals(result.get("edit_others")));

        return this;

    }

    public boolean isAdmin(){ return this.permissions.get("admin"); }
    public UUID getUUID(){ return this.uuid; }

    public boolean verifyPassword(String password){

        return BCrypt.verifyer().verify(password.toCharArray(), this.hash).verified;

    }

    public static User from(String username) throws ExecutionException, InterruptedException {

        CompletableFuture<QueryResult> query = Connector.connection.sendPreparedStatement(
                "SELECT username FROM users WHERE username = ?",
                List.of(username)
        );
        ResultSet results = query.get().getRows();
        if(results.size() == 0) return null;

        RowData result = results.get(0);

        return new User(UUID.fromString(String.valueOf(result.get("id"))));

    }

}
