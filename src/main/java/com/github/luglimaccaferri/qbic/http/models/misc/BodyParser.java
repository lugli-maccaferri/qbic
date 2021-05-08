package com.github.luglimaccaferri.qbic.http.models.misc;

import spark.Request;

import java.util.HashMap;

public class BodyParser {

    private Request request;
    private HashMap<String, String> body = new HashMap<String, String>();

    public BodyParser(Request request){
        this.request = request;

    }

    public String get(String key){
        return this.body.get(key);
    }

    private void put(String key, String value){
        this.body.put(key, value);
    }

}
