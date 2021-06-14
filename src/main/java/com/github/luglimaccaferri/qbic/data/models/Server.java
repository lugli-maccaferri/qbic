package com.github.luglimaccaferri.qbic.data.models;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.models.sqlite.Sqlite;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Server {

    private final String jar_path;
    private final String id;
    private final String name;
    private final String owner;


    private enum STATUS {
      CREATED;
    };

    public Server(String id, String name, String jar_path, String owner){

        this.id = id;
        this.name = name;
        this.jar_path = jar_path;
        this.owner = owner;

    }

    public String getId() { return id; }
    public String getJar() { return jar_path; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public HashMap<String, String> toMap(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", getId());
        map.put("name", getName());
        map.put("owner", getOwner());

        return map;

    }

    public void create() throws SQLException, HTTPError {

        Connection conn = Sqlite.getConnection();

        try{

            PreparedStatement s = conn.prepareStatement("INSERT INTO servers VALUES (?, ?, ?, ?)");
            s.setString(1, getId()); s.setString(2, getName()); s.setString(3, getJar()); s.setString(4, getOwner());
            s.execute();
            createFs();
            conn.commit();

        }catch(Exception e){

            e.printStackTrace();
            conn.rollback();
            throw HTTPError.GENERIC_ERROR;

        }

    }

    public void createFs() throws IOException {

        String main_path = String.format("%s/%s", Core.SERVERS_PATH, this.id);
        Files.createDirectory(Path.of(main_path));
        Files.createFile(Path.of(main_path + "/eula.txt"));
        Files.writeString(Path.of((main_path + "/eula.txt")), "eula=true");

        Core.addCreatedServer(this);

    }

}
