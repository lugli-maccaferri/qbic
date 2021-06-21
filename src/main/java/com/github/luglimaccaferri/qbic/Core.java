package com.github.luglimaccaferri.qbic;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.data.cli.CliShortItem;
import com.github.luglimaccaferri.qbic.data.models.sqlite.Sqlite;
import com.github.luglimaccaferri.qbic.http.Router;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.utils.Migration;
import com.github.luglimaccaferri.qbic.utils.Security;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.github.luglimaccaferri.qbic.data.models.Server;
import okhttp3.OkHttpClient;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Core {

    private final Router router;
    private static JsonObject config;
    private final static OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
    public static final Logger logger = Log.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    private boolean initialized = false;
    public static final String CONFIG_PATH = System.getProperty("user.dir") + "/" + (String) CliParser.options.get("config").value();
    public static final String KEYS_PATH = System.getProperty("user.dir") + "/keys";
    public static final String SQLITE_PATH = System.getProperty("user.dir") + "/qbic.db";
    public static final String SERVERS_PATH = System.getProperty("user.dir") + "/srv";
    public static final String JARS_PATH = System.getProperty("user.dir") + "/jars";
    private static PublicKey PARENT_KEY;
    private static JWTVerifier verifier;

    public Core(){

        System.out.println("initializing qbic-client...");
        System.out.println("config file: " + CliParser.options.get("config").value());
        CliShortItem port = (CliShortItem) CliParser.options.get("port");
        System.out.println("http port: " + port.value());
        this.router = new Router(port.value()); // sono sicuro che sia short

    }

    public static String getKeysPath(){ return KEYS_PATH; }
    public static PublicKey getParentKey(){ return PARENT_KEY; }
    public static JsonObject getConfig(){ return config; }
    public static OkHttpClient getHttpClient() { return httpClient; }
    public static JWTVerifier getVerifier(){ return verifier; }

    public static void updatePublicKey(String pk) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        Files.deleteIfExists(Path.of(KEYS_PATH + "/parent.key"));
        FileOutputStream writer = new FileOutputStream(KEYS_PATH + "/parent.key");
        byte[] kb = Security.hexToBytes(pk);
        writer.write(kb);
        writer.close();
        PARENT_KEY = readParentKey();
        Algorithm rsa = Algorithm.RSA256((RSAPublicKey) Core.PARENT_KEY, null);
        verifier = JWT.require(rsa).withIssuer("qbic").build();

    }

    public void init(){

        if(this.initialized) return;

        try{

            addShutdownHook();
            // directory chiave pubblica/privata
            Files.createDirectories(Path.of(KEYS_PATH)); // createDirectories non throwa niente se la directory esiste già
            // directory server
            Files.createDirectories(Path.of(SERVERS_PATH));
            // directory jar
            Files.createDirectories(Path.of(JARS_PATH));

            if(!Files.exists(Path.of(CONFIG_PATH)))
                Files.copy(
                        Objects.requireNonNull(
                                getClass().getClassLoader().getResourceAsStream("config.json")),
                        Path.of(CONFIG_PATH)
                );

            config = (JsonObject) JsonParser.parseReader(new FileReader(CONFIG_PATH));

            if(Files.exists(Path.of(KEYS_PATH + "/parent.key"))) {
                PARENT_KEY = readParentKey();
                Algorithm rsa = Algorithm.RSA256((RSAPublicKey) Core.PARENT_KEY, null);
                verifier = JWT.require(rsa).withIssuer("qbic").build();
            }

            if(!Files.exists(Path.of(SQLITE_PATH))) Files.createFile(Path.of(SQLITE_PATH));

            Sqlite.init();
            migrate();

            System.out.println("loading servers...");
            loadServers();
            System.out.printf("%d server(s) loaded!%n", Server.getCreatedSize());

            this.router.ignite();
            this.initialized = true;
            System.out.println("qbic has been successfully started!");

        }catch(Exception e){

            logger.warn("startup error");
            this.router.kill();
            e.printStackTrace();

        }

    }

    private static PublicKey readParentKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKey = Files.readAllBytes(Path.of(KEYS_PATH + "/parent.key"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static void loadServers() throws SQLException, HTTPError {


        Connection conn = Sqlite.getConnection();
        PreparedStatement s = conn.prepareStatement("SELECT * FROM servers");
        ResultSet set = s.executeQuery();

        while (set.next()) {

            String id = set.getString("id"),
                    name = set.getString("name"),
                    jar_path = set.getString("jar_path"),
                    owner = set.getString("owner"),
                    owner_name = set.getString("owner_name"),
                    xmx = set.getString("xmx"),
                    xms = set.getString("xms");
            int query_port = set.getInt("query_port");
            int server_port = set.getInt("server_port");


            Server.addCreated(new Server(
                    id, name, jar_path, owner, owner_name, query_port, xmx, xms, server_port
            ));

        }

    }

    private void addShutdownHook(){

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){

                System.out.println("qbic quitting...");
                // fare graceful shutdown qua


            }
        });

    }

    private void migrate() throws SQLException {

        /*
        CREATE TABLE servers(
            id varchar(32) not null primary key,
            name varchar(255) not null,
            jar_path varchar(1000) not null,
            owner varchar(36) not null,
            query_port unsigned short unique not null,
            owner_name varchar(255) not null,
            xmx varchar(255) not null,
            xms varchar(255) not null,
            server_port unsigned short unique not null);
         */

        Migration.createTable("servers", new String[]{
                "id varchar(32) not null primary key,",
                "name varchar(255) not null,",
                "jar_path varchar(1000) not null,",
                "owner varchar(36) not null,",
                "query_port unsigned short unique not null,",
                "owner_name varchar(255) not null,",
                "xmx varchar(255) not null,",
                "xms varchar(255) not null,",
                "server_port unsigned short unique not null"
        });

    }

}