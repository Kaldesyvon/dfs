package dfs.lockservice;

import dfs.dfsservice.LockStatus;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LockServer implements LockConnector, Serializable {
    private final Registry registry;
    private final List<String> locksIds = new ArrayList<>();

    private String ownerId = null;

    public LockServer(int port) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        registry.bind(SERVICE_NAME, this);
    }

    @Override
    public boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException {
        if(Objects.equals(this.ownerId, ownerId) || ownerId == null ) {
            locksIds.add(lockId);
            return true;
        }

        return false;
    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
        locksIds.remove(lockId);
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        registry.unbind(SERVICE_NAME);
    }
}
