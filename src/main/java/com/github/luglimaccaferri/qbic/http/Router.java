package com.github.luglimaccaferri.qbic.http;

import static spark.Spark.*; // importiamo tutte le cose statiche omettendo il namespace Spark

public class Router {

    private short port;

    public Router(short port){

        this.port = port;
        port(port);

    }

    public void ignite(){

        after((req, res) -> res.type("application/json"));

        get("/", (req, res) -> {

            // mai usato express? bene, è la stessa cosa

            return "Ciao";

        });

    }

}
