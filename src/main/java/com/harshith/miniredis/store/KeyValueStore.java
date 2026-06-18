package com.harshith.miniredis.store;

public interface KeyValueStore {
    void set(String key, String value);
    String get(String key);
    boolean delete(String key);
    boolean exists(String key);
    int size();
}