package com.github.luglimaccaferri.qbic.data.cli;

public abstract class CliItem<T> {

    protected T value;
    private String key;

    public abstract CliItem<T> defaultValue(T value);
    public abstract void setValue(T value);
    public void setKey(String key){
        this.key = key;
    }

    @Override
    public String toString(){

        return "{ " + this.key + ": " + this.value.toString() + " }";

    }

}
