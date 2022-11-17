package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;
import dfs.task.Releaser;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LockCacheServer implements dfs.dfsservice.LockCache, LockCacheConnector {

    private final Registry registry;
    private final String address;
    private final LockConnector lockServer;
    private final Releaser releaser;
    private final List<String> lockedList = new ArrayList<>();
    private final List<String> toBeAcquired = new ArrayList<>();
    private final List<String> toBeReleased = new ArrayList<>();
    private final List<String> freeLocks = new ArrayList<>();
    private long acquire = 0;

    LockCacheServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws IOException, AlreadyBoundException {


        this.registry = LocateRegistry.getRegistry(port);
        var socket = new Socket("google.com", 80);
        var ip = socket.getLocalAddress().getHostAddress();
        socket.close();
        this.address = this.generateFullAddress(ip, port);

        var lockCacheService = (LockCacheConnector) UnicastRemoteObject.exportObject(this, port);
        this.registry.bind("LockCacheService", lockCacheService);

        this.lockServer = lockServer;

        this.releaser = new Releaser(toBeReleased, freeLocks,
                address, lockServer, this);

        System.out.println("LockCacheServer is running");
    }


    @Override
    public void acquire(String lockId) throws NotBoundException, RemoteException, InterruptedException {
        synchronized (this) {
            System.out.println("acquiring with lockId: " + lockId);
            while ((toBeAcquired.contains(lockId)
                    && (!lockedList.contains(lockId) && !freeLocks.contains(lockId))
                    || !lockServer.acquire(lockId, address, acquire++))) {

                if (!toBeAcquired.contains(lockId))
                    toBeAcquired.add(lockId);

                this.wait();
            }
            lockedList.add(lockId);
            toBeAcquired.remove(lockId);
            freeLocks.remove(lockId);
        }
    }



    @Override
    public void release(String lockId) {
        synchronized (this) {
            System.out.println("releasing with lockId: " + lockId);
            if (lockedList.contains(lockId)){
                lockedList.remove(lockId);
                freeLocks.add(lockId);
                this.notifyAll();
            }
        }
    }

    @Override
    public void doRelease() {
        return;
    }

    @Override
    public synchronized void revoke(String lockId) throws RemoteException {
        synchronized (this) {
            System.out.println("revoking with lockId: " + lockId);
            if (!toBeReleased.contains(lockId)) {
                toBeReleased.add(lockId);
            }
            releaser.start();
            this.notifyAll();
        }

    }

    @Override
    public void retry(String lockId, long sequence) throws RemoteException {
        synchronized (this){
            System.out.println("retrying with lockId: " + lockId);
            toBeAcquired.remove(lockId);
            this.notifyAll();
        }
    }

    private String generateFullAddress(String ip, int port) {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        String generatedRandomString = new String(array, StandardCharsets.UTF_8);
        return ip + ":" + port + ":" + generatedRandomString;
    }

    @Override
    public void stop() throws NotBoundException, RemoteException {
        releaser.stop();
        this.notifyAll();

        registry.unbind("LockCacheService");
        UnicastRemoteObject.unexportObject(this, true);
    }
}
