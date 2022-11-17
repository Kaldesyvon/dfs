package dfs.task;

import dfs.dfsservice.LockCacheConnector;
import dfs.lockservice.Pair;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Queue;

public class Retrier extends AbstractTask {
    private final HashMap<String, Queue<Pair>> revokedLocksMap;
    private final Queue<String> toBeRetriedQueue;
    private final Object lock;

    public Retrier(HashMap<String, Queue<Pair>> toBeRetriedMap,
                   Queue<String> toBeRetriedNameQueue, Object lock) {

        this.revokedLocksMap = toBeRetriedMap;
        this.toBeRetriedQueue = toBeRetriedNameQueue;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Retrying");
        try {
            while (this.isRunning()) {
                synchronized (lock) {
                    for (String lockId : toBeRetriedQueue) {
                        var locks = revokedLocksMap.get(lockId);
                        while (locks.size() > 0) {
                            var lockData = locks.remove();

                            var parsedOwnerId = lockData.getOwnerId().split(":");
                            Registry registry = LocateRegistry.getRegistry(
                                    parsedOwnerId[0],
                                    Integer.parseInt(parsedOwnerId[1]));
                            var lcc = (LockCacheConnector) registry.lookup("LockCacheService");
                            lcc.retry(lockId, lockData.getSequence());
                        }
                        revokedLocksMap.remove(lockId);
                        toBeRetriedQueue.remove(lockId);
                    }
                    lock.wait();
                }
            }
        } catch (InterruptedException | RemoteException | NotBoundException e){
            throw new RuntimeException(e);
        }
    }
}
