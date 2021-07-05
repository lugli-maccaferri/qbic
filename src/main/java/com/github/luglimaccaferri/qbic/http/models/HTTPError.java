package com.github.luglimaccaferri.qbic.http.models;

import com.google.gson.Gson;
import spark.Response;

import java.util.HashMap;
import java.util.List;

public class HTTPError extends Exception implements JSONResponse{

    private final HashMap<String, Object> response = new HashMap<String, Object>();
    private final int errorCode;

    public HTTPError(String errorMessage, int errorCode){

        super();

        this.errorCode = errorCode;
        response.put("success", false);
        response.put("error", errorMessage);

    }

    public static final HTTPError BAD_REQUEST = new HTTPError("bad_request", 400);
    public static final HTTPError MALFORMED_BODY = new HTTPError("malformed_body", 400).put("comment", "please encode your body in JSON");
    public static final HTTPError INVALID_CREDENTIALS = new HTTPError("invalid_credentials", 401);
    public static final HTTPError FORBIDDEN = new HTTPError("forbidden", 403);
    public static final HTTPError GENERIC_ERROR = new HTTPError("generic_error", 500);
    public static final HTTPError EXPIRED_CREDENTIALS = new HTTPError("expired_credentials", 401);
    public static final HTTPError UNAUTHORIZED = new HTTPError("unauthorized", 401);
    public static final HTTPError SERVER_NOT_FOUND = new HTTPError("server_not_found", 404);
    public static final HTTPError NOT_FOUND = new HTTPError("not_found", 404);
    public static final HTTPError RESOURCE_NOT_FOUND = new HTTPError("resource_not_found", 404);

    public synchronized String toResponse(Response response){
        response.status(this.errorCode);
        return new Gson().toJson(this.response);
    }
    public int getErrorCode(){ return this.errorCode; }
    public synchronized HTTPError put(String key, Object value){ this.response.put(key, value); return this; }

    public synchronized String print(){ return new Gson().toJson(this.response); }

}
