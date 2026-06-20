package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.harshith.miniredis.storage.StorageEngine;

public class WALTest {

    @TempDir
    Path tempDir;
    
    @Test
    void testSetRecovery() throws IOException {

        Path wal = tempDir.resolve("test.wal");

        try(StorageEngine engine =
            new StorageEngine(wal.toString());){
                engine.set("name", "Harshith");
                engine.set("city", "Hyderabad");
        }
        
        try(StorageEngine recovered =
            new StorageEngine(wal.toString());
        ){
            assertEquals(
                "Harshith",
                recovered.get("name")
            );
            assertEquals(
                "Hyderabad",
                recovered.get("city")
            );
        }
    }

    @Test
    void testDeleteRecovery() throws IOException {

        Path wal = tempDir.resolve("test.wal");

        try(StorageEngine engine =
            new StorageEngine(wal.toString());
        ){
            engine.set("user", "Alice");
            engine.set("age", "22");
    
            engine.delete("user");
        }

        try(StorageEngine recovered =
            new StorageEngine(wal.toString());
        ){
            assertNull(
                recovered.get("user")
            );
            assertEquals(
                "22",
                recovered.get("age")
            );
        }
    }

    @Test
    void testWriteRecovery() throws IOException{

        Path wal = tempDir.resolve("test.wal");

        try(StorageEngine engine =
            new StorageEngine(wal.toString());
        ){
            engine.set("x", "1");
            engine.set("x", "2");
            engine.set("x", "3");
        }  

        try(StorageEngine recoverd = 
            new StorageEngine(wal.toString());
        ){
            assertEquals(
                "3", 
                recoverd.get("x")
            );    
        }
    }

    @Test
    void testEmptyWal() throws IOException{

        Path wal = tempDir.resolve("test.wal");

        try(StorageEngine engine =
            new StorageEngine(wal.toString());
        ){
            assertNull(
                engine.get("user")
            );    
        }
    }
}
