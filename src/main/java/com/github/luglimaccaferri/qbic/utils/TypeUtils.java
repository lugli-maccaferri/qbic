package com.github.luglimaccaferri.qbic.utils;

import com.auth0.jwt.interfaces.Claim;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

public class TypeUtils {

    private TypeUtils(){}

    public static ByteBuffer BYTE_BAD_REQUEST = ByteBuffer.allocate(3).put((byte) 4).put((byte) 0).put((byte) 0); // 0x400
    public static ByteBuffer BYTE_INVALID_CREDENTIALS = ByteBuffer.allocate(3).put((byte) 4).put((byte) 0).put((byte) 1); // 0x401
    public static ByteBuffer BYTE_FORBIDDEN = ByteBuffer.allocate(3).put((byte) 4).put((byte) 0).put((byte) 3); // 0x403
    public static ByteBuffer BYTE_NOT_FOUND = ByteBuffer.allocate(3).put((byte) 4).put((byte) 0).put((byte) 4); // 0x404
    public static ByteBuffer BYTE_GENERIC_ERROR = ByteBuffer.allocate(3).put((byte) 5).put((byte) 0).put((byte) 0); // 0x500
    public static ByteBuffer SUCCESS = ByteBuffer.allocate(3).put((byte) 2).put((byte) 0).put((byte) 0); // 0x200

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
