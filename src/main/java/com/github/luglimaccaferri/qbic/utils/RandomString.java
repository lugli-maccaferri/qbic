package com.github.luglimaccaferri.qbic.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class RandomString {

    private static SecureRandom random = new SecureRandom();
    private static String alphanumericCharset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private RandomString(){};

    public static String generateAlphanumeric(int length){

        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < length; i++)
            buffer.append(alphanumericCharset.charAt(random.nextInt(alphanumericCharset.length())));

        return buffer.toString();

    }

}
