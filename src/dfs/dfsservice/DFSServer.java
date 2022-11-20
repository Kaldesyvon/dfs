package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DFSServer implements DFSConnector, Serializable {
    private final LockCache lockCacheService;
    public final String OWNER_ID = this.toString();
    private final Registry registry;
    private final LockConnector lockServer;
    private final ExtentCache extentCacheServer;


    public DFSServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws IOException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        var dfsServer = (DFSConnector) UnicastRemoteObject.exportObject(this, port);

        registry.bind("DFSService", dfsServer);
        this.extentCacheServer = new ExtentCacheServer(port, extentServer);
        this.lockCacheService = new LockCacheServer(port, extentCacheServer, lockServer);
        this.lockServer = lockServer;
        System.out.println("DFS Server is running");
    }

    @Override
    public synchronized List<String> dir(String directoryName) throws IOException, InterruptedException, NotBoundException {
        System.out.println("client dir " +  directoryName);
        lockCacheService.acquire(directoryName);
        try {
            var dirs = extentCacheServer.get(directoryName);
            extentCacheServer.update(directoryName);
            var res = new String(dirs).replace("\\", "/");
            List<String> result = new ArrayList<>();
            result.add(res.substring(res.lastIndexOf("/") + 1));

            return result;
        } catch (IOException e) {
            return null;
        } finally {
            lockCacheService.release(directoryName);
        }
    }

    @Override
    public synchronized boolean mkdir(String directoryName) throws IOException, InterruptedException, NotBoundException {
        System.out.println("client mkdir " +  directoryName);
        lockCacheService.acquire(directoryName);
        var res = extentCacheServer.put(directoryName, "hello".getBytes());
        extentCacheServer.update(directoryName);
        lockServer.release(directoryName, OWNER_ID);
        return res;
    }

    @Override
    public synchronized boolean rmdir(String directoryName) throws IOException, InterruptedException, NotBoundException {
        System.out.println("client rmdir " +  directoryName);
        lockCacheService.acquire(directoryName);
        var res = extentCacheServer.put(directoryName, null);
        extentCacheServer.update(directoryName);
        lockCacheService.release(directoryName);
        return res;
    }

    @Override
    public synchronized byte[] get(String fileName) throws IOException, InterruptedException, NotBoundException {
        System.out.println("client get " +  fileName);
        if (fileName.endsWith("/")) return null;
        lockCacheService.acquire(fileName);
        var res = extentCacheServer.get(fileName);
        extentCacheServer.update(fileName);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public synchronized boolean put(String fileName, byte[] fileData) throws IOException, InterruptedException, NotBoundException {
        System.out.println("client put " +  fileName);
        lockCacheService.acquire(fileName);
        var res = extentCacheServer.put(fileName, fileData);
        extentCacheServer.update(fileName);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public synchronized boolean delete(String fileName) throws IOException, InterruptedException, NotBoundException {
        System.out.println("deleting " +  fileName);
        lockCacheService.acquire(fileName);
        var res = extentCacheServer.put(fileName, null);
        extentCacheServer.update(fileName);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public void stop() throws NotBoundException, RemoteException {
        registry.unbind("DFSService");
        UnicastRemoteObject.unexportObject(this, true);
    }
}
