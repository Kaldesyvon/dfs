package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.extentservice.ExtentServer;
import dfs.lockservice.Pair;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExtentCacheServer implements ExtentCache, ExtentConnector {

    private final Registry registry;
    private final ExtentConnector extentServer;
    private HashMap<String, byte[]> cache = new HashMap<>();
    private List<String> dirties = new ArrayList<>();

    public ExtentCacheServer(int port, ExtentConnector extentServer) throws RemoteException, AlreadyBoundException {
        this.registry = LocateRegistry.getRegistry(port);

        ExtentConnector extentCacheService = (ExtentConnector) UnicastRemoteObject.exportObject(this, port);
        this.registry.bind("ExtentCacheService", extentCacheService);
        this.extentServer = extentServer;

        System.out.println("ExtentCacheService is ready");
    }

    @Override
    public byte[] get(String fileName) throws IOException {
        System.out.println("extent cache get " + fileName);
        synchronized (this) {
            if (!cache.containsKey(fileName)) {
                cache.put(fileName, extentServer.get(fileName));
                System.out.println("file " + fileName + " put into cache");
            }
            System.out.println("returning " + fileName + " from cache");
            return cache.get(fileName);
        }
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws IOException {
        synchronized (this) {
            System.out.println("putting " + fileName +" with content: " + Arrays.toString(fileData));
            this.get(fileName);

            if (!cache.containsKey(fileName)) return false;

            if (fileData == null){
                cache.put(fileName, null);
                dirties.add(fileName);
            }
            if (!Arrays.equals(fileData, cache.get(fileName))){
                cache.put(fileName, fileData);
                dirties.add(fileName);
            }

            return true;
        }
    }

    @Override
    public void stop() throws NotBoundException, RemoteException {
        registry.unbind("ExtentCacheService");
        UnicastRemoteObject.unexportObject(this, true);
    }

    @Override
    public void update(String fileName) throws IOException {
        synchronized (this) {
            if (dirties.contains(fileName)) {
                extentServer.put(fileName, cache.get(fileName));
                dirties.remove(fileName);
            }
        }
    }

    @Override
    public void flush(String fileName) {
        synchronized (this) {
            cache.remove(fileName);
            dirties.remove(fileName);
        }
    }
}
