package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.harshith.miniredis.server.MiniRedisServer;
import com.harshith.miniredis.storage.StorageEngine;

public class ServerTest {

    @TempDir
    Path tempDir;

    private StorageEngine engine;
    private MiniRedisServer server;

    @BeforeEach
    void setup() throws Exception {

        Path wal = tempDir.resolve("test.wal");

        engine = new StorageEngine(wal.toString());

        server = new MiniRedisServer(0, engine);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception ignored) {
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(300);
    }

    @AfterEach
    void cleanup() throws Exception {
        server.stop();
    }

    @Test
    void testPing() throws Exception {

        try(TestClient client =
                new TestClient()) {
            assertEquals(
                    "PONG",
                    client.send("PING"));
        }
    }

    @Test
    void testSetGet() throws Exception {

        try(TestClient client =
                new TestClient()) {
            assertEquals(
                    "OK",
                    client.send(
                            "SET name Harshith"));
            assertEquals(
                    "Harshith",
                    client.send(
                            "GET name"));
        }
    }

    @Test
    void testMissingKey()
            throws Exception {

        try(TestClient client =
                new TestClient()) {
            assertEquals(
                    "NULL",
                    client.send(
                            "GET missing"));
        }
    }

    @Test
    void testDelete() throws Exception {

        try(TestClient client =
                new TestClient()) {
            client.send(
                    "SET city Hyderabad");
            assertEquals(
                    "OK",
                    client.send(
                            "DEL city"));
            assertEquals(
                    "NULL",
                    client.send(
                            "GET city"));
        }
    }

    @Test
    void testUnknownCommand()
            throws Exception {

        try(TestClient client =
                new TestClient()) {
            assertEquals(
                    "ERROR Unknown Command",
                    client.send("HELLO"));
        }
    }

    @Test
    void testWrongArguments()
            throws Exception {

        try(TestClient client =
                new TestClient()) {

            assertEquals(
                    "ERROR Wrong number of arguments",
                    client.send("SET key"));
        }
    }

    @Test
    void testSnapshot()
            throws Exception {

        try(TestClient client =
                new TestClient()) {
            client.send(
                    "SET a 10");
            assertEquals(
                    "OK",
                    client.send(
                            "SNAPSHOT"));
        }
    }

    @Test
    void testPersistence()
            throws Exception{
        
        try(TestClient client1 =
                new TestClient();
            TestClient client2 =
                new TestClient()) {
            client1.send("SET user Harshith");

            server.stop();

            assertEquals(
                "Harshith",
                client2.send(
                        "GET user"));
        }
    }

    private class TestClient implements AutoCloseable {

        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        TestClient() throws IOException {

            socket = new Socket(
                    "localhost",
                    server.getPort());

            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true);

            assertEquals(
                    "MiniRedis Ready",
                    in.readLine());
        }

        String send(String command) throws IOException {

            out.println(command);

            return in.readLine();
        }

        @Override
        public void close()
                throws IOException {

            socket.close();
        }
    }
}
