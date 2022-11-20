package dfs.dfsservice;

import java.io.IOException;

public interface ExtentCache {
    byte[] get(String fileName) throws IOException;
    boolean put(String fileName, byte[] fileData) throws IOException;
    void update(String fileName) throws IOException;
    void flush(String fileName);
}
