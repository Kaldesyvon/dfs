package dfs.task;

import dfs.lockservice.LockConnector;

import java.rmi.RemoteException;
import java.util.List;

public class Releaser extends AbstractTask {

    private final List<String> freeList;
    private final List<String> toBeReleasedList;
    private final LockConnector lockServer;
    private final String ownerId;
    private final Object lock;

    public Releaser(List<String> toBeReleasedList, List<String> freeList,
                    String ownerId, LockConnector lockServer, final Object lock) {
        this.toBeReleasedList = toBeReleasedList;
        this.freeList = freeList;
        this.lockServer = lockServer;
        this.ownerId = ownerId;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Releasing");
        try{
            while (this.isRunning()) {
                synchronized (lock) {
                    for (var lock: toBeReleasedList) {
                        if (freeList.contains(lock)) {
                            freeList.remove(lock);
                            toBeReleasedList.remove(lock);
                            lockServer.release(lock, ownerId);
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
