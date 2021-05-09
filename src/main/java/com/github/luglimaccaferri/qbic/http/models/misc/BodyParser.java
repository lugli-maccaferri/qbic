package com.github.luglimaccaferri.qbic.http.models.misc;

import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import spark.Request;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class BodyParser {

    private final Request request;
    private final HashMap<String, String> body = new HashMap<String, String>();

    public BodyParser(Request request) throws HTTPError {
        this.request = request;
        this.parseBody();
    }

    public HashMap<String, String> getBody(){
        return this.body;
    }
    public String get(String key){
        return this.body.get(key);
    }
    private void put(String key, String value){
        this.body.put(key, value);
    }
    private void parseBody() throws HTTPError {

        switch (this.request.contentType()){
            case "application/json":

                try{
                    JsonObject object = JsonParser.parseString(request.body()).getAsJsonObject();
                    object.keySet().forEach(key -> {
                        Object value = object.get(key);
                        this.body.put(key, URLDecoder.decode(value.toString(), StandardCharsets.UTF_8));
                    });
                }catch(JsonSyntaxException ex){
                    throw HTTPError.BAD_REQUEST;
                }

                break;

            case "application/x-www-form-urlencoded":

                String[] parameters = request.body().split("&");
                Arrays.stream(parameters).forEach(parameter -> {

                    String[] kv = parameter.split("=");
                    this.body.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));

                });

                break;

            default: break;
        }

    }

}
