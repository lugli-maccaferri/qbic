package com.github.luglimaccaferri.qbic.data.cli;

import com.github.luglimaccaferri.qbic.errors.cli.InvalidArgumentException;
import com.github.luglimaccaferri.qbic.utils.TypeUtils;
import java.util.Arrays;
import java.util.HashMap;

public class CliParser {

    public static HashMap<String, CliItem<?>> options = new HashMap<>();

    private CliParser(){} // evitiamo che venga istanziata

    public static CliItem<?> arg(String key, CliItem.Type type) {

        CliItem<?> value = ( type == CliItem.Type.INT ) ? new CliItem<Integer>() : ( type == CliItem.Type.STRING ) ? new CliItem<String>() : new CliItem<Boolean>();

        value.setType(type);
        value.setKey(key);
        options.put(key, value);

        return options.get(key);

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

                if(options.containsKey(key)) {

                    CliItem<?> item = options.get(key);

                    switch (item.getType()) {
                        case INT -> {
                            if (!TypeUtils.isInteger(value))
                                throw new InvalidArgumentException("InvalidArgumentException: args." + key + " must be an int!");
                            item.setValue(Integer.valueOf(value));
                        }
                        case STRING -> item.setValue(value);
                        case BOOLEAN -> item.setValue(Boolean.parseBoolean(value));
                        default -> throw new Exception("args." + key + " has no type");
                    }

                }

            }

    }

}
