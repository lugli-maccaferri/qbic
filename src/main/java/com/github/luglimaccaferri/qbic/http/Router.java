package com.github.luglimaccaferri.qbic.http;

import com.github.luglimaccaferri.qbic.http.controllers.AuthController;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.http.models.misc.BodyParser;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import static com.github.luglimaccaferri.qbic.http.models.ProtectedRoute.route;
import static spark.Spark.*;

public class Router {

    private final String[] acceptedContentTypes = { "application/json", "application/x-www-form-urlencoded" };
    public static final Logger logger = Log.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    public Router(short port){

        port(port);

    }

    public void ignite(){

        before((req, res) -> {

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

            // req.attribute("body", Core.gson.toJson(req.body())); // body in json
            if(req.requestMethod().equals("POST") &&
                    (this.acceptedContentTypes[0].equals(contentType) || this.acceptedContentTypes[1].equals(contentType)) // O(1) gang lmaooo
            ) req.attribute("parsed_body", new BodyParser(req));

        });

        after((req, res) -> res.type("application/json"));

        // root paths

        get("/", (req, res) -> { return Ok.SUCCESS; });
        route(true).get("/auth", AuthController.index); // passa direttamente il body della lambda indicata

        // derived paths

        path("/auth", () -> {
            post("/login", AuthController.login);
        });

        exception(HTTPError.class, (e, req, res) -> {
            res.status(e.getErrorCode());
            res.body(e.print());
        });

    }

}
