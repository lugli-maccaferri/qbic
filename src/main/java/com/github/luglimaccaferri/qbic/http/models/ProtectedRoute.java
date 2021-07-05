package com.github.luglimaccaferri.qbic.http.models;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.luglimaccaferri.qbic.data.models.misc.User;
import com.github.luglimaccaferri.qbic.http.Router;
import com.github.luglimaccaferri.qbic.utils.Security;
import com.google.gson.JsonObject;
import spark.Route;
import xyz.luan.spark.decorator.RouteDecorator;

import java.util.ArrayList;
import java.util.Arrays;

public class ProtectedRoute extends RouteDecorator {

    private final boolean requiresAuth;
    private String[] requiredParams = {};

    public static ProtectedRoute route(String[] requiredParams, boolean requiresAuth){ return new ProtectedRoute(requiredParams, requiresAuth); }
    public static ProtectedRoute route(boolean requiresAuth){
        return new ProtectedRoute(requiresAuth);
    }
    public static ProtectedRoute route(String[] requiredParams){ return new ProtectedRoute(requiredParams); };

    private ProtectedRoute(String[] requiredParams, boolean requiresAuth){

        // Arrays.stream(requiredParams).forEach(param -> this.requiredParams.put(param, true));
        this.requiresAuth = requiresAuth;
        this.requiredParams = requiredParams;

    } // singleton
    private ProtectedRoute(boolean requiresAuth){
        this.requiresAuth = requiresAuth;
    }
    private ProtectedRoute(String[] requiredParams) {
        // Arrays.stream(requiredParams).forEach(param -> this.requiredParams.put(param, true));
        this.requiredParams = requiredParams;
        this.requiresAuth = false;
    }

    @Override
    protected Route before() {
        return (req, res) -> {

            JsonObject body = req.attribute("parsed-body");

            if(this.requiresAuth){

                String authHeader = req.headers("authorization");
                if(authHeader == null) throw HTTPError.INVALID_CREDENTIALS;

                String token = authHeader.split("Bearer ")[1];
                DecodedJWT verified_token = Security.verifyJWT(token);
                req.attribute("jwt", verified_token.getClaims());
                req.attribute("user", User.fromJWT(verified_token));

            }

            if(req.requestMethod().equals("POST") &&  this.requiredParams.length > 0){

                ArrayList<String> missingParameters = new ArrayList<String>();
                Arrays.stream(this.requiredParams).forEach(param -> {
                    String p = body.get(param).getAsString();
                    if(p == null || p.equals("")) missingParameters.add(param);
                });

                if(missingParameters.size() > 0){
                    throw new HTTPError("missing_parameters", 400)
                            .put("parameters", missingParameters.toArray());
                }

            }

            return null;

        };

    }

}
