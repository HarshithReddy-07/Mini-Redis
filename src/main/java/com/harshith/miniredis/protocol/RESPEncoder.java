package com.harshith.miniredis.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class RESPEncoder {

    public byte[] simpleString(String value) {
        return ("+" + value + "\r\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    public byte[] error(String message) {
    return ("-ERR " + message + "\r\n")
            .getBytes(StandardCharsets.UTF_8);
    }

    public byte[] bulkString(String value) {
        byte[] bytes =
                value.getBytes(StandardCharsets.UTF_8);
        String header =
                "$" + bytes.length + "\r\n";
        ByteArrayOutputStream out =
                new ByteArrayOutputStream();
        out.writeBytes(
                header.getBytes(StandardCharsets.UTF_8));
        out.writeBytes(bytes);
        out.writeBytes(
                "\r\n".getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    public byte[] nullBulkString() {
        return "$-1\r\n"
                .getBytes(StandardCharsets.UTF_8);
    }
}

