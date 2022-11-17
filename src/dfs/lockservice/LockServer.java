package dfs.lockservice;

import dfs.task.Retrier;
import dfs.task.Revoker;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LockServer implements dfs.lockservice.LockConnector {

    private Registry registry;
    private final HashMap<String, Pair> lockMap = new HashMap<>();
    private final Revoker revoker;
    private final Retrier retrier;
    private final Queue<String> toBeRevoked = new LinkedList<>();
    private final HashMap<String, Queue<Pair>> revokedLocks = new HashMap<>();

    public LockServer(int port) throws RemoteException, AlreadyBoundException {
        this.registry = LocateRegistry.createRegistry(port);
        LockConnector lockServer = (LockConnector) UnicastRemoteObject.exportObject(this, port);
        this.registry.bind("LockService", lockServer);

        Queue<String> toBeRetriedQueue = new LinkedList<>();
        this.revoker = new Revoker(lockMap, toBeRevoked,
                toBeRetriedQueue, this);
        this.retrier = new Retrier(revokedLocks,
                toBeRetriedQueue, this);

        System.out.println("LockServer is running");
    }

    @Override
    public boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException {
        synchronized (this) {
            if (!this.lockMap.containsKey(lockId)) {
                this.lockMap.put(lockId, new Pair(ownerId, sequence));
                return true;
            } else {
                if (!toBeRevoked.contains(lockId))
                    toBeRevoked.add(lockId);


                Pair unsuccessful = new Pair(ownerId, sequence);

                if (revokedLocks.containsKey(lockId)) {
                    revokedLocks.get(lockId).add(unsuccessful);
                } else {
                    revokedLocks.put(lockId, new LinkedList<>());
                    revokedLocks.get(lockId).add(unsuccessful);
                }

                revoker.start();
                this.notifyAll();

                return false;
            }
        }
    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
        synchronized (this) {
            var lock = lockMap.get(lockId);
            if (ownerId.equals(lock.getOwnerId())) {
                lockMap.remove(lockId);
                retrier.start();

                this.notifyAll();
            }
        }
    }


    @Override
    public void stop() throws RemoteException, NotBoundException {
        revoker.stop();
        retrier.stop();
        this.notifyAll();

        registry.unbind("LockService");
        UnicastRemoteObject.unexportObject(this, true);
    }
}
