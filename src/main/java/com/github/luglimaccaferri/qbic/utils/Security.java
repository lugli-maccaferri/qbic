package com.github.luglimaccaferri.qbic.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;

import java.security.interfaces.RSAPublicKey;

public class Security {
    public static String bytesToHex(byte[] bytes){
        // lenta, ma tanto ci interessa solo una volta ogni tanto
        StringBuilder sb = new StringBuilder();
        for(byte b: bytes){
            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }
    public static byte[] hexToBytes(String hex){

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i+= 2){
            data[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16)
            );
        }

        return data;

    }

    public static DecodedJWT verifyJWT(String token) throws HTTPError {

        try{

            return Core.getVerifier().verify(token);

        }catch(JWTVerificationException e){

            // debug
            e.printStackTrace();
            if(e instanceof TokenExpiredException)
                throw HTTPError.EXPIRED_CREDENTIALS;

            throw HTTPError.FORBIDDEN;

        }

    }

}
