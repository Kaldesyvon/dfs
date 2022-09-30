package src.dfs.lockservice;

import javax.management.remote.rmi.RMIServer;
import java.rmi.RemoteException;

public class LockServer implements LockConnector {
    public LockServer(int port) throws RemoteException {

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
