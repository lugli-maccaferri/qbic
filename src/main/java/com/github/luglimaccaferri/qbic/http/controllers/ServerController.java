package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.data.models.Server;
import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.utils.FileUtils;
import com.github.luglimaccaferri.qbic.utils.RandomString;
import spark.Route;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class ServerController {

    public static Route info = (req, res) -> {

        // todo: come back quando ho scritto la lib per la query

        return null;

    };

    public static Route list = (req, res) -> {

        ArrayList<HashMap<String, String>> servers = Server.getAll();
        return new Ok().put("servers", servers.toArray()).toResponse(res);

    };

    public static Route mainDirectory = (req, res) -> {

        User user = req.attribute("user");
        String id = req.params(":id");

        Server server = Server.find(id);
        if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
        if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

        try{

            return FileUtils.handleResource(server.getMainDirectory()).toResponse(res);

        }catch(Exception e){

            // e.printStackTrace();
            if(e instanceof IllegalArgumentException) return HTTPError.BAD_REQUEST.toResponse(res);
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route files = (req, res) -> {

        try{
            User user = req.attribute("user");
            String id = req.params(":id"), path = new String(Base64.getDecoder().decode(req.params(":path")));
            // path è encodato in base64 per facilitarne la trasmissione
            // path sostanzialmente indica la cartella/file che si vuole visualizzare

            Server server = Server.find(id);
            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

            return FileUtils.handleResource(server.getResource(path)).toResponse(res);

        }catch(Exception e){

            // e.printStackTrace();
            if(e instanceof IllegalArgumentException) return HTTPError.BAD_REQUEST.toResponse(res);
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route create = (req, res) -> {

        User user = req.attribute("user");
        String jar_path = req.queryParams("jar_path"),
            query_port = req.queryParams("query-port"),
            xmx = req.queryParams("xmx"), xms = req.queryParams("xms"), server_port = req.queryParams("server-port");

        if(xmx == null) xmx = "1G";
        if(xms == null) xms = "1G";

        if(!(user.canEditFs() || user.isAdmin())) return HTTPError.FORBIDDEN.toResponse(res);

        Server server = new Server(
                RandomString.generateAlphanumeric(32),
                req.queryParams("name"),
                jar_path,
                user.getUUID().toString(),
                user.getUsername(),
                Short.parseShort(query_port),
                xmx,
                xms,
                Short.parseShort(server_port)
        );

        server.create();

        return new Ok().put("server", server.toMap()).toResponse(res);

    };

    public static Route start = (req, res) -> {

        String server_id = req.params(":id");
        Server server = Server.getCreated(server_id);
        User user = req.attribute("user");

        if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
        if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

        server.start(); // ciao vai su un altro thread

        return new Ok().toResponse(res); // ritorna success: true indipendentemente da quello che accade al server, dato che questo rappresenta lo stato della richiesta, piuttosto che quello del server!

    };

}
