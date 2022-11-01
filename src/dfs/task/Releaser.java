package dfs.task;

import dfs.lockservice.LockConnector;
import dfs.lockservice.Pair;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Releaser extends AbstractTask implements Serializable {

    private final Object lock;
    private final String ownerId;
    private List<String> locks;
    private List<String> toBeReleased;
    private LockConnector lockServer;

    public Releaser(List<String> locks, List<String> toBeReleased, Object lock, LockConnector lockServer, String ownerId){
        this.locks = locks;
        this.toBeReleased = toBeReleased;
        this.lock = lock;
        this.ownerId = ownerId;
        this.lockServer = lockServer;
    }

    @Override
    public void start() {
        System.out.println("Releaser started");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("Releaser stopped");
        super.stop();
    }

    @Override
    public synchronized void run() {
        System.out.println("Releasing...");
        try {
            for (var releasing : toBeReleased) {
                try {
                    var release = releasing;
                    toBeReleased.remove(releasing);
                    lockServer.release(release, ownerId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            }

            lock.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
