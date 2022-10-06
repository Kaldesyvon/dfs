package dfs.lockservice;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LockConnector extends Remote {
    boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException;
    void release(String lockId, String ownerId) throws RemoteException;
    void stop() throws RemoteException, NotBoundException;
}
