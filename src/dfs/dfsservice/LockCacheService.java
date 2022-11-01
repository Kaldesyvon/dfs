package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;
import dfs.task.Releaser;

import java.io.IOException;
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

public class LockCacheService implements LockCache, LockCacheConnector{
    private final int port;
    private final Registry registry;

    private final ExtentConnector extentServer;
    private final LockConnector lockServer;

    private List<String> locks = new ArrayList<>();
    private List<String> toBeReleased = new ArrayList<>();
    private final String address;
    private long sequence = 0;

    private Releaser releaser;



    public LockCacheService(int port, ExtentConnector extentServer, LockConnector lockServer) throws IOException, AlreadyBoundException {
        this.port = port;
        this.extentServer = extentServer;
        this.lockServer = lockServer;
        registry = LocateRegistry.getRegistry(port);
        LockCacheConnector lockCacheService = (LockCacheConnector) UnicastRemoteObject.exportObject(this, port);
        registry.bind(SERVICE_NAME, lockCacheService);

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        address = String.valueOf(socket.getLocalAddress());
        socket.close();

        releaser = new Releaser(locks, toBeReleased, this, lockServer, getOwnerId(address, port));


        System.out.println("Lcc is running");
    }

    @Override
    public void acquire(String lockId) throws RemoteException, InterruptedException, NotBoundException {
        System.out.printf("%s is trying to acquire lock %s%n", getOwnerId(address, port), lockId);
        synchronized (this) {
            if (!locks.contains(lockId)) {
                locks.add(lockId);
            }
            lockServer.acquire(lockId, getOwnerId(address, port), sequence++);
            this.wait();
        }
    }

    @Override
    public void doRelease() {
        return;
    }

    @Override
    public synchronized void release(String lockId) {
        System.out.println("client got released" + lockId);
        this.notifyAll();

    }

    @Override
    public void stop() throws NotBoundException, RemoteException {

    }

    @Override
    public void retry(String lockId, long sequence) throws RemoteException {
        System.out.println("client got retried " + lockId + sequence);
    }

    @Override
    public synchronized void revoke(String lockId) throws RemoteException {
        System.out.println("client got revoked " + lockId);
        if (!toBeReleased.contains(lockId))
            toBeReleased.add(lockId);
        releaser.start();
        notifyAll();


    }

    private String getOwnerId(String ip, int port){
        String uuid = UUID.randomUUID().toString();
        return ip + ":" + port + ":" + uuid;
    }
}
