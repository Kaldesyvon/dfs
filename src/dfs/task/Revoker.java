package dfs.task;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Queue;

import dfs.dfsservice.LockCacheConnector;
import dfs.lockservice.Pair;

public class Revoker extends AbstractTask {

    private final HashMap<String, Pair> lockMap;
    private final Queue<String> toBeRevokedQueue;
    private final Object lock;
    private final Queue<String> toBeRetriedQueue;

    public Revoker(final HashMap<String, Pair> lockMap, final Queue<String> toBeRevokedQueue,
                   final Queue<String> toBeRetriedNameQueue, final Object lock){

        this.toBeRetriedQueue = toBeRetriedNameQueue;
        this.lockMap = lockMap;
        this.toBeRevokedQueue = toBeRevokedQueue;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Revoking");
        try {
            while (this.isRunning()) synchronized (this.lock) {
                while (this.toBeRevokedQueue.size() > 0) try {
                    final var lockId = this.toBeRevokedQueue.remove();

                    final var parsedOwnerId = this.lockMap.get(lockId).getOwnerId().split(":");
                    final Registry registry = LocateRegistry.getRegistry(
                        parsedOwnerId[0],
                        Integer.parseInt(parsedOwnerId[1]));
                    final LockCacheConnector lcc =
                        (LockCacheConnector) registry.lookup("LockCacheService");
                    lcc.revoke(lockId);

                    this.toBeRetriedQueue.add(lockId);
                } catch (final RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
                this.lock.wait();
            }
        } catch (final InterruptedException e){
            e.printStackTrace();
        }
    }
}
