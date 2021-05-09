package com.github.luglimaccaferri.qbic.http.models;

import spark.Response;

import java.util.HashMap;

public interface JSONResponse {

    JSONResponse put(String key, Object value);
    String toResponse(Response response);

    String print();

}
