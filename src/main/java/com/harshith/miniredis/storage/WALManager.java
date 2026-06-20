package com.harshith.miniredis.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.function.Consumer;

public class WALManager implements AutoCloseable {

    private final FileChannel fileChannel;
    private final Path path;
    private static final int MAX_RECORD_SIZE = 1024 * 1024; // 1 MB
    
    public WALManager(String filePath) throws IOException{
        this.path = Path.of(filePath);
        this.fileChannel = FileChannel.open(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND
        );
    }

    public synchronized void append(String record) throws IOException{
        byte[] payload = record.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(payload);
        int checkSum = (int)crc.getValue();
        ByteBuffer buffer = ByteBuffer.allocate(8 + payload.length);
        buffer.putInt(payload.length);
        buffer.put(payload);
        buffer.putInt(checkSum);
        buffer.flip();
        while(buffer.hasRemaining()){
            fileChannel.write(buffer);
        }
        fileChannel.force(true);
    }

    public void recover(Consumer<String> handler) throws IOException {
        try (FileChannel recoveryChannel =
                FileChannel.open(
                    path,
                    StandardOpenOption.READ)) {
            ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
            ByteBuffer checksumBuffer = ByteBuffer.allocate(Integer.BYTES);
            while (true) {
                lengthBuffer.clear();
                checksumBuffer.clear();
                if(!readFully(recoveryChannel, lengthBuffer)){
                    break;
                }
                lengthBuffer.flip();
                int length = lengthBuffer.getInt();
                if (length <= 0 || length > MAX_RECORD_SIZE) {
                    break;
                }
                ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
                if (!readFully(recoveryChannel, payloadBuffer)) {
                    break;
                }
                payloadBuffer.flip();
                byte[] payload = new byte[length];
                payloadBuffer.get(payload);
                if (!readFully(recoveryChannel, checksumBuffer)) {
                    break;
                }
                checksumBuffer.flip();
                int storedCheckSum = checksumBuffer.getInt();
                CRC32 crc = new CRC32();
                crc.update(payload);
                int calculatedCheckSum = (int)crc.getValue();
                if(storedCheckSum != calculatedCheckSum){
                    break;
                }
                String command = new String(payload, StandardCharsets.UTF_8);
                handler.accept(command);
            }            
        }
    }

    private boolean readFully( FileChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int bytesRead =
                channel.read(buffer);
            if (bytesRead == -1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException{
        if(fileChannel.isOpen()){
            fileChannel.close();
        }
    }
}
