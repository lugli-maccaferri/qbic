package com.github.luglimaccaferri.qbic.data.cli;

public class CliStringItem extends CliItem<String>{

    public CliStringItem(){

        super();

    }

    @Override
    public CliItem<String> defaultValue(String value) {

        super.value = value;
        return this;

    }

    @Override
    public void setValue(String value) {

        super.value = value;

    }
}
