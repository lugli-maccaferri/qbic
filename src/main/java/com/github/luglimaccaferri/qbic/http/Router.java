package com.github.luglimaccaferri.qbic.http;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.controllers.AuthController;
import com.github.luglimaccaferri.qbic.http.controllers.ServerController;
import com.github.luglimaccaferri.qbic.http.controllers.WebsocketController;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;

import static com.github.luglimaccaferri.qbic.http.models.ProtectedRoute.route;
import static spark.Spark.*;

public class Router {

    public Router(short port){ port(port); }
    public void kill(){ stop(); }

    public void ignite(){

        webSocket("/console-stream", WebsocketController.class);

        before((req, res) -> {

            res.type("application/json");

            String contentType = req.headers("Content-Type");
            String requestMethod = req.requestMethod();
            String url = req.url();

            Core.logger.warn(requestMethod + " " + url);
            Core.logger.warn(contentType);

        });

        // root paths

        get("/", (req, res) -> {
            return new Ok().put("message", "ciaooooooo").toResponse(res);
        });
        route(true).get("/auth", AuthController.index); // passa direttamente il body della lambda indicata

        // derived paths

        path("/auth", () -> {
            post("/public-key", AuthController.publicKey);
        });

        path("/server", () -> {
            route(new String[]{"name", "query-port", "server-port", "rcon-port"}, true).post("/create", ServerController.create);
            route(true).post("/start/:id", ServerController.start);
            route(true).post("/stop/:id", ServerController.stop);
            route(true).get("/files/:id", ServerController.mainDirectory);
            route(true).get("/files/:id/:path", ServerController.files);
            route(true).post("/edit/:id/:path", ServerController.editFile);
            route(true).post("/create/:id/:path", ServerController.createFile);
            get("/list", ServerController.list);
            get("/info/:id", ServerController.info);
            route(new String[]{"command"}, true).post("/send-command/:id", ServerController.sendCommand);
        });

        get("*", (req, res) -> HTTPError.NOT_FOUND.toResponse(res));

        exception(HTTPError.class, (e, req, res) -> {
            res.status(e.getErrorCode());
            res.body(e.print());
        });

    }

}
