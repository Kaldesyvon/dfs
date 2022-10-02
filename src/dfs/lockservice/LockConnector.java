package dfs.lockservice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LockConnector extends Remote {
    void acquire(String lockId, String ownerId, long sequence) throws RemoteException;
    void release(String lockId, String ownerId) throws RemoteException;
    void stop() throws RemoteException;
}
