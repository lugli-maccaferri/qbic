package com.github.luglimaccaferri.qbic.utils;

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
}
