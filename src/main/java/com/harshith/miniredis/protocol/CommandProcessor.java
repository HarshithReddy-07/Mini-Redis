package com.harshith.miniredis.protocol;

import java.io.IOException;

import com.harshith.miniredis.storage.StorageEngine;

public class CommandProcessor {

    private final StorageEngine engine;

    public CommandProcessor( StorageEngine engine ){
        this.engine = engine;
    }

    public String execute( String command){

        if(command == null){
            return "ERROR";
        }

        command = command.trim();

        if(command.isEmpty()){
            return "ERROR Empty Command";
        }

        String[] parts = command.split("\\s+");
        String op = parts[0].toUpperCase();
        
        try{
            switch (op) {
                case "PING":
                    if(parts.length != 1)
                        return "ERROR Wrong number of arguments";
                    return "PONG";

                case "SET":
                    if (parts.length != 3)
                        return "ERROR Wrong number of arguments";
                    engine.set(parts[1], parts[2]);
                    return "OK";

                case "GET":
                    if (parts.length != 2)
                        return "ERROR Wrong number of arguments";
                    String value = engine.get(parts[1]);
                    return value != null ? value : "NULL";

                case "DEL":
                    if (parts.length != 2)
                        return "ERROR Wrong number of arguments";
                    engine.delete(parts[1]);
                    return "OK";    

                case "SNAPSHOT":
                    if(parts.length != 1)
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
