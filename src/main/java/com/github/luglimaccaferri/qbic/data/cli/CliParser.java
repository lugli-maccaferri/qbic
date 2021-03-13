package com.github.luglimaccaferri.qbic.data.cli;

import com.github.luglimaccaferri.qbic.errors.cli.InvalidArgumentException;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import java.util.Arrays;
import java.util.HashMap;

public class CliParser {

    public static HashMap<String, CliItem<?>> options = new HashMap<>();

    private CliParser(){} // evitiamo che venga istanziata

    public static CliItem<String> string(String key){

        CliItem<String> item = new CliStringItem();
        item.setKey(key);

        options.put(key, item);
        return item;

    }

    public static CliItem<Integer> integer(String key){

        CliItem<Integer> item = new CliIntegerItem();
        item.setKey(key);

        options.put(key, item);
        return item;

    }

    public static CliItem<Boolean> bool(String key){

        CliItem<Boolean> item = new CliBooleanItem();
        item.setKey(key);

        options.put(key, item);
        return item;

    }

    public static void printConfig(){

        options
                .forEach((key, value) -> System.out.println(value.toString()));

    }

    public static void fromArgs(String... args) throws Exception {

            String[] parsed = Arrays.stream(args)
                    .map(str -> str.split("--")[1])
                    .toArray(String[]::new);

            for(String arg: parsed){ // non posso throware exception negli streams, quindi devo usare per forza un bel for of...

                String[] split = arg.split("=");
                String key = split[0], value = split[1];

                System.out.println(key);

                if(options.containsKey(key)) {

                    CliItem<?> item = options.get(key);

                    if(item instanceof CliStringItem)
                        ((CliStringItem) item).setValue(value);
                    else if(item instanceof CliIntegerItem){
                        if (!TypeUtils.isInteger(value))
                            throw new InvalidArgumentException("InvalidArgumentException: args." + key + " must be an int!");
                        ((CliIntegerItem) item).setValue(Integer.valueOf(value));
                    }
                    else if(item instanceof CliBooleanItem){
                        ((CliBooleanItem) item).setValue(Boolean.parseBoolean(value));
                    }

                }

            }

    }

}
