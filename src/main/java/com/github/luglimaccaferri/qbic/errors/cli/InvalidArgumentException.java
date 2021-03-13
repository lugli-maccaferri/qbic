package com.github.luglimaccaferri.qbic.errors.cli;

public class InvalidArgumentException extends Exception {
    public InvalidArgumentException(String err) {
        super(err);
    }
}