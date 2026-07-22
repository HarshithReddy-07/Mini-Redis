package com.harshith.miniredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.harshith.miniredis.protocol.RESPEncoder;

public class RESPEncoderTest {
    @Test
    void testSimpleString() {
        RESPEncoder encoder =
                new RESPEncoder();
        assertEquals(
                "+OK\r\n",
                new String(
                        encoder.simpleString("OK"),
                        StandardCharsets.UTF_8));
    }

    @Test
    void testError() {
        RESPEncoder encoder =
                new RESPEncoder();
        assertEquals(
                "-ERR Wrong Command\r\n",
                new String(
                        encoder.error("Wrong Command"),
                        StandardCharsets.UTF_8));
    }

    @Test
    void testBulkString() {
        RESPEncoder encoder =
                new RESPEncoder();
        assertEquals(
                "$8\r\nHarshith\r\n",
                new String(
                        encoder.bulkString("Harshith"),
                        StandardCharsets.UTF_8));
    }

    @Test
    void testNullBulkString() {
        RESPEncoder encoder =
                new RESPEncoder();
        assertEquals(
                "$-1\r\n",
                new String(
                        encoder.nullBulkString(),
                        StandardCharsets.UTF_8));
    }

    @Test
    void testUnicodeBulkString() {
        RESPEncoder encoder =
                new RESPEncoder();
        String expected =
                "$6\r\n東京\r\n";
        assertEquals(
                expected,
                new String(
                        encoder.bulkString("東京"),
                        StandardCharsets.UTF_8));
    }
}
