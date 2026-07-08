package com.harshith.miniredis.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.harshith.miniredis.protocol.CommandProcessor;

public class ClientHandler implements Runnable{

    private final Socket socket;
    private final CommandProcessor processor;

    public ClientHandler(Socket socket, CommandProcessor processor){
        this.processor = processor;
        this.socket = socket;
    }

    @Override
    public void run(){
        try(
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            );
            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            );
        ){
            writer.println("MiniRedis Ready");
            while(true){
                String command = reader.readLine();
                if(command == null){
                    break;
                }
                String response = processor.execute(command);
                writer.println(response);
                if("BYE".equals(response)){
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
}
