package dfs.dfsservice;

public interface ExtentCache {
    byte[] get(String fileName);
    boolean put(String fileName, byte[] fileData);
    void update(String fileName);
    void flush(String fileName);
}
