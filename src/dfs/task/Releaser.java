package dfs.task;

import dfs.lockservice.LockConnector;
import dfs.lockservice.Pair;

import java.io.Serializable;
import java.io.StringReader;
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
    private List<String> freeLocks;
    private LockConnector lockServer;

    public Releaser(List<String> locks, List<String> toBeReleased, Object lock, LockConnector lockServer, String ownerId, List<String> freeLocks) {
        this.locks = locks;
        this.toBeReleased = toBeReleased;
        this.lock = lock;
        this.ownerId = ownerId;
        this.lockServer = lockServer;
        this.freeLocks = freeLocks;
    }

    @Override
    public synchronized void run() {
        System.out.println("Releasing...");
        try {
            while (isRunning()) {
                synchronized (lock) {
                    for (int i = 0; i < toBeReleased.size(); i++) {
                        String lock = toBeReleased.get(i);
                        if (freeLocks.contains(lock)) {

                            freeLocks.remove(lock);
                            toBeReleased.remove(lock);
                            i--;
                            lockServer.release(lock, ownerId);

                        }
                    }
                    lock.wait();
                }
            }
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
