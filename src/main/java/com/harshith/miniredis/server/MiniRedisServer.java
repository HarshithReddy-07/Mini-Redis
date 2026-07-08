package com.harshith.miniredis.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.harshith.miniredis.protocol.CommandProcessor;
import com.harshith.miniredis.storage.StorageEngine;

public class MiniRedisServer {

    private final int port;
    private final StorageEngine engine;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public MiniRedisServer( int port, StorageEngine engine){
        this.port = port;
        this.engine = engine;
    }

    public void start() throws IOException{
        running = true;
        serverSocket = new ServerSocket(port);
        System.out.println("MiniRedis is listening on port " + serverSocket.getLocalPort());
        while(running){
            try {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, new CommandProcessor(engine));
                Thread thread =
                        new Thread(handler);

                thread.setName(
                        "client-" + thread.threadId());

                thread.start();
            }catch (SocketException e) {
                if (running) {
                    throw e;
                }
            }
        }
    }
    

    public void stop() throws IOException{
        running = false;
        if(serverSocket != null){
            serverSocket.close();
        }
        engine.close();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

}
