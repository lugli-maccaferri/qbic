package com.github.luglimaccaferri.qbic;

import com.github.luglimaccaferri.qbic.data.cli.CliItem;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.http.Router;
import org.json.simple.JSONObject;
import static java.lang.System.*;

public class Core {

    private JSONObject config;
    private final Router router;

    public Core(){

        out.println("initializing qbic...");
        out.println("config file: " + CliParser.options.get("config").value());
        CliItem<?> port = CliParser.options.get("port");
        out.println("http port: " + port.value());
        this.router = new Router((short) port.value()); // sono sicuro che sia short

    }

    public void init(){

        this.router.ignite();
        // tutto il resto della storia

    }

}
