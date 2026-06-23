package com.harshith.miniredis.store;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore implements KeyValueStore{
    private final ConcurrentHashMap<String, String> storage;

    public InMemoryStore(){
        this.storage = new ConcurrentHashMap<>();
    }

    @Override
    public void set(String key, String value){
        storage.put(key, value);
    }

    @Override
    public String get(String key){
        return storage.get(key);
    }
    
    @Override
    public boolean delete(String key){
        return storage.remove(key) != null;
    }

    @Override
    public boolean exists(String key){
        return storage.containsKey(key);
    }

    @Override
    public int size(){
        return storage.size();
    }

    public Map<String, String> snapshot() {
        return new HashMap<>(storage);
    }

    public void clear(){
        storage.clear();
    }

    public void load(Map<String, String> data){
        storage.clear();
        storage.putAll(data);
    }
}
