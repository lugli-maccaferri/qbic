package com.github.luglimaccaferri.qbic.http.controllers;

import com.auth0.jwt.interfaces.Claim;
import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.google.gson.JsonObject;
import spark.Route;

import java.util.Map;

public class AuthController {

    public static Route index = (req, res) -> {

        Map<String, Claim> jwt = req.attribute("jwt");

        return new Ok()
                .put("user", jwt)
                .toResponse(res);

    };

    public static Route publicKey = (req, res) -> {

        JsonObject body = req.attribute("parsed-body");
        String public_key = body.get("public-key").getAsString();

        // trovare un modo decente per verificare la veridicità dell'authserver

        Core.logger.warn("updating public key!");
        Core.updatePublicKey(public_key);

        return new Ok().toResponse(res);

    };

}
