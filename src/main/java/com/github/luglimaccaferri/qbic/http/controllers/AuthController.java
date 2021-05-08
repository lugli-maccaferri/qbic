package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.http.models.misc.User;
import com.google.gson.Gson;
import spark.Route;

public class AuthController {

    public static Route index = (request, response) -> {

        return new Ok().put("message", "/auth endpoint").print();

    };

    public static Route login = (request, response) -> {

        // User user = Core.gson.fromJson((String) request.attribute("json_body"), User.class);

        return Ok.SUCCESS;

    };

}
