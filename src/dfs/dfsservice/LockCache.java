package dfs.dfsservice;

public interface LockCache {

    void acquire(String lockId);
    void doRelease();
    void release(String lockId);
    void stop();
}
