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
import org.eclipse.jetty.websocket.api.Session;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;

public class Server extends Thread {

    private final String jar_path;
    private final String id;
    private final String name;
    private final String owner;
    private final String main_path;
    private static final String DEFAULT_PATH = "https://static.macca.cloud/qbic/jars/spigot.jar";
    private ProcessBuilder process_builder;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final ConcurrentLinkedQueue<Session> sessions = new ConcurrentLinkedQueue<Session>();

    private static final ConcurrentHashMap<String, Server> created_servers = new ConcurrentHashMap<String, Server>();
    private static final ConcurrentHashMap<String, Server> started_servers = new ConcurrentHashMap<String, Server>();

    private enum Status {
        BORN,
        CREATED,
        TO_INIT,
        SHUTTING_DOWN,
        STOPPED,
        STARTING_UP,
        RUNNING,
        UNKNOWN
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

        // aggiungere parametri opzionali (xmx, xms, port)

    }

    public Status getStatus(){ return status; }
    public String getServerId() { return id; }
    public String getJar() { return jar_path; }
    public String getServerName() { return name; }
    public String getOwner() { return owner; }
    public synchronized static Server getStarted(String server_id) { return started_servers.get(server_id); }
    public synchronized static Server getCreated(String server_id) { return created_servers.get(server_id); }
    public synchronized File getMainDirectory(){

        if(!Files.exists(Path.of(this.main_path))) return null;
        return new File(this.main_path);


    }
    public synchronized File getResource(String path) throws IOException {

        Path p = Path.of(this.main_path + "/" + path);
        if(!Files.exists(p)) return null;
        File f = p.toFile();
        if(!f.getCanonicalPath().contains(this.main_path)) return null; // in questo modo previene il ../../

        return p.toFile().getCanonicalFile();


    }
    public synchronized static Server find(String server_id){

        Server created = created_servers.get(server_id),
                started = started_servers.get(server_id);

        if(created != null) return created;
        if(started != null) return started;

        return null;

    }

    @Override
    public void run() {

        try {
            this.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized static int getCreatedSize(){ return created_servers.size(); }
    public synchronized static int getStartedSize(){ return started_servers.size(); }
    public synchronized void addListener(Session session){ this.sessions.add(session); System.out.printf("server %s has %d listeners%n", id, sessions.size()); }
    public synchronized void removeListener(Session session){ this.sessions.remove(session); System.out.printf("server %s has %d listeners%n", id, sessions.size()); }

    public synchronized static void addCreated(Server server){

        if(created_servers.contains(server)) return;

        created_servers.put(server.getServerId(), server);
        if(started_servers.contains(server)) started_servers.remove(server.getServerId());

    }

    public synchronized static void addStarted(Server server){

        if(started_servers.contains(server)) return;

        started_servers.put(server.getServerId(), server);
        if(created_servers.contains(server)) created_servers.remove(server.getServerId());

    }



    private synchronized void startServer() throws IOException {

        if(this.status == Status.RUNNING || this.status == Status.STARTING_UP)
            return;

        this.status = Status.STARTING_UP;
        System.out.printf("starting %s%n", this.name);

        this.process_builder = new ProcessBuilder("java", "-Xmx1G", "-Xms1G", "-jar", "server.jar", "nogui");
        this.process_builder.redirectErrorStream(true);
        this.process_builder.directory(Path.of(this.main_path).toFile());

        this.process = this.process_builder.start();
        this.reader = new BufferedReader(
                new InputStreamReader(
                        this.process.getInputStream()
                )
        );

        this.status = Status.RUNNING;
        addStarted(this);
        System.out.printf("started server %s (pid: %d)%n", this.name, this.process.pid());
        String line;

        while((line = this.reader.readLine()) != null){
            if(this.sessions.size() > 0){
                final String s = line;
                this.sessions.forEach(session -> {
                    try {
                        session.getRemote().sendString(s);
                    } catch (IOException e) {
                        // boh è successo qualcosa di brutto veramente dai
                        e.printStackTrace();
                    }
                });
            }
        }

    }

    public HashMap<String, String> toMap(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", getServerId());
        map.put("name", getServerName());
        map.put("owner", getOwner());

        return map;

    }

    public synchronized void create() throws SQLException, HTTPError {

        Connection conn = Sqlite.getConnection();

        try{

            PreparedStatement s = conn.prepareStatement("INSERT INTO servers VALUES (?, ?, ?, ?)");
            s.setString(1, getServerId()); s.setString(2, getServerName()); s.setString(3, getJar()); s.setString(4, getOwner());
            s.execute();
            createFs();

            if(TypeUtils.isUrl(jar_path))
                downloadJar();
            else
                copyJar();

            conn.commit();
            this.status = Status.CREATED;
            addCreated(this);

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

    private synchronized void createFs() throws IOException {

        Files.createDirectory(Path.of(main_path));
        Files.createFile(Path.of(main_path + "/eula.txt"));
        Files.writeString(Path.of((main_path + "/eula.txt")), "eula=true");

    }

}
