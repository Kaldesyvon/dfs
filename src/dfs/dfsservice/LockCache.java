package dfs.dfsservice;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface LockCache {

    void acquire(String lockId) throws RemoteException, InterruptedException, NotBoundException;
    void doRelease();
    void release(String lockId);
    void stop() throws NotBoundException, RemoteException;
}
