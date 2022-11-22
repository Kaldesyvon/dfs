package dfs.dfsservice;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dfs.lockservice.LockConnector;
import dfs.task.Releaser;

public class LockCacheServer implements dfs.dfsservice.LockCache, LockCacheConnector {

    private Registry registry;
    private String address;
    private LockConnector lockServer;
    private Releaser releaser;
    private final List<String> lockedLocks = new ArrayList<>();
    private final List<String> toBeAcquired = new ArrayList<>();
    private final List<String> toBeReleased = new ArrayList<>();
    private final List<String> freeLocks = new ArrayList<>();
    private long acquire = 0;

    LockCacheServer(final int port, final ExtentCache extentCache, final LockConnector lockServer) {
        try {
            this.registry = LocateRegistry.getRegistry(port);
            final var socket = new Socket("google.com", 80);
            final var ip = socket.getLocalAddress().getHostAddress();
            socket.close();
            this.address = LockCacheServer.generateFullAddress(ip, port);

            final var lockCacheService = (LockCacheConnector) UnicastRemoteObject.exportObject(this, port);
            this.registry.bind("LockCacheService", lockCacheService);

            this.lockServer = lockServer;

            this.releaser = new Releaser(extentCache, this.toBeReleased, this.freeLocks,
                this.address, lockServer, this);

            System.out.println("LockCacheServer is running");
        } catch (final IOException | AlreadyBoundException e){
            e.printStackTrace();
        }


    }


    @Override
    public void acquire(final String lockId) {
        synchronized (this) {
            try {
                while ((!this.freeLocks.contains(lockId) && !this.lockedLocks.contains(lockId)) && (this.toBeAcquired.contains(lockId) || !this.lockServer.acquire(lockId, this.address, this.acquire++))) {
                    if (!this.toBeAcquired.contains(lockId)) this.toBeAcquired.add(lockId);
                    this.wait();
                }
            } catch (final RemoteException | InterruptedException | NotBoundException e) {
                e.printStackTrace();
            }
            this.freeLocks.remove(lockId);
            this.toBeAcquired.remove(lockId);
            this.lockedLocks.add(lockId);
        }
    }



    @Override
    public void release(final String lockId) {
        synchronized (this) {
            if (this.lockedLocks.contains(lockId)){
                this.lockedLocks.remove(lockId);
                this.freeLocks.add(lockId);
                this.notifyAll();
            }
        }
    }

    @Override
    public void doRelease() {
    }

    @Override
    public synchronized void revoke(final String lockId) {
        synchronized (this) {
            if (!this.toBeReleased.contains(lockId)) this.toBeReleased.add(lockId);
            this.releaser.start();
            this.notifyAll();
        }

    }

    @Override
    public void retry(final String lockId, final long sequence) {
        synchronized (this){
            this.toBeAcquired.remove(lockId);
            this.notifyAll();
        }
    }

    private static String generateFullAddress(final String ip, final int port) {
        final byte[] array = new byte[7];
        new Random().nextBytes(array);
        final String generatedRandomString = new String(array, StandardCharsets.UTF_8);
        return ip + ":" + port + ":" + generatedRandomString;
    }

    @Override
    public void stop() {
        try {
            this.registry.unbind("LockCacheService");
            UnicastRemoteObject.unexportObject(this, true);
        } catch (final IOException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
