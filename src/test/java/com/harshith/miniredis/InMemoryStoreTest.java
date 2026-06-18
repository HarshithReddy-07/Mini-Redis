package com.harshith.miniredis;

import org.junit.jupiter.api.Test;

import com.harshith.miniredis.store.InMemoryStore;
import com.harshith.miniredis.store.KeyValueStore;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStoreTest{
    
    @Test
    void shouldSetAndGetValue(){
        KeyValueStore store = new InMemoryStore();

        store.set("City", "Vizag");

        assertTrue(
            store.delete("City")
        );

        assertNull(
            store.get("City")
        );
    }

    @Test
    void shouldCheckExistence(){
        KeyValueStore store = new InMemoryStore();

        store.set("City", "Vizag");

        assertTrue(
            store.exists("City")
        );

        assertFalse(
            store.exists("Town")
        );
    }
}