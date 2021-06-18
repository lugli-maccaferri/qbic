package com.github.luglimaccaferri.qbic.http.models;

import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class Ok implements JSONResponse{

    private HashMap<String, Object> response = new HashMap<String, Object>();

    public Ok(){ response.put("success", true); }

    public synchronized String toResponse(Response response){
        response.status(200);
        return new Gson().toJson(this.response);
    }
    public synchronized Ok put(String key, Object value) {
        if(value instanceof Map<?, ?>) this.response.put(key, TypeUtils.serializeMap((Map<?, ?>) value));
        else this.response.put(key, value);
        return this;
    }

    public synchronized String print(){ return new Gson().toJson(this.response); }
    public static final Ok SUCCESS = new Ok();


}