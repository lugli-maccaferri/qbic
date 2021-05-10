package com.github.luglimaccaferri.qbic.http;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.controllers.AuthController;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import static com.github.luglimaccaferri.qbic.http.models.ProtectedRoute.route;
import static spark.Spark.*;

public class Router {

    // private final String[] acceptedContentTypes = { "application/json", "application/x-www-form-urlencoded" };
    public static final Logger logger = Log.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    public Router(short port){ port(port); }

    public void kill(){ stop(); }

    public void ignite(){

        before((req, res) -> {

            res.type("application/json");

            String contentType = req.headers("Content-Type");
            String requestMethod = req.requestMethod();
            String url = req.url();

            logger.info(requestMethod + " " + url);
            logger.info(contentType);

            /*
            *
            * Content-Type per le richieste (non di upload file) accettati:
            * - application/json
            * - application/x-www-form-urlencoded
            *
            * */

        });

        // root paths

        get("/", (req, res) -> { return Ok.SUCCESS; });
        route(true).get("/auth", AuthController.index); // passa direttamente il body della lambda indicata

        // derived paths

        path("/auth", () -> {
            route(new String[]{"username", "password"}).post("/login", AuthController.login);
        });

        exception(HTTPError.class, (e, req, res) -> {
            res.status(e.getErrorCode());
            res.body(e.print());
        });

    }

}
