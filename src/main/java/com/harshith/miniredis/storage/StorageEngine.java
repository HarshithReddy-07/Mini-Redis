package com.harshith.miniredis.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.harshith.miniredis.store.InMemoryStore;

public class StorageEngine implements AutoCloseable {

    private final InMemoryStore store;
    private final WALManager wal;
    private final BinarySnapshotManager snapshotManager;
    private final Object writeLock = new Object();

    public StorageEngine(String walPath)
        throws IOException {

        this.store = new InMemoryStore();

        this.wal = new WALManager(walPath);

        Path snapshotPath =
                Paths.get(walPath)
                    .resolveSibling("snapshot.rdb");

        this.snapshotManager =
                new BinarySnapshotManager(
                        snapshotPath);

        recover();
    }

    public void snapshot()
        throws IOException {
            synchronized (writeLock){
                snapshotManager.createSnapshot(store.snapshot());
                wal.reset();
            }

    }

    private void recover() throws IOException{
        store.clear();
        store.load(snapshotManager.loadSnapshot());
        wal.recover(record -> {
            String[] parts = record.split(" ");
            if(parts[0].equals("SET")){
                if(parts.length != 3){
                    //Corruption
                    return;
                }
                store.set(parts[1], parts[2]);
            }else if(parts[0].equals("DEL")){
                if(parts.length != 2){
                    //Corruption
                    return;
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
