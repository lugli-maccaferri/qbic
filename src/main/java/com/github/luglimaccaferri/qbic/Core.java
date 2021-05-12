package com.github.luglimaccaferri.qbic;

import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.data.cli.CliShortItem;
import com.github.luglimaccaferri.qbic.data.mysql.Connector;
import com.github.luglimaccaferri.qbic.http.Router;
import com.github.luglimaccaferri.qbic.http.models.misc.User;
import com.github.luglimaccaferri.qbic.utils.RandomString;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.lang.System.*;

public class Core {

    private final Router router;
    private static JsonObject config;
    private static Connector mysqlConnector = null;
    public static final Logger logger = Log.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    public Core(){

        out.println("initializing qbic...");
        out.println("config file: " + CliParser.options.get("config").value());
        CliShortItem port = (CliShortItem) CliParser.options.get("port");
        out.println("http port: " + port.value());
        this.router = new Router(port.value()); // sono sicuro che sia short

    }

    public static JsonObject getConfig(){ return config; }

    public void init(){

        try{

            String configPath = System.getProperty("user.dir") + "/" + (String) CliParser.options.get("config").value();
            if(!Files.exists(Path.of(configPath)))
                Files.copy(Path.of("config.json"), Path.of(configPath));

            config = (JsonObject) JsonParser.parseReader(new FileReader(configPath));
            mysqlConnector = Connector.connect(config.getAsJsonObject("mysql"));

            String rootUser = (String) CliParser.options.get("root-user").value();
            if(rootUser != null){

                //User.create(rootUser, RandomString.generateAlphanumeric(16), true);
                System.out.println(
                        String.format(
                                "generated root user () with UUID %s"
                        , UUID.randomUUID().toString())
                );

            }
            this.router.ignite();

        }catch(Exception e){

            logger.warn("startup error");
            Connector.connection.disconnect();
            this.router.kill();
            e.printStackTrace();

        }

    }

}
