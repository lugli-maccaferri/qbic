package com.github.luglimaccaferri.qbic.http.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import spark.Response;

import java.util.HashMap;

public interface JSONResponse {

    JSONResponse put(String key, Object value) throws JsonProcessingException;
    String toResponse(Response response);

    String print();

}
