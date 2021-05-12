package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.http.models.misc.User;
import spark.Route;

public class AuthController {

    public static Route index = (req, res) -> {

        return new Ok().put("message", "/auth endpoint").toResponse(res);

    };

    public static Route login = (req, res) -> {

        String username = req.queryParams("username"),
                password = req.queryParams("password");

        User user = User.from(username);
        if(user == null) return HTTPError.INVALID_CREDENTIALS.toResponse(res);
        if(!user.verifyPassword(password)) return HTTPError.INVALID_CREDENTIALS.toResponse(res);

        return new Ok().toResponse(res);

    };

}
