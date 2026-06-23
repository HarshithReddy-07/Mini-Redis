package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.harshith.miniredis.storage.BinarySnapshotManager;

public class BinarySnapshotManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testEmptySnapshot() throws IOException {

        Path snapshot =
                tempDir.resolve("snapshot.rdb");

        BinarySnapshotManager manager =
                new BinarySnapshotManager(snapshot);

        manager.createSnapshot(new HashMap<>());

        Map<String,String> loaded =
                manager.loadSnapshot();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void testSnapshotRoundTrip()
            throws IOException {

        Path snapshot =
                tempDir.resolve("snapshot.rdb");

        BinarySnapshotManager manager =
                new BinarySnapshotManager(snapshot);

        Map<String,String> original =
                new HashMap<>();

        original.put("name", "Harshith");
        original.put("city", "Hyderabad");
        original.put("lang", "Java");

        manager.createSnapshot(original);

        Map<String,String> loaded =
                manager.loadSnapshot();

        assertEquals(original, loaded);
    }

    @Test
    void testUnicodeSnapshot()
            throws IOException {

        Path snapshot =
                tempDir.resolve("snapshot.rdb");

        BinarySnapshotManager manager =
                new BinarySnapshotManager(snapshot);

        Map<String,String> original =
                new HashMap<>();

        original.put("name", "హర్షిత్");
        original.put("city", "東京");

        manager.createSnapshot(original);

        Map<String,String> loaded =
                manager.loadSnapshot();

        assertEquals(original, loaded);
    }

    @Test
    void testMissingSnapshot()
            throws IOException {

        Path snapshot =
                tempDir.resolve("missing.rdb");

        BinarySnapshotManager manager =
                new BinarySnapshotManager(snapshot);

        Map<String,String> loaded =
                manager.loadSnapshot();

        assertTrue(loaded.isEmpty());
    }
}