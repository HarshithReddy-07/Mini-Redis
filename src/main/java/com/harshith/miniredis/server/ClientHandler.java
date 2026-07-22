package com.harshith.miniredis.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import com.harshith.miniredis.protocol.CommandProcessor;
import com.harshith.miniredis.protocol.RESPEncoder;
import com.harshith.miniredis.protocol.RESPParser;

public class ClientHandler implements Runnable{

    private final Socket socket;
    private final CommandProcessor processor;
    private final RESPEncoder encoder;
    private final RESPParser parser;

    public ClientHandler(Socket socket, CommandProcessor processor){
        this.processor = processor;
        this.socket = socket;
        this.parser = new RESPParser();
        this.encoder = new RESPEncoder();
    }

    @Override
    public void run(){
        try(
            InputStream in =
                socket.getInputStream();

            OutputStream out =
                socket.getOutputStream();
        ){
            while(true){
                List<String> command = parser.parse(in);
                if(command == null){
                    break;
                }
                String result = processor.execute(command);
                byte[] response = encodeResponse(result);
                out.write(response);
                out.flush();
                if("BYE".equals(result)){
                    break;
                }
            }
        } catch (IOException ignored) {
        }finally{
            try{
                socket.close();
            }catch(IOException ignored){
            }
        }
    }

    private byte[] encodeResponse(String result){
        if(result == null){
            return encoder.nullBulkString();
        }
        switch (result) {
            case "OK":
            case "PONG":
            case "BYE":
                return encoder.simpleString(result);
        
            default:
                if(result.startsWith("ERROR")){
                    return encoder.error(result.substring(6));
                }
                return encoder.bulkString(result);
        }
    }
}
