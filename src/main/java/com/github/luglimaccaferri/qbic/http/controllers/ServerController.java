package com.github.luglimaccaferri.qbic.http.controllers;

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

        if(!user.canEditFs()) return HTTPError.FORBIDDEN;

        Server server = new Server(
                RandomString.generateAlphanumeric(32),
                req.queryParams("name"),
                jar_path,
                user.getUUID().toString()
        );

        server.create();

        return Ok.SUCCESS.put("server", server.toMap()).toResponse(res);

    };

}
