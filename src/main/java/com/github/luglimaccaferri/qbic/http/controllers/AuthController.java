package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.http.models.misc.User;
import spark.Route;

public class AuthController {

    public static Route index = (req, res) -> {

        return new Ok().put("message", "/auth endpoint").toResponse(res);

    };

    public static Route publicKey = (req, res) -> {

        String public_key = req.queryParams("public-key");

        /*System.out.println(req.headers("Host"));
        if(req.ip().compareToIgnoreCase(Core.getConfig().get("parent").getAsString()) != 0)
            return HTTPError.FORBIDDEN.toResponse(res);*/

        Core.logger.warn("updating public key!");
        Core.updatePublicKey(public_key);

        return new Ok().toResponse(res);

    };

}
