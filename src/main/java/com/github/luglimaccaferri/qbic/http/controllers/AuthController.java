package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.http.models.Ok;
import spark.Route;

public class AuthController {

    public static Route index = (req, res) -> {

        return new Ok().put("message", "/auth endpoint").toResponse(res);

    };

    public static Route login = (req, res) -> {

        String username = req.queryParams("username"),
                password = req.queryParams("password");

        return new Ok().toResponse(res);

    };

}
