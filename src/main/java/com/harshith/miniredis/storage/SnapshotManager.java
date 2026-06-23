package com.harshith.miniredis.storage;

import java.io.IOException;
import java.util.Map;

public interface SnapshotManager {

    void createSnapshot(
        Map<String, String> data
    ) throws IOException;

    Map<String, String> loadSnapshot()
        throws IOException;
    
} 
