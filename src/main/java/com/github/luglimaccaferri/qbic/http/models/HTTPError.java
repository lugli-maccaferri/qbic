package com.github.luglimaccaferri.qbic.http.models;

import spark.Response;

public class HTTPError {

    private String error_message;
    private int error_code;

    public HTTPError(String error_message, int error_code){

        this.error_message = error_message;
        this.error_code = error_code;

    }



}
