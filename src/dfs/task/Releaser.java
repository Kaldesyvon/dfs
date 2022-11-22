package dfs.task;

import java.rmi.RemoteException;
import java.util.List;

import dfs.dfsservice.ExtentCache;
import dfs.lockservice.LockConnector;

public class Releaser extends AbstractTask {

    private final List<String> freeLocks;

    private final List<String> toBeReleased;
    private final LockConnector lockServer;
    private final String ownerId;
    private final Object lock;
    private final ExtentCache extentCache;

    public Releaser(final ExtentCache extentCache, final List<String> toBeReleased, final List<String> freeLocks,
                    final String ownerId, final LockConnector lockServer, final Object lock) {
        this.toBeReleased = toBeReleased;
        this.freeLocks = freeLocks;
        this.lockServer = lockServer;
        this.ownerId = ownerId;
        this.lock = lock;
        this.extentCache = extentCache;
    }

    @Override
    @SuppressWarnings({"en"})
    public void run() {
        try {
            while (this.isRunning()) synchronized (this.lock) {
                for(int i = 0; i < this.toBeReleased.size(); i++ )
                {
                    final String lock = this.toBeReleased.get( i );
                    if (this.freeLocks.contains(lock)) {
                        this.freeLocks.remove(lock);
                        this.extentCache.flush(lock);
                        this.toBeReleased.remove(lock);
                        this.lockServer.release(lock, this.ownerId);
                        i--;
                    }
                }

                this.lock.wait();
            }
        } catch (final InterruptedException | RemoteException e) {
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
