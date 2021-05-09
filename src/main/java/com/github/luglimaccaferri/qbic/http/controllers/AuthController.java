package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.http.models.misc.BodyParser;
import com.github.luglimaccaferri.qbic.http.models.misc.User;
import com.google.gson.Gson;
import spark.Route;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AuthController {

    public static Route index = (req, res) -> {

        return new Ok().put("message", "/auth endpoint").toResponse(res);

    };

    public static Route login = (req, res) -> {

        BodyParser body = (BodyParser) req.attribute("parsed_body");
        Ok ok = new Ok();

        ok.put("username", body.get("username"));
        ok.put("password", body.get("password"));

        return ok.toResponse(res);

    };

}
