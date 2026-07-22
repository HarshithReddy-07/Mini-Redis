package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.harshith.miniredis.protocol.RESPParser;

public class RESPParserTest {
    private InputStream input(String data) {
        return new ByteArrayInputStream(
                data.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testPing() throws Exception {
        RESPParser parser = new RESPParser();
        List<String> result = parser.parse(
                input("*1\r\n$4\r\nPING\r\n"));
        assertEquals(
                List.of("PING"),
                result);
    }

    @Test
    void testGet() throws Exception {
        RESPParser parser = new RESPParser();
        List<String> result = parser.parse(
                input("*2\r\n$3\r\nGET\r\n$4\r\nname\r\n"));
        assertEquals(
                List.of("GET", "name"),
                result);
    }

    @Test
    void testSet() throws Exception {
        RESPParser parser = new RESPParser();
        List<String> result = parser.parse(
                input("*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$8\r\nHarshith\r\n"));
        assertEquals(
                List.of("SET", "name", "Harshith"),
                result);
    }

    @Test
    void testInvalidArray() {

        RESPParser parser = new RESPParser();

        assertThrows(IOException.class,
                () -> parser.parse(
                        input("$3\r\nGET\r\n")));
    }

    @Test
    void testUnexpectedEOF() {
        RESPParser parser = new RESPParser();
        assertThrows(IOException.class,
                () -> parser.parse(
                        input("*2\r\n$3\r\nGET\r\n")));
    }
}
