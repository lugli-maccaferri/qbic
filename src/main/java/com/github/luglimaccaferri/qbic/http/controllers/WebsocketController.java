package com.github.luglimaccaferri.qbic.http.controllers;
import com.github.luglimaccaferri.qbic.data.models.Server;
import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.utils.Security;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(maxIdleTime = Integer.MAX_VALUE) // un bel timeout largo
public class WebsocketController {

    private static final ConcurrentHashMap<Session, User> sessions = new ConcurrentHashMap<Session, User>();

    @OnWebSocketConnect
    public void onConnect(Session session){

        // sessions.put(session, null);

    }

    @OnWebSocketClose
    public void onClose(Session s, int statusCode, String reason) {

        User user = sessions.get(s);
        if(user == null) return;

        String emitter = user.getEmitter();
        user.resetEmitter();

        if(emitter == null) return;

        Server.find(emitter).removeListener(s);
        sessions.remove(s);

    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {

        String[] msg = message.split(":");
        if(msg.length < 2){
            session.getRemote().sendBytes(TypeUtils.BYTE_BAD_REQUEST.rewind());
            return;
        }

        try{

            User user;
            Server server;
            String command = msg[0];

            switch (command.toLowerCase()) {
                case "hello" -> {

                    // hello:jwt
                    // autenticazione al server websocket

                    String jwt = msg[1];
                    user = User.fromJWT(Security.verifyJWT(jwt));
                    sessions.put(session, user);
                    session.getRemote().sendBytes(TypeUtils.SUCCESS.rewind());

                }
                case "read" -> {

                    // read:server_id
                    // subscribe all'output della console di un dato server

                    String server_id = msg[1];
                    user = sessions.get(session);

                    if(user == null){ session.getRemote().sendBytes(TypeUtils.BYTE_FORBIDDEN.rewind()); return; }
                    server = Server.find(server_id);
                    if(server == null){ session.getRemote().sendBytes(TypeUtils.BYTE_NOT_FOUND.rewind()); return; }

                    server.addListener(session);
                    user.setEmitter(server_id);
                    session.getRemote().sendBytes(TypeUtils.SUCCESS.rewind());

                }
                default -> session.getRemote().sendBytes(TypeUtils.BYTE_BAD_REQUEST.rewind());
            }

        }catch(HTTPError e){

            session.getRemote().sendBytes(TypeUtils.BYTE_FORBIDDEN.rewind());

        }

    }

}
