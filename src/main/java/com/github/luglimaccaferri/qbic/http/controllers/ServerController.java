package com.github.luglimaccaferri.qbic.http.controllers;

import com.github.luglimaccaferri.qbic.data.models.Server;
import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.data.net.query.QbicQuery;
import com.github.luglimaccaferri.qbic.data.net.query.QbicQueryResponse;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import com.github.luglimaccaferri.qbic.utils.FileUtils;
import com.github.luglimaccaferri.qbic.utils.RandomString;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.vv32.rcon.Rcon;
import spark.Route;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;

public class ServerController {

    public static Route deleteServer = (req, res) -> {

        String server_id = req.params(":id");

        try{

            User user = req.attribute("user");
            Server server = Server.find(server_id);
            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.FORBIDDEN.toResponse(res);

            if(server.isRunning()) return new HTTPError("conflict", 409).put("comment", "stop the server first").toResponse(res);

            server.stopServer();
            FileUtils.deleteDirectory(Path.of(server.getMainDirectory().getAbsolutePath()));
            Server.delete(server_id);

            return new Ok().toResponse(res);

        }catch(Exception e){

            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route getServer = (req, res) -> {

        try{

            String server_id = req.params(":id");
            User user = req.attribute("user");
            Server server = Server.find(server_id);

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.FORBIDDEN.toResponse(res);

            return new Ok().put("server", server.toMap()).toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route deleteFile = (req, res) -> {

        try{

            String server_id = req.params(":id"),
                    path = new String(Base64.getDecoder().decode(req.params(":path"))).trim();
            User user = req.attribute("user");
            Server server = Server.find(server_id);

            System.out.println(path);

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);
            if(!FileUtils.isValidPath(server.getMainDirectory().toPath().toString() + "/" + path, server.getMainDirectory().toPath().toString())) return HTTPError.BAD_REQUEST.toResponse(res);

            File resource = server.getResource(path);
            if(resource == null) return HTTPError.RESOURCE_NOT_FOUND.toResponse(res);

            if(resource.isDirectory()){

                Files
                        .walk(Path.of(resource.getAbsolutePath()))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

            }else Files.deleteIfExists(Path.of(resource.getAbsolutePath()));

            return new Ok().toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route createFile = (req, res) -> {

        try{

            JsonObject body = req.attribute("parsed-body");
            String server_id = req.params(":id"),
                    path = new String(Base64.getDecoder().decode(req.params(":path"))).trim();
            JsonElement is_dir = body.get("is-directory");
            boolean bool_is_dir = false;
            if(is_dir != null) bool_is_dir = Boolean.parseBoolean(is_dir.getAsString());

            Server server = Server.find(server_id);
            User user = req.attribute("user");

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);
            if(!FileUtils.isValidPath(server.getMainDirectory().toPath().toString() + "/" + path, server.getMainDirectory().toPath().toString())) return HTTPError.BAD_REQUEST.toResponse(res);

            File resource = server.getResource(path);
            if(resource != null) return new HTTPError("file_already_exists", 409).toResponse(res); // wtf perché non va whatt

            FileUtils.resolveAndCreate(server, path, bool_is_dir);

            return new Ok().toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route editFile = (req, res) -> {

        try{

            JsonObject body = req.attribute("parsed-body");
            String server_id = req.params(":id"),
                    path = new String(Base64.getDecoder().decode(req.params(":path"))).trim();
            String file_contents = body.get("file-contents").getAsString();
            Server server = Server.find(server_id);
            User user = req.attribute("user");

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);
            File resource = server.getResource(path);
            if(resource == null) return HTTPError.RESOURCE_NOT_FOUND.toResponse(res);

            FileOutputStream fout = new FileOutputStream(resource.getAbsolutePath());
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            byte[] content = file_contents.getBytes();
            bout.write(content);

            bout.close();
            fout.close();

            return new Ok().toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route sendCommand = (req, res) -> {

        try{
            JsonObject body = req.attribute("parsed-body");

            String server_id = req.params(":id"),
                    command = body.get("command").getAsString();
            User user = req.attribute("user");

            Server server = Server.find(server_id);
            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

            Rcon rcon = Rcon.open("localhost", server.getRconPort());
            if(!rcon.authenticate("qbic")) return new HTTPError("invalid_rcon", 500).toResponse(res);

            String response = rcon.sendCommand(command);

            return new Ok().put("command_response", response).toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            if(e instanceof ConnectException) return new HTTPError("failed_rcon", 500).toResponse(res);

            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route info = (req, res) -> {

        try{
            String server_id = req.params(":id");
            Server server = Server.find(server_id);

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            QbicQuery query = new QbicQuery("", server.getQueryPort());
            QbicQueryResponse response = query.fullStat();

            return new Ok()
                    .put("motd", response.getMotd())
                    .put("online_players", Integer.parseInt(response.getOnlinePlayers()))
                    .put("max_players", Integer.parseInt(response.getMaxPlayers()))
                    .put("gamemode", response.getGametype())
                    .put("main_world", response.getMap())
                    .put("players", response.getPlayers())
                    .put("plugins", response.getPlugins())
                    .put("version", response.getVersion())
                    .toResponse(res);

        }catch(Exception e){

            if(e instanceof SocketTimeoutException)
                return new HTTPError("socket_timeout", 500)
                        .put("comment", "server is probably offline (not started)")
                        .toResponse(res);

            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

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
            String id = req.params(":id"),
                    path = new String(Base64.getDecoder().decode(req.params(":path"))).trim();

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

        try{
            JsonObject body = req.attribute("parsed-body");
            User user = req.attribute("user");
            String name = body.get("name").getAsString(),
                    query_port = body.get("query-port").getAsString(),
                    rcon_port = body.get("rcon-port").getAsString(),
                    server_port = body.get("server-port").getAsString();
            JsonElement jar_path = body.get("jar-path"), xmx = body.get("xmx"), xms = body.get("xms");
            String str_jar_path, str_xmx, str_xms;

            if(xmx == null) str_xmx = "1G"; else str_xmx = xmx.getAsString();
            if(xms == null) str_xms = "1G"; else str_xms = xms.getAsString();
            if(jar_path == null) str_jar_path = null; else str_jar_path = jar_path.getAsString();

            if(!(user.canEditFs() || user.isAdmin())) return HTTPError.FORBIDDEN.toResponse(res);

            Server server = new Server(
                    RandomString.generateAlphanumeric(32),
                    name,
                    str_jar_path,
                    user.getUUID().toString(),
                    user.getUsername(),
                    Integer.parseInt(query_port),
                    str_xmx,
                    str_xms,
                    Integer.parseInt(server_port),
                    Integer.parseInt(rcon_port)
            );

            server.create();

            return new Ok().put("server", server.toMap()).toResponse(res);

        }catch(Exception e){

            System.out.println(e.toString());

            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route stop = (req, res) -> {

        try{

            String server_id = req.params(":id");
            Server server = Server.find(server_id);
            User user = req.attribute("user");

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

            server.stopServer();
            return new Ok().toResponse(res);

        }catch(Exception e){

            e.printStackTrace();

            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

    public static Route start = (req, res) -> {

        try{
            // ritorna success: true indipendentemente da quello che accade al server, dato che questo rappresenta lo stato della richiesta, piuttosto che quello del server!
            String server_id = req.params(":id");
            Server server = Server.find(server_id);
            User user = req.attribute("user");

            if(server == null) return HTTPError.SERVER_NOT_FOUND.toResponse(res);
            if(!user.canEditThis(server)) return HTTPError.UNAUTHORIZED.toResponse(res);

            server.start(); // ciao vai su un altro thread

            return new Ok().toResponse(res);

        }catch(Exception e){

            e.printStackTrace();
            return HTTPError.GENERIC_ERROR.toResponse(res);

        }

    };

}
