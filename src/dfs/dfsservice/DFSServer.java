package dfs.dfsservice;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

public class DFSServer implements DFSConnector, Serializable {
    private LockCache lockCacheServer;
    private Registry registry;
    private ExtentCache extentCacheServer;

    public DFSServer(final int port, final ExtentConnector extentServer, final LockConnector lockServer) {
        try {
            this.registry = LocateRegistry.createRegistry(port);
            final var dfsServer = (DFSConnector) UnicastRemoteObject.exportObject(this, port);

            this.registry.bind("DFSService", dfsServer);

            this.extentCacheServer = new ExtentCacheServer(port, extentServer);
            this.lockCacheServer = new LockCacheServer(port, this.extentCacheServer, lockServer);

            System.out.println("DFS Server is running");
        } catch (final IOException | AlreadyBoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<String> dir(final String directoryName) {
        System.out.println("client dir " +  directoryName);
        this.lockCacheServer.acquire(directoryName);
        final var dirs = this.extentCacheServer.get(directoryName);
        this.extentCacheServer.update(directoryName);
        final var res = new String(dirs).replace("\\", "/");
        final List<String> result = new ArrayList<>();
        result.add(res.substring(res.lastIndexOf("/") + 1));
        this.lockCacheServer.release(directoryName);

        return result;


    }

    @Override
    public synchronized boolean mkdir(final String directoryName) {
        System.out.println("DFSServer mkdir start");
        this.lockCacheServer.acquire(directoryName);
        final boolean bool = this.extentCacheServer.put(directoryName, new byte[10]);
        this.extentCacheServer.update(directoryName);
        this.lockCacheServer.release(directoryName);
        System.out.println("DFSServer mkdir " + directoryName + " success");
        return bool;
    }

    @Override
    public synchronized boolean rmdir(final String directoryName) {
        System.out.println("DFSServer rmdir start");
        this.lockCacheServer.acquire(directoryName);
        final boolean bool = this.extentCacheServer.put(directoryName, null);
        this.extentCacheServer.update(directoryName);
        this.lockCacheServer.release(directoryName);
        System.out.println("DFSServer rmdir " + directoryName + " success");
        return bool;
    }

    @Override
    public synchronized byte[] get(final String fileName) {
        System.out.println("DFSServer get start");
        this.lockCacheServer.acquire(fileName);
        final byte[] bytes = this.extentCacheServer.get(fileName);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);
        System.out.println("DFSServer get " + fileName + " success");
        return bytes;
    }

    @Override
    public synchronized boolean put(final String fileName, final byte[] fileData) {
        System.out.println("DFSServer put start");
        if (fileData == null) return false;
        this.lockCacheServer.acquire(fileName);
        final boolean bool = this.extentCacheServer.put(fileName, fileData);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);
        System.out.println("DFSServer put " + fileName + " success");
        return bool;
    }

    @Override
    public synchronized boolean delete(final String fileName) {
        System.out.println("DFSServer delete start");
        this.lockCacheServer.acquire(fileName);
        final boolean bool = this.extentCacheServer.put(fileName, null);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);
        System.out.println("DFSServer delete " + fileName + " success");
        return bool;
    }

    @Override
    public void stop() {
        try {
            this.lockCacheServer.stop();
            this.registry.unbind("DFSService");
            UnicastRemoteObject.unexportObject(this, true);
            System.out.println("DFS Server stopped");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
