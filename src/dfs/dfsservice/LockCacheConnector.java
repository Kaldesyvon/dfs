package dfs.dfsservice;

import java.rmi.Remote;

public interface LockCacheConnector extends Remote {
    String SERVICE_NAME = "LockCacheServer";

    void retry(String lockId, long sequence);
    void revoke(String lockId);
}
