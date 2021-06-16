package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.models.Server;
import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.utils.RandomString;
import spark.Route;

public class ServerController {

    public static Route create = (req, res) -> {

        User user = req.attribute("user");
        String jar_path = req.queryParams("jar_path");

        if(!(user.canEditFs() || user.isAdmin())) return HTTPError.FORBIDDEN;

        Server server = new Server(
                RandomString.generateAlphanumeric(32),
                req.queryParams("name"),
                jar_path,
                user.getUUID().toString()
        );

        server.create();

        return Ok.SUCCESS.put("server", server.toMap()).toResponse(res);

    };

    public static Route start = (req, res) -> {

        String server_id = req.params(":id");
        Server server = Server.getCreated(server_id);
        User user = req.attribute("user");

        if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
        if(!(user.isAdmin() || user.getUUID().toString().equals(server.getOwner()))) return HTTPError.UNAUTHORIZED.toResponse(res);

        server.start(); // ciao vai su un altro thread

        return Ok.SUCCESS.toResponse(res); // ritorna success: true indipendentemente da quello che accade al server, dato che questo rappresenta lo stato della richiesta, piuttosto che quello del server!

    };

}
