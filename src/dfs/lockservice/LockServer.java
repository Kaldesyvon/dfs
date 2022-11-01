package dfs.lockservice;

import dfs.task.Retrier;
import dfs.task.Revoker;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;



public class LockServer implements LockConnector, Serializable {
    private final Registry registry;
    private HashMap<String, Pair> locks = new HashMap<String, Pair>();
    private Queue<String> toBeRevoked = new LinkedList<String>();
    private Queue<String> toBeReleased = new LinkedList<String>();
    private Queue<String> toBeRetried = new LinkedList<String>();
    private final Revoker revoker;
    private final Retrier retrier;


    private String ownerId = null;

    public LockServer(int port) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        var lockServer = (LockConnector) UnicastRemoteObject.exportObject(this, port);
        registry.bind(LockConnector.SERVICE_NAME, lockServer);

        this.retrier = new Retrier(locks, toBeRetried, lockServer);
        this.revoker = new Revoker(locks, toBeRetried, toBeRevoked, this);

        System.out.println("LockServer lock and loaded");
    }

    @Override
    public boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException {
        synchronized (this){
            System.out.printf("%s is acquiring with lockID %s %n", ownerId, lockId);

            if (!locks.containsKey(lockId)) {
                locks.put(lockId, new Pair(ownerId, sequence));
                notify();
                return true;
            } else {
                if (!toBeRevoked.contains(lockId))
                    toBeRevoked.add(lockId);

                revoker.start();

                this.notifyAll();
                return false;
            }
        }
    }


    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
        System.out.printf("%s is releasing with lockID %s %n", ownerId, lockId);
        synchronized (this) {
            if (locks.get(lockId) != null && locks.get(lockId).equals(ownerId)) {
                locks.remove(lockId);
                retrier.start();

                this.notifyAll();
            }
        }
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        locks.clear();
        registry.unbind(LockConnector.SERVICE_NAME);
        UnicastRemoteObject.unexportObject(this, true);
    }


}
