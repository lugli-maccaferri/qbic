package com.github.luglimaccaferri.qbic;

import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.data.cli.CliShortItem;
import com.github.luglimaccaferri.qbic.http.Router;
import com.github.luglimaccaferri.qbic.utils.RandomString;
import com.github.luglimaccaferri.qbic.utils.Security;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Core {

    private final Router router;
    private static JsonObject config;
    private final static HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    public static final Logger logger = Log.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    private boolean initialized = false;

    public static String CONFIG_PATH = System.getProperty("user.dir") + "/" + (String) CliParser.options.get("config").value();
    public static String KEYS_PATH = System.getProperty("user.dir") + "/keys";
    public static KeyPair KEYS;

    public Core(){

        System.out.println("initializing qbic-client...");
        System.out.println("config file: " + CliParser.options.get("config").value());
        CliShortItem port = (CliShortItem) CliParser.options.get("port");
        System.out.println("http port: " + port.value());
        this.router = new Router(port.value()); // sono sicuro che sia short

    }

    public static JsonObject getConfig(){ return config; }
    public static HttpClient getHttpClient() { return httpClient; }

    public static void updatePublicKey(String pk) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        Files.deleteIfExists(Path.of(KEYS_PATH + "/parent.key"));
        FileOutputStream writer = new FileOutputStream(KEYS_PATH + "/parent.key");
        byte[] kb = Security.hexToBytes(pk);
        writer.write(kb);
        writer.close();

    }
    public void init(){

        if(this.initialized) return;

        try{

            // directory chiave pubblica/privata
            Files.createDirectories(Path.of(KEYS_PATH)); // createDirectories non throwa niente se la directory esiste già

            if(!Files.exists(Path.of(CONFIG_PATH)))
                Files.copy(
                        Objects.requireNonNull(
                                getClass().getClassLoader().getResourceAsStream("config.json")),
                        Path.of(CONFIG_PATH)
                );

            config = (JsonObject) JsonParser.parseReader(new FileReader(CONFIG_PATH));

            this.router.ignite();
            this.initialized = true;
            System.out.println("qbic has been successfully started!");

        }catch(Exception e){

            logger.warn("startup error");
            this.router.kill();
            e.printStackTrace();

        }

    }

}