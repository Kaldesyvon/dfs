package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.extentservice.ExtentServer;
import dfs.lockservice.Pair;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class ExtentCacheServer implements ExtentCache, ExtentConnector {

    private final Registry registry;
    private final ExtentConnector extentServer;
    private HashMap<String, byte[]> cache = new HashMap<>();

    public ExtentCacheServer(int port, ExtentConnector extentServer) throws RemoteException, AlreadyBoundException {
        this.registry = LocateRegistry.getRegistry(port);

        ExtentConnector extentCacheService = (ExtentConnector) UnicastRemoteObject.exportObject(this, port);
        this.registry.bind("ExtentCacheService", extentCacheService);
        this.extentServer = extentServer;

        System.out.println("ExtentCacheService is ready");
    }

    @Override
    public byte[] get(String fileName) throws IOException {
        synchronized (this) {
            if (!cache.containsKey(fileName)) {
                cache.put(fileName, extentServer.get(fileName));
            }
            return cache.get(fileName);
        }
    }

    @Override
    public boolean put(String fileName, byte[] fileData) {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public void update(String fileName) {

    }

    @Override
    public void flush(String fileName) {
        synchronized (this) {
            cache.remove(fileName);
        }
    }
}
