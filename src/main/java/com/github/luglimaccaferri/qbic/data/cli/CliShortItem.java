package com.github.luglimaccaferri.qbic.data.cli;

public class CliShortItem extends CliItem<Short>{

    public CliShortItem(){

        super();

    }

    public void setValue(Short value) {

        super.value = value;

    }

    public CliItem<Short> defaultValue(Short value) {

        super.value = value;
        return this;

    }
}
