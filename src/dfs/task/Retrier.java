package dfs.task;

import dfs.dfsservice.LockCacheConnector;
import dfs.lockservice.Pair;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;

public class Retrier extends AbstractTask implements Serializable {

    private HashMap<String, Pair> locks;
    private Queue<String> toBeRetried;
    private final Object lock;

    public Retrier(HashMap<String, Pair> locks, Queue<String> toBeRetried, Object lock) {
        this.locks = locks;
        this.toBeRetried = toBeRetried;
        this.lock = lock;
    }


    public void start() {
        System.out.println("Retrier started");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("Retrier stopped");
        super.stop();
    }

    @Override
    public void run() {
        System.out.println("Retrying...");
        synchronized (lock) {
            for (String lockId : toBeRetried) {
                var owner = locks.get(lockId).getOwnerId().split(":");

                try {
                    Registry registry = LocateRegistry.getRegistry(
                            owner[0],
                            Integer.parseInt(owner[1]));

                    var lcc = (LockCacheConnector) registry.lookup(LockCacheConnector.SERVICE_NAME);
                    lcc.retry(lockId, locks.get(lockId).getSequence());
                }
                catch (RemoteException | NotBoundException e) {
                    throw new RuntimeException(e);
                }




                locks.remove(lockId);
                toBeRetried.remove(lockId);
            }
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
