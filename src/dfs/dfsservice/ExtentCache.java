package dfs.dfsservice;

import java.io.IOException;

public interface ExtentCache {
    byte[] get(String fileName) throws IOException;
    boolean put(String fileName, byte[] fileData);
    void update(String fileName);
    void flush(String fileName);
}
