package com.github.luglimaccaferri.qbic;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;

public class Qbic{

    public static void main(String... args) {

        // java -jar qbic.jar --parametro=valore
        CliParser
                .string("config")
                .defaultValue("./config.json");

        CliParser
                .bool("fancy")
                .defaultValue(true);

        try{
            CliParser.fromArgs(args);
            CliParser.printConfig();
        }catch(Exception err){
            err.printStackTrace();
        }

    }
}
