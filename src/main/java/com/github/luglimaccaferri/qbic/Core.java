package com.github.luglimaccaferri.qbic;

import com.github.luglimaccaferri.qbic.data.cli.CliItem;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.http.Router;
import com.google.gson.Gson;

import static java.lang.System.*;

public class Core {

    private final Router router;
    public static Gson gson = new Gson();

    public Core(){

        out.println("initializing qbic...");
        out.println("config file: " + CliParser.options.get("config").value());
        CliItem<Short> port = (CliItem<Short>) CliParser.options.get("port");
        out.println("http port: " + port.value());
        this.router = new Router(port.value()); // sono sicuro che sia short

    }

    public void init(){

        this.router.ignite();
        // tutto il resto della storia

    }

}
