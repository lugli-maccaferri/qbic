package com.github.luglimaccaferri.qbic.http.models;

import spark.Route;
import xyz.luan.spark.decorator.RouteDecorator;

import java.util.ArrayList;
import java.util.Arrays;

public class ProtectedRoute extends RouteDecorator {

    private boolean requiresAuth;
    // private HashMap<String, Boolean> requiredParams = new HashMap<String, Boolean>();
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

            if(this.requiresAuth){
                String authHeader = req.headers("authorization");
                if(authHeader == null) throw HTTPError.INVALID_CREDENTIALS;
            }

            if(req.requestMethod().equals("POST") &&  this.requiredParams.length > 0){

                ArrayList<String> missingParameters = new ArrayList<String>();
                Arrays.stream(this.requiredParams).forEach(param -> {
                    if(req.queryParams(param) == null) missingParameters.add(param);
                });

                if(missingParameters.size() > 0){
                    throw new HTTPError("missing_parameters", 400)
                            .put("parameters", missingParameters.toArray());
                }

            }

            // dummy middleware (da ampliare)
            // ritorno null perché è un middleware, non devo modificare il flow della navigazione

            return null;
        };
    }

}
