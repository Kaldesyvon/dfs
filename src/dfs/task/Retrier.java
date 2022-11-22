package dfs.task;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Queue;

import dfs.dfsservice.LockCacheConnector;
import dfs.lockservice.Pair;

public class Retrier extends AbstractTask {
    private final HashMap<String, Queue<Pair>> revokedLocksMap;
    private final Queue<String> toBeRetried;
    private final Object lock;

    public Retrier(final HashMap<String, Queue<Pair>> toBeRetriedMap,
                   final Queue<String> toBeRetriedNameQueue, final Object lock) {

        this.revokedLocksMap = toBeRetriedMap;
        this.toBeRetried = toBeRetriedNameQueue;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Retrying...");
        try {
            while (this.isRunning()) synchronized (this.lock) {
                for (final String lockId : this.toBeRetried) {
                    final Queue<Pair> locks = this.revokedLocksMap.get(lockId);
                    while (locks.size() >= 1) {
                        final Pair lock = locks.remove();
                        final String[] ownerId = lock.getOwnerId().split(":");
                        final String host = ownerId[0];
                        final int port = Integer.parseInt(ownerId[1]);
                        final Registry registry = LocateRegistry.getRegistry(host, port);
                        final LockCacheConnector lockCacheConnector = (LockCacheConnector) registry.lookup("LockCacheService");
                        lockCacheConnector.retry(lockId, lock.getSequence());
                    }
                    this.toBeRetried.remove(lockId);
                    this.revokedLocksMap.remove(lockId);
                }
                this.lock.wait();
            }
        } catch (final InterruptedException | RemoteException | NumberFormatException | NotBoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        super.stop();
        synchronized (this.lock) {
            this.lock.notifyAll();
        }
    }
}
