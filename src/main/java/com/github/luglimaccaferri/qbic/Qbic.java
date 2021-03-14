package com.github.luglimaccaferri.qbic;
import com.github.luglimaccaferri.qbic.data.cli.CliParser;
import com.github.luglimaccaferri.qbic.http.Router;

public class Qbic{

    /*public static void init(String... args){

        try{

            CliParser.fromArgs(args);

            router = new Router((short) 3000);
            router.init();

        }catch(Exception err){

            err.printStackTrace();

        }

    }*/

    public static void main(String... args) {

        // java -jar qbic.jar --parametro=valore
        CliParser
                .string("config")
                .defaultValue("./config.json");

        CliParser
                .u16("port")
                .defaultValue((short) 3000);

        CliParser
                .bool("debug")
                .defaultValue(false);

        try{

            CliParser.fromArgs(args);

            Core core = new Core();
            core.init();

        }catch(Exception err){

            err.printStackTrace();

        }

    }
}
