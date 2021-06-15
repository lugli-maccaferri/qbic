package com.github.luglimaccaferri.qbic.data.models;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.models.sqlite.Sqlite;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Server {

    private final String jar_path;
    private final String id;
    private final String name;
    private final String owner;
    private final String main_path;
    private static final String DEFAULT_PATH = "https://static.macca.cloud/qbic/jars/spigot.jar";
    private enum Status {
        BORN,
        CREATED
    };
    private Status status;

    public Server(String id, String name, String jar_path, String owner){

        if(jar_path == null) jar_path = DEFAULT_PATH;

        this.id = id;
        this.name = name;
        this.jar_path = jar_path;
        this.owner = owner;
        this.main_path = String.format("%s/%s", Core.SERVERS_PATH, this.id);
        this.status = Status.BORN;

    }

    public Status getStatus(){ return status; }
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

            if(TypeUtils.isUrl(jar_path))
                downloadJar();
            else
                copyJar();

            conn.commit();
            this.status = Status.CREATED;

        }catch(Exception e){

            e.printStackTrace();
            conn.rollback();

            if(e instanceof CompletionException)
                throw new HTTPError("no_jar_found", 404);

            throw HTTPError.GENERIC_ERROR;

        }

    }

    private void downloadJar(){

        System.out.printf("downloading %s...%n", jar_path);

        CompletableFuture.runAsync(() -> { // basta, ci sono già troppi thread in questa applicazione

            Request request = new Request.Builder().url(jar_path).build();
            try {

                // magari mettiamo backpressure, perché per ora tutto il file scaricato viene caricato in RAM

                Instant start = Instant.now();
                Response response = Core.getHttpClient().newCall(request).execute();
                ResponseBody body = response.body();
                long len = Objects.requireNonNull(body).contentLength();
                BufferedSource src = body.source();
                File file = new File(main_path + "/server.jar");
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                long read = 0, current_read_bytes = 0;

                while(
                        (read = src.read(sink.getBuffer(), 2048)) != -1
                ){
                    current_read_bytes += read;
                }

                System.out.printf("wrote %d bytes (%ds)!%n", current_read_bytes, Duration.between(start, Instant.now()).toMillis() / 1000);
                sink.writeAll(src);
                sink.flush();
                sink.close();
                response.close();

            } catch (IOException e) {

                // e.printStackTrace();
                Core.logger.warn("failed to download %s", jar_path);

            }

        });

    }

    private void copyJar() throws CompletionException {

        CompletableFuture.supplyAsync(() -> {
            try {
                return Files.copy(Path.of(Core.JARS_PATH + "/" + jar_path), Path.of(main_path + "/server.jar"));
            } catch (IOException e) {
                // e.printStackTrace();
                throw new CompletionException(e);
            }
        }).join();

    }

    private void createFs() throws IOException {

        Files.createDirectory(Path.of(main_path));
        Files.createFile(Path.of(main_path + "/eula.txt"));
        Files.writeString(Path.of((main_path + "/eula.txt")), "eula=true");

        Core.addCreatedServer(this);

    }

}
