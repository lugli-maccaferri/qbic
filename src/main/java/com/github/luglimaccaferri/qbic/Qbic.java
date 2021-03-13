package com.github.luglimaccaferri.qbic;

import com.github.luglimaccaferri.qbic.data.cli.CliItem;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;

public class Qbic{

    public static void main(String... args) {

        // java -jar qbic.jar --parametro=valore

        CliParser // --config=path/to/config.json
                .arg("config", CliItem.Type.STRING)
                .defaultValue("./config.json");


        try{
            CliParser.fromArgs(args);
            CliParser.printConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
