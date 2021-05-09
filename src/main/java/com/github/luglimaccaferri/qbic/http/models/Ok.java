package com.github.luglimaccaferri.qbic.http.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Response;

import java.util.HashMap;

public class Ok implements JSONResponse{

    private HashMap<String, Object> response = new HashMap<String, Object>();

    public Ok(){ response.put("success", true); }


    public String toResponse(Response response){
        response.status(200);
        return new Gson().toJson(this.response);
    }
    public Ok put(String key, Object value){ this.response.put(key, value); return this; }

    public String print(){ return new Gson().toJson(this.response); }
    public static final Ok SUCCESS = new Ok();


}