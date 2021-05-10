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
    public static final HTTPError INVALID_CREDENTIALS = new HTTPError("invalid_credentials", 401);

    public String toResponse(Response response){
        response.status(this.errorCode);
        return new Gson().toJson(this.response);
    }
    public int getErrorCode(){ return this.errorCode; }
    public HTTPError put(String key, Object value){ this.response.put(key, value); return this; }

    public String print(){ return new Gson().toJson(this.response); }

}
