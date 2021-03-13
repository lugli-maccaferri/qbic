package com.github.luglimaccaferri.qbic.utils;

public class TypeUtils {

    private TypeUtils(){}

    public static boolean isInteger(String item){

        try{

            Integer.valueOf(item);
            return true;

        }catch(NumberFormatException e){

            return false;

        }

    }

}
