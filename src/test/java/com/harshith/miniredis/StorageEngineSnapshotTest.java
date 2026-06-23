package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.harshith.miniredis.storage.StorageEngine;

public class StorageEngineSnapshotTest {

    @TempDir
    Path tempDir;

    @Test
    void testSnapshotRecovery()
            throws IOException {

        Path wal =
                tempDir.resolve("test.wal");

        try(StorageEngine engine =
                new StorageEngine(
                        wal.toString())) {

            engine.set("a", "1");
            engine.set("b", "2");

            engine.snapshot();
        }

        try(StorageEngine recovered =
                new StorageEngine(
                        wal.toString())) {

            assertEquals(
                    "1",
                    recovered.get("a"));

            assertEquals(
                    "2",
                    recovered.get("b"));
        }
    }

    @Test
    void testSnapshotPlusWalRecovery()
            throws IOException {

        Path wal =
                tempDir.resolve("test.wal");

        try(StorageEngine engine =
                new StorageEngine(
                        wal.toString())) {

            engine.set("a", "1");
            engine.set("b", "2");

            engine.snapshot();

            engine.set("b", "20");
            engine.set("c", "3");
        }

        try(StorageEngine recovered =
                new StorageEngine(
                        wal.toString())) {

            assertEquals(
                    "1",
                    recovered.get("a"));

            assertEquals(
                    "20",
                    recovered.get("b"));

            assertEquals(
                    "3",
                    recovered.get("c"));
        }
    }

    @Test
    void testWalCompaction()
            throws IOException {

        Path wal =
                tempDir.resolve("test.wal");

        try(StorageEngine engine =
                new StorageEngine(
                        wal.toString())) {

            for(int i = 0; i < 100; i++) {

                engine.set(
                        "key" + i,
                        "value" + i);
            }

            engine.snapshot();
        }

        assertEquals(
                0,
                java.nio.file.Files.size(wal));
    }

    @Test
    void testWritesAfterSnapshot()
            throws IOException {

        Path wal =
                tempDir.resolve("test.wal");

        try(StorageEngine engine =
                new StorageEngine(
                        wal.toString())) {

            engine.set("a", "1");

            engine.snapshot();

            engine.set("b", "2");
        }

        try(StorageEngine recovered =
                new StorageEngine(
                        wal.toString())) {

            assertEquals(
                    "1",
                    recovered.get("a"));

            assertEquals(
                    "2",
                    recovered.get("b"));
        }
    }
}