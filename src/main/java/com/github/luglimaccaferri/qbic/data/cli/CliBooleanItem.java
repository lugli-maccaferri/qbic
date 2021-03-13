package com.github.luglimaccaferri.qbic.data.cli;

public class CliBooleanItem extends CliItem<Boolean>{

    public CliBooleanItem(){

        super();

    }

    public void setValue(Boolean value) {

        super.value = value;

    }

    public CliItem<Boolean> defaultValue(Boolean value) {

        super.value = value;
        return this;

    }
}
