package dfs.task;

import dfs.dfsservice.ExtentCache;
import dfs.lockservice.LockConnector;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

public class Releaser extends AbstractTask {

    private final List<String> freeList;
    private final List<String> toBeReleasedList;
    private final LockConnector lockServer;
    private final String ownerId;
    private final Object lock;
    private final ExtentCache extentCache;

    public Releaser(ExtentCache extentCache, List<String> toBeReleasedList, List<String> freeList,
                    String ownerId, LockConnector lockServer, final Object lock) {
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
            while (this.isRunning()) {
                synchronized (lock) {
                    for( int i = 0; i < toBeReleasedList.size(); i++ )
                    {
                        String lock = toBeReleasedList.get( i );
                        if (freeList.contains(lock)) {
                            freeList.remove(lock);
                            extentCache.flush(lock);
                            toBeReleasedList.remove(lock);
                            lockServer.release(lock, ownerId);
                            i--;
                        }
                    }

                    lock.wait();
                }
            }
        } catch (RemoteException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
