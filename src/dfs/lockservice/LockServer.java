package dfs.lockservice;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class LockServer implements LockConnector, Serializable {
    private static final String SERVICE_NAME = "LockService";
    private final Registry registry;
    private final List<String> locksIds = new ArrayList<>();

    public LockServer(int port) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        registry.bind(SERVICE_NAME, this);
    }

    @Override
    public boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException {
        while (locksIds.contains(lockId));
        locksIds.add(lockId);
        return true;
    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
//        locksIDs.remove(lockId);
        locksIds.clear();
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        registry.unbind(SERVICE_NAME);
    }
}
