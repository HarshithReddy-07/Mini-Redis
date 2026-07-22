package com.harshith.miniredis.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RESPParser {

    public List<String> parse(InputStream in) throws IOException{
        int count = readArrayLength(in);
        List<String> command = new ArrayList<>(count);
        for(int i = 0 ; i < count ; i++){
            command.add(readBulkString(in));
        }
        return command;
    } 

    private int readArrayLength(InputStream in) throws IOException{
        String line = readLine(in);
        if(line.isEmpty() || line.charAt(0) != '*'){
            throw new IOException("Expected array");
        }
        try{
            int length = Integer.parseInt(line.substring(1));
            if(length < 0){
                throw new IOException("Negative Array Length");
            }
            return length;
        }catch(NumberFormatException e){
            throw new IOException("Invalid Array Length");
        }
    }

    private String readBulkString(InputStream in) throws IOException{
        String line = readLine(in);
        if(line.isEmpty() || line.charAt(0) != '$'){
            throw new IOException("Expected Bulk String");
        }
        int length;
        try{
            length = Integer.parseInt(line.substring(1));
            if(length < 0){
                throw new IOException("Negative Bulk String Length");
            }
        }catch(NumberFormatException e){
            throw new IOException("Invalid Bulk String Length");
        }
        byte[] bytes = in.readNBytes(length);
        if(bytes.length != length){
            throw new IOException("Unexpected EOF");
        }
        expectCRLF(in);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int b = in.read();
            if (b == -1) {
                throw new IOException("Unexpected EOF");
            }
            if (b == '\r') {
                int next = in.read();
                if (next != '\n') {
                    throw new IOException("Expected LF");
                }
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }

    private void expectCRLF(InputStream in) throws IOException {
        if (in.read() != '\r' || in.read() != '\n') {
            throw new IOException("Missing CRLF");
        }
    }
}
