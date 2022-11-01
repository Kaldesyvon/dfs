package dfs.lockservice;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LockConnector extends Remote {
    String SERVICE_NAME = "LockService";
    boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException, NotBoundException;
    void release(String lockId, String ownerId) throws RemoteException;
    void stop() throws RemoteException, NotBoundException;
}
