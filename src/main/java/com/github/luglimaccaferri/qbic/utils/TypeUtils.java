package com.github.luglimaccaferri.qbic.utils;

import com.auth0.jwt.interfaces.Claim;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class TypeUtils {

    private TypeUtils(){}

    public static boolean isUrl(String url) throws MalformedURLException {

        try{

            URL u = new URL(url);
            u.toURI();

            return true;

        }catch(MalformedURLException | URISyntaxException e){

            return false;

        }

    }

    public static JsonObject serializeMap(Map<?, ?> value){
        JsonObject obj = new JsonObject();
        ((Map<?, ?>) value).forEach(
                (k, v) -> {

                    if(v == null) return;
                    if(v instanceof Map){
                        obj.add(k.toString(), serializeMap((Map<?, ?>) v));
                        return;
                    }

                    String item;

                    if(v instanceof Claim)
                        item = ((Claim) v).asString();
                    else item = v.toString();

                    if(isInteger(item))
                        obj.addProperty(k.toString(), Integer.valueOf(item));
                    else if(isDouble(item))
                        obj.addProperty(k.toString(), Double.valueOf(item));
                    else if(isBoolean(item))
                        obj.addProperty(k.toString(), Boolean.valueOf(item));
                    else obj.addProperty(k.toString(), item);
                }
        );
        return obj;
    }

    public static boolean isBoolean(String item){

        return "true".equalsIgnoreCase(item) || "false".equalsIgnoreCase(item);

    }

    public static boolean isDouble(String item){

        if(item == null) return false;

        try{
            Double.valueOf(item);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isInteger(String item){

        if(item == null) return false;

        try{

            Integer.valueOf(item);
            return true;

        }catch(NumberFormatException e){

            return false;

        }

    }

}
