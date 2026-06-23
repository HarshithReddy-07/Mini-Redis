package com.harshith.miniredis.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class BinarySnapshotManager implements SnapshotManager{
    private static final byte[] MAGIC = {'M', 'R', 'D', 'B'};
    private static final int VERSION = 1;
    private final Path snapshotPath;

    public BinarySnapshotManager(Path snapshotPath){
        this.snapshotPath = snapshotPath;
    }

    @Override
    public void createSnapshot(Map<String, String> data) throws IOException{
        Path parent = snapshotPath.getParent();
        if(parent != null){
            Files.createDirectories(parent);
        }
        Path tempPath = snapshotPath.resolveSibling(snapshotPath.getFileName() + ".tmp");
        try(
            FileChannel channel = 
                    FileChannel.open(
                        tempPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            DataOutputStream out = 
                        new DataOutputStream(
                            new BufferedOutputStream(
                                java.nio.channels.Channels.newOutputStream(channel)
                            )
                        );            
        ){
            out.write(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(data.size());
            for(Map.Entry<String, String> entry : data.entrySet()){
                writeString(out, entry.getKey());
                writeString(out, entry.getValue());
            }
            out.flush();
            channel.force(true);
        }
        try {
            Files.move(
                    tempPath,
                    snapshotPath,
                    java.nio.file.StandardCopyOption
                            .REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption
                            .ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {

            Files.move(
                    tempPath,
                    snapshotPath,
                    java.nio.file.StandardCopyOption
                            .REPLACE_EXISTING);
        }
    }

    @Override
    public Map<String, String> loadSnapshot() throws IOException{
        Map<String, String> result =
                new HashMap<>();

        if (!Files.exists(snapshotPath)) {
            return result;
        }
        try (
                DataInputStream in =
                        new DataInputStream(
                                new BufferedInputStream(
                                        Files.newInputStream(
                                                snapshotPath)))
        ){
            byte[] magic = new byte[4];
            in.readFully(magic);

            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException(
                        "Invalid snapshot magic");
            }

            int version = in.readInt();

            if (version != VERSION) {
                throw new IOException(
                        "Unsupported snapshot version: "
                                + version);
            }

            int count = in.readInt();

            for (int i = 0; i < count; i++) {
                String key = readString(in);
                String value = readString(in);
                result.put(key, value);
            }    
        } 
        return result;
    }

    private void writeString(
            DataOutputStream out,
            String value)
            throws IOException {

        byte[] bytes =
                value.getBytes(
                        StandardCharsets.UTF_8);

        out.writeInt(bytes.length);

        out.write(bytes);
    }

    private String readString(
            DataInputStream in)
            throws IOException {

        int length = in.readInt();

        if (length < 0) {
            throw new IOException(
                    "Negative string length");
        }

        byte[] bytes = new byte[length];

        in.readFully(bytes);

        return new String(
                bytes,
                StandardCharsets.UTF_8);
    }
}
