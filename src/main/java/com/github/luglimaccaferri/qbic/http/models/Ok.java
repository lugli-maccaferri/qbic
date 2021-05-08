package com.github.luglimaccaferri.qbic.http.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class Ok {

    private HashMap<String, Object> response = new HashMap<String, Object>();

    public Ok(){ response.put("success", true); }

    public Ok put(String key, String value){ this.response.put(key, value); return this; }
    public Ok put(String key, int value){ this.response.put(key, value); return this; }
    public Ok put(String key, double value){ this.response.put(key, value); return this; }
    public Ok put(String key, float value){ this.response.put(key, value); return this; }
    public Ok put(String key, boolean value){ this.response.put(key, value); return this; }

    public String print(){ return new Gson().toJson(this.response); }
    public static final String SUCCESS = new Ok().print();


}