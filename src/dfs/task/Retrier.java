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
    private final Queue<String> toBeretried;
    private final Object lock;

    public Retrier(final HashMap<String, Queue<Pair>> toBeRetriedMap,
                   final Queue<String> toBeRetriedNameQueue, final Object lock) {

        this.revokedLocksMap = toBeRetriedMap;
        this.toBeretried = toBeRetriedNameQueue;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Retrying...");
        try {
            while (this.isRunning()) synchronized (this.lock) {
                for (final String lockId : this.toBeretried) {
                    final Queue<Pair> locks = this.revokedLocksMap.get(lockId);
                    while (locks.size() > 0) try {
                        final var lockData = locks.remove();

                        final String[] parsedOwnerId = lockData.getOwnerId().split(":");
                        final Registry registry = LocateRegistry.getRegistry(
                            parsedOwnerId[0],
                            Integer.parseInt(parsedOwnerId[1]));
                        final LockCacheConnector lcc =
                            (LockCacheConnector) registry.lookup("LockCacheService");
                        lcc.retry(lockId, lockData.getSequence());


                    } catch (final RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    this.revokedLocksMap.remove(lockId);
                    this.toBeretried.remove(lockId);
                }
                this.lock.wait();
            }
        } catch (final InterruptedException e){
            System.out.println("Retrier stopped");
        }
    }

}
