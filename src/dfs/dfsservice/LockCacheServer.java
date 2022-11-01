package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;
import dfs.task.Releaser;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockCacheServer implements LockCache, LockCacheConnector, Serializable {
    private final int port;
    private final Registry registry;

    private final ExtentConnector extentServer;
    private final LockConnector lockServer;

    private List<String> locks = new ArrayList<>();
    private List<String> toBeReleased = new ArrayList<>();
    private List<String> freeLocks = new ArrayList<>();
    private List<String> lockedLocks = new ArrayList<>();

    private final String address;
    private long sequence = 0;

    private Releaser releaser;



    public LockCacheServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws IOException, AlreadyBoundException {
        this.port = port;
        this.extentServer = extentServer;
        this.lockServer = lockServer;

        registry = LocateRegistry.getRegistry(port);
        LockCacheConnector stub = (LockCacheConnector) UnicastRemoteObject.exportObject(this, port);
        registry.bind("LockCacheService", stub);

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        address = String.valueOf(socket.getLocalAddress());
        socket.close();

        releaser = new Releaser(locks, toBeReleased, this, lockServer, getOwnerId(address, port), freeLocks);


//        System.out.println("Lcc is running on: " + LocateRegistry.getRegistry(ownerId[0], Integer.parseInt(ownerId[1])).lookup(LockCacheServer.SERVICE_NAME));
        System.out.println("Lcc is running on port: " + port);
    }

    @Override
    public void acquire(String lockId) throws RemoteException, InterruptedException, NotBoundException {
        System.out.printf("%s is trying to acquire lock %s%n", getOwnerId(address, port), lockId);
        synchronized (this) {
            while (!lockedLocks.contains(lockId)
                    && !freeLocks.contains(lockId)
                    && (locks.contains(lockId) || !lockServer.acquire(lockId, address, sequence++))) {
                locks.add(lockId);

                if (!locks.contains(lockId)){
                    locks.add(lockId);
                }

                lockServer.acquire(lockId, getOwnerId(address, port), sequence++);
                wait();
            }

            lockedLocks.add(lockId);
            locks.remove(lockId);
            freeLocks.remove(lockId);

        }
    }

    @Override
    public void doRelease() {
        return;
    }

    @Override
    public synchronized void release(String lockId) {
        System.out.println("client got released" + lockId);
        synchronized (this) {
            if (lockedLocks.contains(lockId)) {
                freeLocks.add(lockId);
                lockedLocks.remove(lockId);
                this.notifyAll();
            }
        }
    }

    @Override
    public void stop() throws NotBoundException, RemoteException {
        releaser.stop();
        this.notifyAll();
        registry.unbind(SERVICE_NAME);
        UnicastRemoteObject.unexportObject(this, true);
    }

    @Override
    public void retry(String lockId, long sequence) throws RemoteException {
        synchronized (this){
            locks.remove(lockId);
            this.notifyAll();
        }
    }

    @Override
    public void revoke(String lockId) throws RemoteException {
        synchronized (this) {
            System.out.println("client got revoked " + lockId);
            if (!toBeReleased.contains(lockId))
                toBeReleased.add(lockId);
            releaser.start();
            this.notifyAll();
        }


    }

    private String getOwnerId(String ip, int port){
        String uuid = UUID.randomUUID().toString();
        return ip + ":" + port + ":" + uuid;
    }
}
