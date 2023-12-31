package dfs.dfsservice;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dfs.extentservice.ExtentConnector;

public class ExtentCacheServer implements ExtentCache, ExtentConnector {

    private Registry registry;
    private ExtentConnector extentServer;
    private final HashMap<String, byte[]> cache = new HashMap<>();
    private final List<String> dirties = new ArrayList<>();

    public ExtentCacheServer(final int port, final ExtentConnector extentServer) {
        try {
            this.registry = LocateRegistry.getRegistry(port);

            final ExtentConnector extentCacheService = (ExtentConnector) UnicastRemoteObject.exportObject(this, port);
            this.registry.bind("ExtentCacheService", extentCacheService);
            this.extentServer = extentServer;

            System.out.println("ExtentCacheService is ready");
        } catch (final IOException | AlreadyBoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public byte[] get(final String fileName) {
        System.out.println("extent cache get " + fileName);
        synchronized (this) {
            if (!this.cache.containsKey(fileName)) this.cache.put(fileName, this.extentServer.get(fileName));
            return this.cache.get(fileName);
        }
    }

    @Override
    public boolean put(final String fileName, final byte[] fileData) {
        synchronized (this) {
            System.out.println("putting " + fileName +" with content: " + Arrays.toString(fileData));
            this.get(fileName);

            if (!this.cache.containsKey(fileName)) return false;

            if (fileData == null){
                this.cache.put(fileName, null);
                this.dirties.add(fileName);
            }
            if (!Arrays.equals(fileData, this.cache.get(fileName))){
                this.cache.put(fileName, fileData);
                this.dirties.add(fileName);
            }

            return true;
        }
    }



    @Override
    public void update(final String fileName) {
        synchronized (this) {
            if (this.dirties.contains(fileName)) {
                this.extentServer.put(fileName, this.cache.get(fileName));
                this.dirties.remove(fileName);
            }
        }
    }

    @Override
    public void flush(final String fileName) {
        synchronized (this) {
            this.cache.remove(fileName);
            this.dirties.remove(fileName);
        }
    }

    @Override
    public void stop() {
        try {
            this.registry.unbind("ExtentCacheService");
            UnicastRemoteObject.unexportObject(this, true);
        } catch (final IOException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
