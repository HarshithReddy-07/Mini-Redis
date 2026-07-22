package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
                            "SET", "name", "Harshith"));
            assertEquals(
                    "Harshith",
                    client.send(
                            "GET", "name"));
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
                            "GET", "missing"));
        }
    }

    @Test
    void testDelete() throws Exception {

        try(TestClient client =
                new TestClient()) {
            client.send(
                    "SET", "city", "Hyderabad");
            assertEquals(
                    "OK",
                    client.send(
                            "DEL", "city"));
            assertEquals(
                    "NULL",
                    client.send(
                            "GET", "city"));
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
                    client.send("SET", "key"));
        }
    }

    @Test
    void testSnapshot()
            throws Exception {

        try(TestClient client =
                new TestClient()) {
            client.send(
                    "SET", "a", "10");
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
            client1.send("SET", "user", "Harshith");

            server.stop();

            assertEquals(
                "Harshith",
                client2.send(
                        "GET", "user"));
        }
    }

    private class TestClient implements AutoCloseable {

        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        TestClient() throws IOException {

            socket = new Socket(
                    "localhost",
                    server.getPort());

            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        String send(String... args) throws IOException {

            out.write(
                    resp(args).getBytes(StandardCharsets.UTF_8));

            out.flush();

            return readResponse();
        }

        private String resp(String... args) {

            StringBuilder sb = new StringBuilder();

            sb.append("*")
            .append(args.length)
            .append("\r\n");

            for (String arg : args) {

                byte[] bytes =
                        arg.getBytes(StandardCharsets.UTF_8);

                sb.append("$")
                .append(bytes.length)
                .append("\r\n")
                .append(arg)
                .append("\r\n");
            }

            return sb.toString();
        }

        private String readResponse() throws IOException {

            int type = in.read();

            if (type == -1) {
                throw new IOException("Server closed connection");
            }

            switch (type) {

                case '+':
                    return readLine();

                case '-':
                    return "ERROR " + readLine().substring(4);

                case '$':

                    int length =
                            Integer.parseInt(readLine());

                    if (length == -1) {
                        return null;
                    }

                    byte[] bytes =
                            in.readNBytes(length);

                    if (bytes.length != length) {
                        throw new IOException(
                                "Unexpected EOF");
                    }

                    expectCRLF();

                    return new String(
                            bytes,
                            StandardCharsets.UTF_8);

                default:
                    throw new IOException(
                            "Unknown RESP type: "
                                    + (char) type);
            }
        }

        private String readLine() throws IOException {

            StringBuilder sb = new StringBuilder();

            while (true) {

                int b = in.read();

                if (b == -1) {
                    throw new IOException(
                            "Unexpected EOF");
                }

                if (b == '\r') {

                    int next = in.read();

                    if (next != '\n') {
                        throw new IOException(
                                "Expected LF");
                    }

                    break;
                }

                sb.append((char) b);
            }

            return sb.toString();
        }

        private void expectCRLF() throws IOException {

            if (in.read() != '\r'
                    || in.read() != '\n') {
                throw new IOException(
                        "Missing CRLF");
            }
        }

        @Override
        public void close() throws IOException {

            socket.close();
        }
    }
}
