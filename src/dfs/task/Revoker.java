package dfs.task;

import dfs.dfsservice.LockCacheConnector;
import dfs.lockservice.Pair;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Queue;

public class Revoker extends AbstractTask {

    private final HashMap<String, Pair> lockMap;
    private final Queue<String> toBeRevokedQueue;
    private final Object lock;
    private final Queue<String> toBeRetriedQueue;

    public Revoker(HashMap<String, Pair> lockMap, Queue<String> toBeRevokedQueue,
                   Queue<String> toBeRetriedNameQueue, Object lock){

        this.toBeRetriedQueue = toBeRetriedNameQueue;
        this.lockMap = lockMap;
        this.toBeRevokedQueue = toBeRevokedQueue;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Revoking");
        try {
            while (this.isRunning()) {
                synchronized (lock) {
                    while (toBeRevokedQueue.size() > 0) {
                        var lockId = toBeRevokedQueue.remove();

                        var parsedOwnerId = this.lockMap.get(lockId).getOwnerId().split(":");
                        Registry registry = LocateRegistry.getRegistry(
                                parsedOwnerId[0],
                                Integer.parseInt(parsedOwnerId[1]));
                        var lcc =
                                (LockCacheConnector) registry.lookup("LockCacheService");
                        lcc.revoke(lockId);

                        toBeRetriedQueue.add(lockId);
                    }
                    lock.wait();
                }
            }
        } catch (InterruptedException | RemoteException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
