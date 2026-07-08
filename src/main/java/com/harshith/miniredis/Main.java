package com.harshith.miniredis;

import com.harshith.miniredis.server.MiniRedisServer;
import com.harshith.miniredis.storage.StorageEngine;

public class Main {
    public static void main(String[] args) throws Exception{
        
        StorageEngine engine = new StorageEngine("data/database.wal");
        MiniRedisServer server = new MiniRedisServer(6379, engine);
        server.start();
    }
}
