package com.github.luglimaccaferri.qbic.data.cli;

public class CliIntegerItem extends CliItem<Integer>{

    public CliIntegerItem(){

        super();

    }

    public void setValue(Integer value) {

        super.value = value;

    }

    @Override
    public CliItem<Integer> defaultValue(Integer value) {

        super.value = value;
        return this;

    }
}
