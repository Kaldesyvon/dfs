package dfs.dfsservice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LockCacheConnector extends Remote {
    String SERVICE_NAME = "LockCacheService";

    void retry(String lockId, long sequence) throws RemoteException;
    void revoke(String lockId) throws RemoteException;
}
