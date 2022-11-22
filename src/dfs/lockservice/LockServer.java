package dfs.lockservice;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import dfs.task.Retrier;
import dfs.task.Revoker;

public class LockServer implements dfs.lockservice.LockConnector {

    private Registry registry;
    private final HashMap<String, Pair> lockMap = new HashMap<>();
    private Revoker revoker;
    private Retrier retrier;
    private final Queue<String> toBeRevoked = new LinkedList<>();
    private final HashMap<String, Queue<Pair>> revokedLocks = new HashMap<>();

    public LockServer(final int port) {
        try {
            this.registry = LocateRegistry.createRegistry(port);
            final LockConnector lockServer = (LockConnector) UnicastRemoteObject.exportObject(this, port);
            this.registry.bind("LockService", lockServer);

            final Queue<String> toBeRetriedQueue = new LinkedList<>();
            this.revoker = new Revoker(this.lockMap, this.toBeRevoked,
                toBeRetriedQueue, this);
            this.retrier = new Retrier(this.revokedLocks,
                toBeRetriedQueue, this);

            System.out.println("LockServer is running");
        } catch (final IOException | AlreadyBoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean acquire(final String lockId, final String ownerId, final long sequence) {
        synchronized (this) {
            if (!this.lockMap.containsKey(lockId)) {
                this.lockMap.put(lockId, new Pair(ownerId, sequence));
                return true;
            } else {
                if (!this.toBeRevoked.contains(lockId))
                    this.toBeRevoked.add(lockId);


                final Pair unsuccessful = new Pair(ownerId, sequence);

                if (this.revokedLocks.containsKey(lockId)) this.revokedLocks.get(lockId).add(unsuccessful);
                else {
                    this.revokedLocks.put(lockId, new LinkedList<>());
                    this.revokedLocks.get(lockId).add(unsuccessful);
                }

                this.revoker.start();
                this.notifyAll();

                return false;
            }
        }
    }

    @Override
    public void release(final String lockId, final String ownerId) throws RemoteException {
        synchronized (this) {
            final var lock = this.lockMap.get(lockId);
            if (ownerId.equals(lock.getOwnerId())) {
                this.lockMap.remove(lockId);
                this.retrier.start();

                this.notifyAll();
            }
        }
    }


    @Override
    public void stop() throws RemoteException, NotBoundException {
//        revoker.stop();
//        retrier.stop();
//        this.notifyAll();

        this.registry.unbind("LockService");
        UnicastRemoteObject.unexportObject(this, true);
    }
}
