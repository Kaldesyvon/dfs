package dfs.task;

import java.rmi.RemoteException;
import java.util.List;

import dfs.dfsservice.ExtentCache;
import dfs.lockservice.LockConnector;

public class Releaser extends AbstractTask {

    private final List<String> freeList;
    private final List<String> toBeReleasedList;
    private final LockConnector lockServer;
    private final String ownerId;
    private final Object lock;
    private final ExtentCache extentCache;

    public Releaser(final ExtentCache extentCache, final List<String> toBeReleasedList, final List<String> freeList,
                    final String ownerId, final LockConnector lockServer, final Object lock) {
        this.toBeReleasedList = toBeReleasedList;
        this.freeList = freeList;
        this.lockServer = lockServer;
        this.ownerId = ownerId;
        this.lock = lock;
        this.extentCache = extentCache;
    }

    @Override
    @SuppressWarnings({"en", "ForLoopReplaceableByForEach"})
    public void run() {
        try{
            while (this.isRunning()) synchronized (this.lock) {
                for (int i = 0; i < this.toBeReleasedList.size(); i++) {
                    final String lock = this.toBeReleasedList.get(i);
                    if (this.freeList.contains(lock)) try {
                        this.freeList.remove(lock);
                        this.extentCache.flush(lock);
                        this.toBeReleasedList.remove(lock);
                        this.lockServer.release(lock, this.ownerId);
                        i--;
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.lock.wait();
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }
}
