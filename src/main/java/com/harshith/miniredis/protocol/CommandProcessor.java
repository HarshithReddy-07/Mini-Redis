package com.harshith.miniredis.protocol;

import java.io.IOException;
import java.util.List;

import com.harshith.miniredis.storage.StorageEngine;

public class CommandProcessor {

    private final StorageEngine engine;

    public CommandProcessor( StorageEngine engine ){
        this.engine = engine;
    }

    public String execute( List<String> command){

        if(command == null){
            return "ERROR";
        }

        int size = command.size();

        if(size == 0){
            return "ERROR Empty Command";
        }

        String op = command.get(0).toUpperCase();
        
        try{
            switch (op) {
                case "PING":
                    if(size != 1)
                        return "ERROR Wrong number of arguments";
                    return "PONG";

                case "SET":
                    if (size != 3)
                        return "ERROR Wrong number of arguments";
                    engine.set(command.get(1), command.get(2));
                    return "OK";

                case "GET":
                    if (size != 2)
                        return "ERROR Wrong number of arguments";
                    return engine.get(command.get(1));

                case "DEL":
                    if (size != 2)
                        return "ERROR Wrong number of arguments";
                    engine.delete(command.get(1));
                    return "OK";    

                case "SNAPSHOT":
                    if(size != 1)
                        return "ERROR Wrong number of arguments";
                    engine.snapshot();
                    return "OK";

                case "EXIT":
                    return "BYE";    

                default:
                    return "ERROR Unknown Command";
            }
        }catch(IOException e){
            return "ERROR" + e.getMessage();
        }
    }
}
