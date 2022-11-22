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
    private final Queue<String> toBeRevoked;
    private final Object lock;
    private final Queue<String> toBeRetried;

    public Revoker(final HashMap<String, Pair> lockMap, final Queue<String> toBeRevoked,
                   final Queue<String> toBeRetried, final Object lock){

        this.toBeRetried = toBeRetried;
        this.lockMap = lockMap;
        this.toBeRevoked = toBeRevoked;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Revoking");
        try {
            while (this.isRunning()) synchronized (this.lock) {
                while (this.toBeRevoked.size() > 0) {
                    final String lockId = this.toBeRevoked.remove();
                    final String[] ownerId = this.lockMap.get(lockId).getOwnerId().split(":");
                    final String host = ownerId[0];
                    final int port = Integer.parseInt(ownerId[1]);
                    final Registry registry = LocateRegistry.getRegistry(host, port);
                    final LockCacheConnector lockCacheConnector = (LockCacheConnector) registry.lookup("LockCacheService");
                    lockCacheConnector.revoke(lockId);
                    this.toBeRetried.add(lockId);
                }
                this.lock.wait();
            }
        } catch (final InterruptedException | NumberFormatException | RemoteException | NotBoundException e){
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
