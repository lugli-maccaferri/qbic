package com.github.luglimaccaferri.qbic.data.cli;

public final class CliItem <T> {

    public static enum Type {
        STRING,
        INT,
        BOOLEAN
    }
    private T item;
    private String key;
    private Type type;

    public CliItem(){

        this.item = null;

    }

    public CliItem<T> defaultValue(String value){

        this.item = (T) value;
        return this;

    }
    public CliItem<T> defaultValue(Integer value){

        this.item = (T) value;
        return this;

    }
    public CliItem<T> defaultValue(Boolean value){

        this.item = (T) value;
        return this;

    }

    public CliItem<T> setType(Type type){

        this.type = type;
        return this;

    }

    public Type getType(){

        return this.type;

    }

    public void setValue(String value){

        this.item = (T) value;

    }

    public void setValue(Integer value){

        this.item = (T) value;

    }

    public void setValue(Boolean value){

        this.item = (T) value;

    }

    public void setKey(String key){

        this.key = key;

    }


    @Override
    public String toString(){

        return "{ " + this.key + ": " + this.item.toString() + " }";

    }

}
