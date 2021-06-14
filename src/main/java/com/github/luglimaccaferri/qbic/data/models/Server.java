package com.github.luglimaccaferri.qbic.data.models;

import com.github.luglimaccaferri.qbic.Core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void createFs() throws IOException {

        String main_path = String.format("%s/%s", Core.SERVERS_PATH, this.id);
        Files.createDirectory(Path.of(main_path));
        Files.createFile(Path.of(main_path + "/eula.txt"));
        Files.writeString(Path.of((main_path + "/eula.txt")), "eula=true");

        Core.addCreatedServer(this);

    }

}
