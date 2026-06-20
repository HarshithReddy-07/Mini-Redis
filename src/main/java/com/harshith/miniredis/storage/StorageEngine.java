package com.harshith.miniredis.storage;

import java.io.IOException;

import com.harshith.miniredis.store.InMemoryStore;

public class StorageEngine implements AutoCloseable {

    private final InMemoryStore store;
    private final WALManager wal;
    private final Object writeLock = new Object();

    public StorageEngine(String walPath) throws IOException{
        this.store = new InMemoryStore();
        this.wal = new WALManager(walPath);
        recover();
    }

    private void recover() throws IOException{
        wal.recover(record -> {
            String[] parts = record.split(" ");
            if(parts[0].equals("SET")){
                if(parts.length != 3){
                    //Corruption
                }
                store.set(parts[1], parts[2]);
            }else if(parts[0].equals("DEL")){
                if(parts.length != 2){
                    //Corruption
                }
                store.delete(parts[1]);
            }
        });
    }

    public void set(String key, String value)
            throws IOException {
        synchronized(writeLock) {
            wal.append("SET " + key + " " + value);
            store.set(key, value);
        }
    }

    public void delete(String key)
            throws IOException {
        synchronized(writeLock) {
            wal.append("DEL " + key);
            store.delete(key);
        }
    }

    public String get(String key){
        return store.get(key);
    }

    @Override
    public void close() throws IOException{
        wal.close();
    }
}
