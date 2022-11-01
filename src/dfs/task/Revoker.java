package dfs.task;

import dfs.dfsservice.LockCacheConnector;
import dfs.dfsservice.LockCacheServer;
import dfs.lockservice.Pair;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;

public class Revoker extends AbstractTask implements Serializable {

    private final Object lock;
    private HashMap<String, Pair> locks;
    private Queue<String> toBeRevoked;
    private Queue<String> toBeRetried;

    public Revoker(HashMap<String, Pair> locks, Queue<String> toBeRetried , Queue<String> toBeRevoked, Object lock){
        this.locks = locks;
        this.toBeRevoked = toBeRevoked;
        this.toBeRetried = toBeRetried;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Revoking...");
        try {
            while (this.isRunning()) {
                synchronized (lock) {
                    while (toBeRevoked.size() > 0) {
                        var lockId = toBeRevoked.remove();
                        var ownerId = locks.get(lockId).getOwnerId().split(":");

                        Registry registry = LocateRegistry.getRegistry(
                                ownerId[0],
                                Integer.parseInt(ownerId[1]));
                        var lcc = (LockCacheConnector) registry.lookup("LockCacheService");
                        lcc.revoke(lockId);
                        toBeRetried.add(lockId);
                    }
                    lock.wait();
                }
            }

        } catch (RemoteException | NotBoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

