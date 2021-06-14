package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import spark.Route;

public class ServerController {

    public static Route create = (req, res) -> {

        User user = req.attribute("user");
        if(!user.canEditFs()) return HTTPError.FORBIDDEN;

        return Ok.SUCCESS.toResponse(res);

    };

}
