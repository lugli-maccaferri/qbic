package com.github.luglimaccaferri.qbic.http.models;

import com.github.luglimaccaferri.qbic.http.models.misc.BodyParser;
import spark.Route;
import xyz.luan.spark.decorator.RouteDecorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProtectedRoute extends RouteDecorator {
    private boolean requiresAuth;
    private HashMap<String, Boolean> requiredParams = new HashMap<String, Boolean>();
    private String[] arrayRequiredParams = {};
    public static ProtectedRoute route(String[] requiredParams, boolean requiresAuth){
        return new ProtectedRoute(requiredParams, requiresAuth);
    }
    public static ProtectedRoute route(boolean requiresAuth){
        return new ProtectedRoute(requiresAuth);
    }

    private ProtectedRoute(String[] requiredParams, boolean requiresAuth){
        Arrays.stream(requiredParams).forEach(param -> this.requiredParams.put(param, true));
        this.requiresAuth = requiresAuth;
        this.arrayRequiredParams = requiredParams;
    } // singleton
    private ProtectedRoute(boolean requiresAuth){
        this.requiresAuth = requiresAuth;
    }

    @Override
    protected Route before() {
        return (req, res) -> {

            if(this.requiresAuth){
                String authHeader = req.headers("authorization");
                if(authHeader == null) throw HTTPError.INVALID_CREDENTIALS;
            }

            if(req.requestMethod().equals("POST") &&  this.arrayRequiredParams.length > 0){

                ArrayList<String> missingParameters = new ArrayList<String>();
                BodyParser bp = (BodyParser) req.attribute("parsed_body");
                bp.getBody().entrySet().forEach(key -> {

                    if(this.requiredParams.get(key) == null)
                        missingParameters.add(key.toString());

                });

                if(missingParameters.size() > 0){

                    throw new HTTPError("missing_parameters", 400);

                }

            }

            // dummy middleware (da ampliare)
            // ritorno null perché è un middleware, non devo modificare il flow della navigazione

            return null;
        };
    }

}
