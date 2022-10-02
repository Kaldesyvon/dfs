package dfs.lockservice;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LockServer implements LockConnector {
    private Registry registry;

    public LockServer(int port) throws RemoteException {
        registry = LocateRegistry.createRegistry(port);
    }

    @Override
    public void acquire(String lockId, String ownerId, long sequence) throws RemoteException {

    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {

    }

    @Override
    public void stop() throws RemoteException {

    }
}
