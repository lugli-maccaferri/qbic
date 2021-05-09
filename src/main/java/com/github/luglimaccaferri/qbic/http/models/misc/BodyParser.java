package com.github.luglimaccaferri.qbic.http.models.misc;

import com.github.luglimaccaferri.qbic.http.Router;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import io.netty.handler.codec.json.JsonObjectDecoder;
import spark.Request;

import java.util.HashMap;

public class BodyParser {

    private Request request;
    private HashMap<String, String> body = new HashMap<String, String>();

    public BodyParser(Request request) throws MalformedJsonException {
        this.request = request;
        this.parseBody();
    }

    public String get(String key){
        return this.body.get(key);
    }
    private void put(String key, String value){
        this.body.put(key, value);
    }
    private void parseBody(){

        switch (this.request.contentType()){
            case "application/json":

                JsonObject object = (JsonObject) JsonParser.parseString(request.body());

                break;

            case "multipart/form-data":

                Router.logger.info("ok");

                break;

            default: break;
        }

    }

}
