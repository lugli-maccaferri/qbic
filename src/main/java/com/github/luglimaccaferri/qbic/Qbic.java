package com.github.luglimaccaferri.qbic;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;

public class Qbic{

    public static void main(String... args) {

        // java -jar qbic.jar --parametro=valore
        CliParser
                .string("config")
                .defaultValue("./config.json");
        CliParser
                .u16("port")
                .defaultValue((short) 3001);

        try{

            CliParser.fromArgs(args);

            Core core = new Core();
            core.init();

        }catch(Exception err){

            err.printStackTrace();

        }

    }
}