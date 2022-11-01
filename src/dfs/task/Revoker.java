package dfs.task;

import dfs.dfsservice.LockCacheConnector;
import dfs.dfsservice.LockCacheService;
import dfs.lockservice.Pair;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
    public void start() {
        System.out.println("Revoker started");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("Revoker stopped");
        super.stop();
    }

    @Override
    public void run() {
        System.out.println("Revoking...");
        synchronized (lock) {
            while (toBeRevoked.size() > 0) {
                String lockId = toBeRevoked.remove();

                var ownerId = this.locks.get(lockId).getOwnerId().split(":");
                System.out.println(ownerId[0] + " and " + ownerId[1]);
                try {
                    Registry registry = LocateRegistry.getRegistry(
                            ownerId[0],
                            Integer.parseInt(ownerId[1]));
                    var lcc = (LockCacheService) registry.lookup(LockCacheService.SERVICE_NAME);
                    System.out.println(lcc.toString());
                    lcc.revoke(lockId);
                    toBeRetried.add(lockId);

                } catch (RemoteException | NotBoundException e) {
                    throw new RuntimeException(e);
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

